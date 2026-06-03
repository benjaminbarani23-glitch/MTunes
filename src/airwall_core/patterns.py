import base64
import re
import unicodedata
from dataclasses import dataclass
from pathlib import Path

import yaml

from airwall_core.config import PATTERNS_SEED
from airwall_core.models import DetectionResult, Route
from airwall_core.config import get_settings


@dataclass
class PatternRule:
    id: str
    category: str
    pattern: re.Pattern[str]
    severity: int


HEURISTICS: list[tuple[str, int, str]] = [
    (r"ignore\s+(all\s+)?(previous|prior)\s+instructions", 85, "prompt_injection"),
    (r"reveal\s+(your\s+)?system\s+prompt", 90, "extraction"),
    (r"act\s+as\s+(an?\s+)?admin(istrator)?", 75, "jailbreak"),
    (r"disregard\s+(safety|policy)", 80, "jailbreak"),
    (r"print\s+(the\s+)?(hidden|secret)\s+instructions", 88, "extraction"),
    (r"<\s*tool_call", 70, "tool_abuse"),
    (r"execute\s+shell\s+command", 72, "tool_abuse"),
]

RESOURCE_KEYWORDS: dict[str, list[str]] = {
    "payroll": [r"salary|salaries|payroll|compensation|wage"],
    "employee_directory": [r"employee\s+list|all\s+employees|staff\s+directory"],
    "internal_docs": [r"internal\s+doc|confidential\s+memo|company\s+secrets"],
    "api_secrets": [r"api\s+key|secret\s+key|credentials|token"],
    "customer_pii": [r"customer\s+(ssn|address|credit\s+card)"],
    "public_faq": [r"faq|help\s+center|public\s+info"],
    "own_orders": [r"my\s+order|order\s+status"],
}


def load_seed_patterns(path: Path | None = None) -> list[PatternRule]:
    path = path or PATTERNS_SEED
    if not path.exists():
        return _default_patterns()
    data = yaml.safe_load(path.read_text(encoding="utf-8")) or {}
    rules: list[PatternRule] = []
    for item in data.get("patterns", []):
        rules.append(
            PatternRule(
                id=item["id"],
                category=item["category"],
                pattern=re.compile(item["pattern"], re.IGNORECASE),
                severity=int(item.get("severity", 50)),
            )
        )
    return rules


def _default_patterns() -> list[PatternRule]:
    return [
        PatternRule("inj1", "prompt_injection", re.compile(r"ignore previous instructions", re.I), 85),
        PatternRule("ext1", "extraction", re.compile(r"system prompt", re.I), 80),
    ]


def normalize_text(text: str) -> tuple[str, bool]:
    normalized = unicodedata.normalize("NFKC", text)
    obfuscated = False
    zero_width = ["\u200b", "\u200c", "\u200d", "\ufeff"]
    for zw in zero_width:
        if zw in normalized:
            obfuscated = True
            normalized = normalized.replace(zw, "")
    return normalized.lower(), obfuscated


def try_decode_layers(text: str) -> tuple[str, bool]:
    combined = text
    found = False
    for decoder in (_try_base64, _try_hex):
        decoded, ok = decoder(text.strip())
        if ok:
            combined += "\n" + decoded
            found = True
    return combined, found


def _try_base64(s: str) -> tuple[str, bool]:
    try:
        if len(s) < 16 or len(s) % 4 != 0:
            return "", False
        raw = base64.b64decode(s, validate=True)
        return raw.decode("utf-8", errors="ignore"), True
    except Exception:
        return "", False


def _try_hex(s: str) -> tuple[str, bool]:
    try:
        if len(s) < 16 or len(s) % 2 != 0:
            return "", False
        return bytes.fromhex(s).decode("utf-8", errors="ignore"), True
    except Exception:
        return "", False


def extract_requested_resources(text: str) -> list[str]:
    requested: list[str] = []
    for resource, patterns in RESOURCE_KEYWORDS.items():
        for pat in patterns:
            if re.search(pat, text, re.IGNORECASE):
                requested.append(resource)
                break
    return requested


def score_to_route(score: int) -> Route:
    settings = get_settings()
    if score <= settings.risk_allow_max:
        return Route.ALLOW
    if score <= settings.risk_sandbox_max:
        return Route.SANDBOX
    return Route.BLOCK


def analyze_prompt(prompt: str, rules: list[PatternRule] | None = None) -> DetectionResult:
    rules = rules or load_seed_patterns()
    normalized, obfuscated = normalize_text(prompt)
    inspection, decoded = try_decode_layers(prompt)
    full_text = f"{normalized}\n{inspection.lower()}"

    matched: list[str] = []
    attack_types: set[str] = set()
    reasons: list[str] = []
    max_severity = 0

    for rule in rules:
        if rule.pattern.search(full_text):
            matched.append(rule.id)
            attack_types.add(rule.category)
            max_severity = max(max_severity, rule.severity)
            reasons.append(f"pattern:{rule.id}")

    for pattern, severity, category in HEURISTICS:
        if re.search(pattern, full_text, re.IGNORECASE):
            attack_types.add(category)
            max_severity = max(max_severity, severity)
            reasons.append(f"heuristic:{category}")

    if obfuscated:
        max_severity = max(max_severity, 55)
        reasons.append("obfuscation:zero_width")
    if decoded:
        max_severity = max(max_severity, 50)
        reasons.append("obfuscation:encoding")

    risk_score = min(100, max_severity)
    route = score_to_route(risk_score)

    return DetectionResult(
        risk_score=risk_score,
        attack_types=sorted(attack_types),
        matched_patterns=matched,
        route=route,
        obfuscation_detected=obfuscated or decoded,
        reasons=reasons,
    )
