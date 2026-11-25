import {defineConfig} from "cypress";

export default defineConfig({
  expose: {
    apiBaseUrl: (process.env.VITE_API_BASE_URL?.trim() || "http://localhost:8080/api").replace(/\/+$/, ""),
  },
  e2e: {
    baseUrl: "http://localhost:5173",
  },
});
