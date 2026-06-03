import hashlib
import json

from sqlalchemy.orm import Session

from airwall_core.database import AuditLog
from airwall_core.models import AuditEvent


def hash_prompt(prompt: str) -> str:
    return hashlib.sha256(prompt.encode("utf-8")).hexdigest()


def record_audit(session: Session, event: AuditEvent, prompt: str | None = None, store_full: bool = False) -> None:
    prompt_hash = event.prompt_hash
    if prompt and not prompt_hash:
        prompt_hash = hash_prompt(prompt)

    log = AuditLog(
        correlation_id=event.correlation_id,
        user_id=event.user_id,
        group_ids_json=json.dumps(event.group_ids),
        risk_score=event.risk_score,
        attack_type=event.attack_type,
        decision=event.decision.value,
        reasons_json=json.dumps(event.reasons),
        prompt_hash=prompt_hash,
        latency_ms=event.latency_ms,
    )
    session.add(log)
    session.flush()
