from airwall_core.models import DetectionResult, PromptRequest
from airwall_core.patterns import analyze_prompt


def analyze(request: PromptRequest) -> DetectionResult:
    return analyze_prompt(request.prompt)
