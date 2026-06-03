import time
from uuid import uuid4

import httpx
from sqlalchemy.orm import Session

from agents.audit.logger import hash_prompt, record_audit
from agents.detection.analyzer import analyze
from agents.sandbox.evaluator import evaluate
from airwall_core.config import get_settings
from airwall_core.models import (
    AuditEvent,
    Decision,
    DetectionResult,
    PipelineResult,
    PromptRequest,
    Route,
)
from airwall_core.rbac import get_group_names


BLOCK_MESSAGE = (
    "Request blocked by Airwall security policy. "
    "If you believe this is an error, contact your administrator."
)


async def run_pipeline(
    session: Session,
    request: PromptRequest,
    *,
    call_production: bool = False,
    messages: list[dict] | None = None,
    model: str | None = None,
) -> PipelineResult:
    correlation_id = str(uuid4())
    started = time.perf_counter()
    agent_path: list[str] = ["detection"]
    all_reasons: list[str] = []

    detection: DetectionResult = analyze(request)
    all_reasons.extend(detection.reasons)
    group_names = get_group_names(session, request.user_id)

    def _audit(decision: Decision, attack_type: str | None = None) -> None:
        elapsed = int((time.perf_counter() - started) * 1000)
        event = AuditEvent(
            correlation_id=correlation_id,
            user_id=request.user_id,
            group_ids=group_names,
            risk_score=detection.risk_score,
            attack_type=attack_type or (detection.attack_types[0] if detection.attack_types else None),
            decision=decision,
            agent_path=agent_path,
            reasons=all_reasons,
            latency_ms=elapsed,
            prompt_hash=hash_prompt(request.prompt),
        )
        settings = get_settings()
        record_audit(
            session,
            event,
            prompt=request.prompt if settings.log_full_prompts else None,
        )

    if detection.route == Route.BLOCK:
        agent_path.append("block")
        _audit(Decision.BLOCKED, "prompt_injection" if detection.attack_types else "high_risk")
        return PipelineResult(
            correlation_id=correlation_id,
            decision=Decision.BLOCKED,
            risk_score=detection.risk_score,
            attack_types=detection.attack_types,
            route=detection.route,
            reasons=all_reasons,
            blocked_message=BLOCK_MESSAGE,
        )

    if detection.route == Route.SANDBOX:
        agent_path.append("sandbox")
        sandbox = evaluate(session, request, detection)
        all_reasons.extend(sandbox.reasons)
        if sandbox.decision == Decision.BLOCKED:
            _audit(Decision.BLOCKED, "permission_denied")
            return PipelineResult(
                correlation_id=correlation_id,
                decision=Decision.BLOCKED,
                risk_score=detection.risk_score,
                attack_types=detection.attack_types,
                route=detection.route,
                reasons=all_reasons,
                blocked_message=BLOCK_MESSAGE,
            )

    decision = Decision.ALLOWED
    production_response = None

    if call_production:
        agent_path.append("production")
        production_response = await _call_production(messages or [{"role": "user", "content": request.prompt}], model)
        if production_response is None:
            all_reasons.append("production:not_configured")
        else:
            all_reasons.append("production:completed")

    _audit(decision)
    return PipelineResult(
        correlation_id=correlation_id,
        decision=decision,
        risk_score=detection.risk_score,
        attack_types=detection.attack_types,
        route=detection.route,
        reasons=all_reasons,
        production_response=production_response,
    )


async def _call_production(messages: list[dict], model: str | None) -> dict | None:
    settings = get_settings()
    if not settings.production_llm_url:
        return None
    payload = {"model": model or "gpt-4o-mini", "messages": messages}
    headers = {}
    if settings.production_llm_api_key:
        headers["Authorization"] = f"Bearer {settings.production_llm_api_key}"
    async with httpx.AsyncClient(timeout=60.0) as client:
        resp = await client.post(
            f"{settings.production_llm_url.rstrip('/')}/v1/chat/completions",
            json=payload,
            headers=headers,
        )
        resp.raise_for_status()
        return resp.json()
