from contextlib import asynccontextmanager

from fastapi import Depends, FastAPI, Header, HTTPException
from pydantic import BaseModel, Field
from sqlalchemy.orm import Session

from airwall_core.database import get_db, init_db
from airwall_core.models import Decision, PromptRequest
from airwall_core.seed import run_seed
from airwall_core.config import get_settings
from airwall_gateway.orchestrator import run_pipeline


@asynccontextmanager
async def lifespan(app: FastAPI):
    try:
        init_db()
        run_seed()
    except Exception as exc:
        print(f"Warning: DB init failed ({exc}). Start postgres via docker compose.")
    yield


app = FastAPI(title="Airwall", version="0.1.0", lifespan=lifespan)


def verify_api_key(x_api_key: str | None = Header(default=None, alias="X-API-Key")) -> None:
    settings = get_settings()
    if x_api_key != settings.airwall_api_key:
        raise HTTPException(status_code=401, detail="Invalid API key")


class GuardRequest(BaseModel):
    prompt: str
    user_id: str
    session_id: str | None = None


class GuardResponse(BaseModel):
    correlation_id: str
    decision: str
    risk_score: int | None = None
    attack_types: list[str] = Field(default_factory=list)
    reasons: list[str] = Field(default_factory=list)
    blocked_message: str | None = None


class ChatMessage(BaseModel):
    role: str
    content: str


class ChatCompletionRequest(BaseModel):
    model: str = "gpt-4o-mini"
    messages: list[ChatMessage]
    user_id: str


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/v1/guard", response_model=GuardResponse, dependencies=[Depends(verify_api_key)])
async def guard(
    body: GuardRequest,
    x_airwall_user_id: str | None = Header(default=None, alias="X-Airwall-User-Id"),
    db: Session = Depends(get_db),
):
    user_id = x_airwall_user_id or body.user_id
    req = PromptRequest(prompt=body.prompt, user_id=user_id, session_id=body.session_id)
    result = await run_pipeline(db, req, call_production=False)
    return GuardResponse(
        correlation_id=result.correlation_id,
        decision=result.decision.value,
        risk_score=result.risk_score,
        attack_types=result.attack_types,
        reasons=result.reasons,
        blocked_message=result.blocked_message,
    )


@app.post("/v1/chat/completions", dependencies=[Depends(verify_api_key)])
async def chat_completions(
    body: ChatCompletionRequest,
    x_airwall_user_id: str | None = Header(default=None, alias="X-Airwall-User-Id"),
    db: Session = Depends(get_db),
):
    user_id = x_airwall_user_id or body.user_id
    prompt = ""
    for msg in reversed(body.messages):
        if msg.role == "user":
            prompt = msg.content
            break
    req = PromptRequest(prompt=prompt, user_id=user_id)
    result = await run_pipeline(
        db,
        req,
        call_production=True,
        messages=[m.model_dump() for m in body.messages],
        model=body.model,
    )
    if result.decision == Decision.BLOCKED:
        raise HTTPException(
            status_code=403,
            detail={
                "error": "blocked_by_airwall",
                "message": result.blocked_message,
                "correlation_id": result.correlation_id,
                "risk_score": result.risk_score,
                "reasons": result.reasons,
            },
        )
    if result.production_response:
        return result.production_response
    return {
        "id": f"chatcmpl-{result.correlation_id[:8]}",
        "object": "chat.completion",
        "choices": [
            {
                "index": 0,
                "message": {
                    "role": "assistant",
                    "content": (
                        "[Airwall] Production LLM not configured. Set PRODUCTION_LLM_URL. "
                        "Request passed security checks."
                    ),
                },
                "finish_reason": "stop",
            }
        ],
        "airwall": {
            "correlation_id": result.correlation_id,
            "risk_score": result.risk_score,
            "reasons": result.reasons,
        },
    }


def run():
    import uvicorn

    uvicorn.run("airwall_gateway.main:app", host="0.0.0.0", port=8080, reload=True)


if __name__ == "__main__":
    run()
