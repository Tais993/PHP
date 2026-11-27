function apiUrl(path) {
  return `${Cypress.expose('apiBaseUrl')}${path}`;
}

export function createBoard() {
  return cy.request('POST', apiUrl('/boards'), {
    title: `Task test board ${globalThis.crypto.randomUUID()}`,
    description: '',
  }).its('body');
}

export function getDefaultView(boardId) {
  return cy.request(apiUrl(`/boards/${boardId}/views`)).its('body').then(views => {
    const view = views.find(({name}) => name === 'Default');
    expect(view, 'default board view').to.exist;
    return view;
  });
}

export function createBoardView(boardId, {name, position}) {
  return cy.request('POST', apiUrl('/views'), {
    boardId,
    name,
    type: 'BOARD',
    groupBy: 'STATUS',
    sortBy: 'CREATED_AT',
    sortDirection: 'DESC',
    position,
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

export function getViewStatusIds(viewId) {
  return cy.request(apiUrl(`/views/${viewId}/tasks`)).its('body.groups').then(groups => {
    return Object.fromEntries(Object.values(groups)
      .filter(({id}) => id !== null)
      .map(({name, id}) => [name, id]));
  });
}

export function replaceViewFilterRules(viewId, filterRules) {
  return cy.request('PUT', apiUrl(`/views/${viewId}/filter-rules`), {filterRules});
}

export function setTaskDueDate(taskId, dueDate) {
  return cy.request('PATCH', apiUrl(`/tasks/${taskId}`), {dueDate});
}

export function deleteBoardWithTasks(boardId) {
  const boardUrl = apiUrl(`/boards/${boardId}`);

  return cy.request(`${boardUrl}/tasks`).its('body').each(({ id }) => {
    return cy.request('DELETE', apiUrl(`/tasks/${id}`)).its('status').should('eq', 204);
  }).then(() => {
    return cy.request('DELETE', boardUrl).its('status').should('eq', 204);
  });
}
