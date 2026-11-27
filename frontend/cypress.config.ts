import registerCodeCoverageTasks from '@cypress/code-coverage/task';
import {defineConfig} from "cypress";

export default defineConfig({
  projectId: 'xf5z4s',
  expose: {
    apiBaseUrl: (process.env.VITE_API_BASE_URL?.trim() || "http://localhost:8080/api").replace(/\/+$/, ""),
    coverage: process.env.VITE_COVERAGE === 'true',
    codeCoverage: {expectFrontendCoverageOnly: true},
  },
  e2e: {
    baseUrl: "http://localhost:5173",

    setupNodeEvents(on, config) {
      if (config.expose.coverage) {
        registerCodeCoverageTasks(on, config);
      }
      return config;
    },
  },
});
