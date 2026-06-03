from datetime import datetime, timezone
from enum import Enum
from typing import Any
from uuid import uuid4

from pydantic import BaseModel, Field


class Route(str, Enum):
    ALLOW = "allow"
    SANDBOX = "sandbox"
    BLOCK = "block"


class Decision(str, Enum):
    ALLOWED = "allowed"
    BLOCKED = "blocked"
    SANDBOXED = "sandboxed"


class PromptRequest(BaseModel):
    prompt: str
    user_id: str
    session_id: str | None = None
    metadata: dict[str, Any] = Field(default_factory=dict)
    tools: list[dict[str, Any]] | None = None
    group_ids: list[str] | None = None


class DetectionResult(BaseModel):
    risk_score: int
    attack_types: list[str] = Field(default_factory=list)
    matched_patterns: list[str] = Field(default_factory=list)
    route: Route
    obfuscation_detected: bool = False
    reasons: list[str] = Field(default_factory=list)


class SandboxResult(BaseModel):
    predicted_leak: bool = False
    permission_violation: bool = False
    decision: Decision
    denied_resources: list[str] = Field(default_factory=list)
    reasons: list[str] = Field(default_factory=list)


class AuditEvent(BaseModel):
    timestamp: datetime = Field(default_factory=lambda: datetime.now(timezone.utc))
    correlation_id: str = Field(default_factory=lambda: str(uuid4()))
    user_id: str
    group_ids: list[str] = Field(default_factory=list)
    risk_score: int | None = None
    attack_type: str | None = None
    decision: Decision
    agent_path: list[str] = Field(default_factory=list)
    reasons: list[str] = Field(default_factory=list)
    latency_ms: int | None = None
    prompt_hash: str | None = None


class PipelineResult(BaseModel):
    correlation_id: str
    decision: Decision
    risk_score: int | None = None
    attack_types: list[str] = Field(default_factory=list)
    route: Route | None = None
    reasons: list[str] = Field(default_factory=list)
    production_response: dict[str, Any] | None = None
    blocked_message: str | None = None
