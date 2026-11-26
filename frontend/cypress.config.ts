import {execFileSync} from 'node:child_process';
import {fileURLToPath} from 'node:url';
import {defineConfig} from "cypress";

const composeFile = fileURLToPath(new URL('../compose.dev.yaml', import.meta.url));

// This too, next priority after E2E tests are resolved
function runStatusFixture(boardId: number, sql: string) {
  if (!Number.isSafeInteger(boardId) || boardId <= 0) {
    throw new Error('Expected a positive test board ID');
  }

  return execFileSync('docker', [
    'compose', '-f', composeFile, 'exec', '-T', 'postgres',
    'psql', '-X', '-qAt', '-v', 'ON_ERROR_STOP=1', '-v', `board_id=${boardId}`,
    '-U', 'homepage', '-d', 'homepage',
  ], {input: sql, encoding: 'utf8'}).trim();
}

export default defineConfig({
  expose: {
    apiBaseUrl: (process.env.VITE_API_BASE_URL?.trim() || "http://localhost:8080/api").replace(/\/+$/, ""),
  },
  e2e: {
    baseUrl: "http://localhost:5173",

    // This really has to be resolved, for now statuses are creating like this.
    setupNodeEvents(on) {
      // The application has no endpoint for managing board statuses.
      on('task', {
        seedBoardStatuses(boardId: number) {
          const statuses = runStatusFixture(boardId, `
            WITH statuses AS (
              INSERT INTO board_status (board_id, name, position)
              VALUES (:board_id, 'To do', 0), (:board_id, 'Done', 1)
              RETURNING id, name
            )
            SELECT json_object_agg(name, id) FROM statuses;
          `);
          return JSON.parse(statuses);
        },
        deleteBoardStatuses(boardId: number) {
          runStatusFixture(boardId, 'DELETE FROM board_status WHERE board_id = :board_id;');
          return null;
        },
      });
    },
  },
});
