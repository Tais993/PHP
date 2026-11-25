import {createBoard, createBoardView, createTask, deleteBoardWithTasks} from '../support/board-fixtures.js';

function addTaskThroughForm(title) {
  cy.get('[data-cy="add-task"]').should('be.enabled').click();
  cy.get('[data-cy="task-title"]').type(title);
  cy.get('[data-cy="create-task"]').click();
  cy.contains('[data-cy="task-card"]', title).should('be.visible');
}

describe('Tasks', () => {
  let boardId;
  let viewPath;

  beforeEach(() => {
    boardId = undefined;
    return createBoard().then(({ id }) => {
      boardId = id;
      return createBoardView(boardId);
    }).then(({ id: viewId }) => {
      viewPath = `/boards/${boardId}/views/${viewId}`;
    });
  });

  afterEach(() => {
    if (boardId !== undefined) {
      return deleteBoardWithTasks(boardId);
    }
  });

  it('creates two tasks in a view', () => {
    cy.visit(viewPath);

    addTaskThroughForm('Plan the sprint');
    addTaskThroughForm('Review the backlog');

    cy.get('[data-cy="task-card"]').should('have.length', 2);
  });

  it('renames one task without changing the other', () => {
    const originalTitle = 'Draft the release notes';
    const updatedTitle = 'Publish the release notes';
    const otherTitle = 'Review the changelog';

    createTask(boardId, originalTitle);
    createTask(boardId, otherTitle);
    cy.visit(viewPath);

    cy.contains('[data-cy="task-card"]', originalTitle).click();
    cy.get('[data-cy="edit-task-title"]').click();
    cy.get('[data-cy="edit-task-title-input"]').clear();
    cy.get('[data-cy="edit-task-title-input"]').type(updatedTitle);
    cy.get('[data-cy="save-task-title"]').click();
    cy.get('[data-cy="task-detail-title"]').should('have.text', updatedTitle);

    cy.reload();
    cy.get('[data-cy="task-detail-title"]').should('have.text', updatedTitle);
    cy.get('[data-cy="close-task-details"]').click();
    cy.contains('[data-cy="task-card"]', updatedTitle).should('be.visible');
    cy.contains('[data-cy="task-card"]', otherTitle).should('be.visible');
    cy.get('[data-cy="task-card"]').should('have.length', 2);
  });
});
