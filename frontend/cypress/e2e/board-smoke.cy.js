describe('Board smoke test', () => {
  let createdBoardUrl;
  let createdTaskUrl;

  afterEach(() => {
    if (createdTaskUrl) {
      cy.request('DELETE', createdTaskUrl).its('status').should('eq', 204);
      createdTaskUrl = undefined;
    }

    if (createdBoardUrl) {
      cy.request('DELETE', createdBoardUrl).its('status').should('eq', 204);
      createdBoardUrl = undefined;
    }
  });

  it('creates and opens a board, a view, and a task', () => {
    const boardTitle = `Smoke test board ${globalThis.crypto.randomUUID()}`;
    const viewName = 'Sprint board';
    const taskTitle = 'First task';
    let viewPath;

    cy.intercept({ method: 'POST', pathname: /\/boards$/ }).as('createBoard');
    cy.intercept({ method: 'POST', pathname: /\/views$/ }).as('createView');
    cy.intercept({ method: 'POST', pathname: /\/tasks$/ }).as('createTask');
    cy.visit('/');

    cy.get('[data-cy="add-board"]').should('be.enabled').click();
    cy.get('[data-cy="board-title"]').type(boardTitle);
    cy.get('[data-cy="create-board"]').click();

    cy.wait('@createBoard').then(({ request, response }) => {
      expect(response.statusCode).to.equal(201);
      const boardId = response.body.id;
      createdBoardUrl = `${request.url}/${boardId}`;
      const boardPath = `/boards/${boardId}`;

      cy.contains('[data-cy="board-link"]', boardTitle).click();
      cy.location('pathname').should('eq', boardPath);
    });

    cy.get('[data-cy="add-view"]').should('be.enabled').click();
    cy.get('[data-cy="view-name"]').type(viewName);
    cy.get('[data-cy="create-view"]').click();

    cy.wait('@createView').then(({ response }) => {
      const { boardId, id: viewId } = response.body;
      viewPath = `/boards/${boardId}/views/${viewId}`;
      cy.contains('[data-cy="view-link"]', viewName).click();
      cy.location('pathname').should('eq', viewPath);
    });
    cy.get('[data-cy="view-heading"]').should('have.text', viewName);

    cy.get('[data-cy="add-task"]').should('be.enabled').click();
    cy.get('[data-cy="task-title"]').type(taskTitle);
    cy.get('[data-cy="create-task"]').click();

    cy.wait('@createTask').then(({ request, response }) => {
      expect(response.statusCode).to.equal(201);
      const taskId = response.body.id;
      createdTaskUrl = `${request.url}/${taskId}`;

      cy.contains('[data-cy="task-card"]', taskTitle).click();
      cy.location('pathname').should('eq', `${viewPath}/tasks/${taskId}`);
    });
    cy.get('[data-cy="task-detail-title"]').should('have.text', taskTitle).and('be.visible');
  });
});
