from sqlalchemy.orm import Session

from airwall_core.models import Decision, DetectionResult, PromptRequest, SandboxResult
from airwall_core.patterns import extract_requested_resources
from airwall_core.rbac import check_resources_allowed, get_group_names


def evaluate(
    session: Session,
    request: PromptRequest,
    detection: DetectionResult,
) -> SandboxResult:
    requested = extract_requested_resources(request.prompt)
    allowed, denied = check_resources_allowed(session, request.user_id, requested)

    reasons: list[str] = []
    if denied:
        reasons.append(f"permission_denied:{','.join(denied)}")
        return SandboxResult(
            predicted_leak=True,
            permission_violation=True,
            decision=Decision.BLOCKED,
            denied_resources=denied,
            reasons=reasons,
        )

    groups = get_group_names(session, request.user_id)
    if not groups and requested:
        reasons.append("permission_denied:no_groups_assigned")
        return SandboxResult(
            permission_violation=True,
            decision=Decision.BLOCKED,
            denied_resources=requested,
            reasons=reasons,
        )

    return SandboxResult(
        decision=Decision.ALLOWED,
        reasons=["sandbox:permission_ok"],
    )
