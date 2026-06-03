import httpx


class AirwallClient:
    def __init__(self, base_url: str, api_key: str, timeout: float = 30.0):
        self.base_url = base_url.rstrip("/")
        self.api_key = api_key
        self.timeout = timeout

    def _headers(self, user_id: str | None = None) -> dict[str, str]:
        headers = {"X-API-Key": self.api_key}
        if user_id:
            headers["X-Airwall-User-Id"] = user_id
        return headers

    def guard(self, prompt: str, user_id: str, session_id: str | None = None) -> dict:
        with httpx.Client(timeout=self.timeout) as client:
            resp = client.post(
                f"{self.base_url}/v1/guard",
                json={"prompt": prompt, "user_id": user_id, "session_id": session_id},
                headers=self._headers(user_id),
            )
            resp.raise_for_status()
            return resp.json()

    def chat_completions_create(
        self,
        *,
        model: str,
        messages: list[dict],
        user_id: str,
    ) -> dict:
        with httpx.Client(timeout=self.timeout) as client:
            resp = client.post(
                f"{self.base_url}/v1/chat/completions",
                json={"model": model, "messages": messages, "user_id": user_id},
                headers=self._headers(user_id),
            )
            if resp.status_code == 403:
                detail = resp.json().get("detail", {})
                raise PermissionError(detail.get("message", "Blocked by Airwall"))
            resp.raise_for_status()
            return resp.json()

    @property
    def chat(self):
        return _ChatNamespace(self)


class _ChatNamespace:
    def __init__(self, client: AirwallClient):
        self._client = client

    @property
    def completions(self):
        return _CompletionsNamespace(self._client)


class _CompletionsNamespace:
    def __init__(self, client: AirwallClient):
        self._client = client

    def create(self, **kwargs):
        return self._client.chat_completions_create(
            model=kwargs["model"],
            messages=kwargs["messages"],
            user_id=kwargs["user_id"],
        )
