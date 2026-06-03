# Push to GitHub

Local git is ready: branch `main`, initial commit created.

## One-time: log in to GitHub

```powershell
gh auth login --hostname github.com --git-protocol https --web
```

Follow the browser prompt at https://github.com/login/device

## Create private repo and push

```powershell
cd C:\Users\benyamin\airwall
gh repo create airwall --private --source=. --remote=origin --description "AI prompt security gateway"
git push -u origin main
```

If the name `airwall` is taken:

```powershell
gh repo create airwall-security --private --source=. --remote=origin
git push -u origin main
```
