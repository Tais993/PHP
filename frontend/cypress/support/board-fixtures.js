function apiUrl(path) {
  return `${Cypress.expose('apiBaseUrl')}${path}`;
}

export function createBoard() {
  return cy.request('POST', apiUrl('/boards'), {
    title: `Task test board ${globalThis.crypto.randomUUID()}`,
    description: '',
  }).its('body');
}

export function createBoardView(boardId) {
  return cy.request('POST', apiUrl('/views'), {
    boardId,
    name: 'Task test view',
    type: 'BOARD',
    groupBy: 'STATUS',
    sortBy: 'CREATED_AT',
    sortDirection: 'DESC',
    position: 0,
  }).its('body');
}

export function createTask(boardId, title, {description = '', statusId = null} = {}) {
  return cy.request('POST', apiUrl('/tasks'), {
    boardId,
    title,
    description,
    statusId,
  }).its('body');
}

export function replaceViewFilterRules(viewId, filterRules) {
  return cy.request('PUT', apiUrl(`/views/${viewId}/filter-rules`), {filterRules});
}

export function deleteBoardWithTasks(boardId, {hasStatusFixtures = false} = {}) {
  const boardUrl = apiUrl(`/boards/${boardId}`);

  return cy.request(`${boardUrl}/tasks`).its('body').each(({ id }) => {
    return cy.request('DELETE', apiUrl(`/tasks/${id}`)).its('status').should('eq', 204);
  }).then(() => {
    if (hasStatusFixtures) {
      return cy.task('deleteBoardStatuses', boardId);
    }
  }).then(() => {
    return cy.request('DELETE', boardUrl).its('status').should('eq', 204);
  });
}
