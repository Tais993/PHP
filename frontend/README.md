# Frontend

React/TypeScript with Vite. Builds independently from Spring Boot into static files in `dist/`.

## Configuration

`src/config.ts` is the single place that reads frontend environment variables. API consumers use its `apiUrl()` helper. Copy `.env.example` to `.env.local` for local overrides, or set variables in the frontend CI build environment.

| Variable | Meaning | Default |
| --- | --- | --- |
| `VITE_API_BASE_URL` | Public backend base URL, including `/api` | `http://localhost:8080/api` in development; `/api` in production |

For a separately hosted frontend, set `VITE_API_BASE_URL=https://your-backend.example/api` **before building**. Vite embeds these values in the build: changing hosting runtime settings does not change an existing build. All `VITE_` values are public; keep credentials in the backend environment.

On the backend, set `FRONTEND_ORIGINS=https://your-frontend.example` (the exact frontend origin, without a path or trailing slash). Multiple origins are comma-separated. The local Spring profile and Docker Compose allow `http://localhost:5173` and `http://127.0.0.1:5173` by default.

## Development

Requires Node.js 24. From this directory:

```sh
npm ci
npm run dev
```

Start the backend separately with `./gradlew :web:bootRun` from `backend/` (`gradlew.bat` on Windows), or `docker compose up -d --build` from the repository root. The frontend runs on `http://localhost:5173` and calls the backend directly. If port 5173 is in use, Vite stops so it cannot silently switch to an origin the backend does not allow.

## Build and hosting

```sh
npm run lint
npm run build
```

`npm run preview` is for checking builds locally. The deployed frontend is served
as static assets through Cloudflare Workers.

### Cloudflare setup

Connect the GitHub repository in Cloudflare and use these settings:

| Field | Value |
| --- | --- |
| Project name | `php-frontend` |
| Build command | `npm run build` |
| Deploy command | `npx wrangler deploy` |
| Preview command | `npx wrangler preview` |
| Advanced settings → Path | `frontend` |

Select the branch containing the frontend changes. Set these variables in the
Cloudflare **build environment** before deploying:

| Variable | Value |
| --- | --- |
| `NODE_VERSION` | `24` |
| `VITE_API_BASE_URL` | `https://your-backend.example/api` |

Replace the example API URL with the backend's public HTTPS URL, including `/api`.
Cloudflare builds the frontend itself; variables in GitHub Actions do not
configure Cloudflare's build environment. Changing the API URL requires a rebuild.

`wrangler.jsonc` configures the project name, the `dist/` asset directory, and the
React routing fallback. It allows refreshing routes such as `/boards/1`.
Keep the Cloudflare project name and the `name` in this file identical.

After deployment, set `FRONTEND_ORIGINS` in the backend hosting environment to the
assigned frontend origin, for example:

```dotenv
FRONTEND_ORIGINS=https://php-frontend.your-account.workers.dev
```

Use the actual frontend URL without a path or trailing slash. Multiple origins
are comma-separated. Preview URLs need their own allowed origins if used to call
the backend.

The backend remains a separate Docker deployment on Heroku or Azure Container
Apps. The Docker publish/deploy workflow ships the API; the frontend build
workflow checks the frontend without deploying it. Cloudflare handles frontend
deployments through its Git integration.
