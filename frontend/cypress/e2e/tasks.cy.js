import {
  createBoard,
  createTask,
  deleteBoardWithTasks,
  getDefaultView,
  getViewStatusIds,
  replaceViewFilterRules,
} from '../support/board-fixtures.js';

function addTaskThroughForm(title) {
  cy.get('[data-cy="add-task"]').should('be.enabled').click();
  cy.get('[data-cy="task-title"]').type(title);
  cy.get('[data-cy="create-task"]').click();
  cy.contains('[data-cy="task-card"]', title).should('be.visible');
}

const taskCard = '[data-cy="task-card"]';
const taskDetails = '[role="dialog"]';

function column(name) {
  return cy.get(`section[aria-label="${name}"]`);
}

function statusSelect() {
  return cy.get('aside[aria-label="Task properties"] select');
}

function dueDateInput() {
  return cy.get('input[type="date"]');
}

function closeTaskDetails() {
  cy.get('[data-cy="close-task-details"]').click();
  cy.get('[data-cy="task-detail-title"]').should('not.exist');
}

describe('Tasks', () => {
  let boardId;
  let viewId;
  let viewPath;

  beforeEach(() => {
    boardId = undefined;
    return createBoard().then(({ id }) => {
      boardId = id;
      return getDefaultView(boardId);
    }).then(({ id }) => {
      viewId = id;
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

  it('edits and clears a task description', () => {
    const title = 'Write the sprint summary';
    const originalDescription = 'Summarize the completed work.';
    const updatedDescription = 'Include the remaining work and open questions.';

    createTask(boardId, title, {description: originalDescription});
    cy.visit(viewPath);
    cy.contains(taskCard, title).click();

    cy.get(taskDetails).contains(originalDescription).should('be.visible');
    cy.get('button[aria-label="Edit description"]').click();
    cy.get('[data-cy="edit-task-description-input"]').should('have.value', originalDescription).clear();
    cy.get('[data-cy="edit-task-description-input"]').type(updatedDescription);
    cy.get('[data-cy="save-task-description"]').click();
    cy.get(taskDetails).contains(updatedDescription).should('be.visible');

    cy.reload();
    cy.get(taskDetails).contains(updatedDescription).should('be.visible');
    cy.get('button[aria-label="Edit description"]').click();
    cy.get('[data-cy="edit-task-description-input"]').clear();
    cy.get('[data-cy="save-task-description"]').click();
    cy.get(taskDetails).contains('No description provided.').should('be.visible');

    cy.reload();
    cy.get(taskDetails).contains('No description provided.').should('be.visible');
    closeTaskDetails();
    cy.contains(taskCard, title).should('contain.text', 'No description provided.');
  });

  it('sets, changes, and clears a task due date', () => {
    const title = 'Schedule the release';
    const firstDate = '2027-05-10';
    const revisedDate = '2027-06-15';

    createTask(boardId, title);
    cy.visit(viewPath);
    cy.contains(taskCard, title).click();

    dueDateInput().should('have.value', '').type(firstDate);
    cy.contains('button', 'Clear due date').should('be.enabled');
    cy.reload();
    dueDateInput().should('have.value', firstDate);

    dueDateInput().type(revisedDate);
    dueDateInput().should('have.value', revisedDate).and('be.enabled');
    cy.reload();
    dueDateInput().should('have.value', revisedDate);

    cy.contains('button', 'Clear due date').click();
    dueDateInput().should('have.value', '').and('be.enabled');
    cy.reload();
    dueDateInput().should('have.value', '');
    cy.contains('button', 'Clear due date').should('not.exist');
    closeTaskDetails();
    cy.contains(taskCard, title).should('contain.text', 'Due: Not set');
  });

  describe('Status columns', () => {
    let statusIds;

    beforeEach(() => {
      cy.viewport(1280, 800);
      return getViewStatusIds(viewId).then(ids => {
        statusIds = ids;
      });
    });

    it('creates a task in the selected column', () => {
      const title = 'Verify the deployment';

      cy.visit(viewPath);
      column('Done').contains('button', '+ Add task').click();
      cy.get(taskDetails).within(() => {
        cy.get('input[placeholder="What needs doing?"]').type(title);
        cy.contains('button', 'Create task').click();
      });

      column('Done').contains(taskCard, title).should('be.visible');
      cy.reload();
      column('Done').contains(taskCard, title).should('be.visible');
    });

    it('moves a task by dragging it to a different column', () => {
      const title = 'Review the pull request';

      createTask(boardId, title, {statusId: statusIds['To Do']});
      cy.visit(viewPath);

      cy.window().then(win => {
        const dragEvent = {eventConstructor: 'DragEvent', dataTransfer: new win.DataTransfer()};
        column('To Do').contains(taskCard, title).trigger('dragstart', dragEvent);
        column('Done').trigger('dragenter', dragEvent);
        column('Done').trigger('dragover', dragEvent);
        column('Done').contains(taskCard, title).should('be.visible');
        column('Done').trigger('drop', dragEvent);
      });

      column('Done').contains(taskCard, title).should('contain.text', 'Done');
      column('To Do').contains(taskCard, title).should('not.exist');
      cy.reload();
      column('Done').contains(taskCard, title).should('be.visible');
      column('To Do').contains(taskCard, title).should('not.exist');
    });

    it('filters out a task after its status changes in a saved view', () => {
      const title = 'Publish the release';
      const remainingTitle = 'Update the release checklist';

      createTask(boardId, title, {statusId: statusIds['To Do']});
      createTask(boardId, remainingTitle, {statusId: statusIds['To Do']});
      replaceViewFilterRules(viewId, [{field: 'STATUS', operator: 'IN', values: [String(statusIds['To Do'])]}]);
      cy.visit(viewPath);

      column('To Do').contains(taskCard, title).click();
      statusSelect().select('Done');
      statusSelect().should('have.value', String(statusIds.Done)).and('be.enabled');
      closeTaskDetails();
      cy.contains(taskCard, remainingTitle).should('be.visible');
      cy.contains(taskCard, title).should('not.exist');

      cy.reload();
      cy.contains(taskCard, remainingTitle).should('be.visible');
      cy.contains(taskCard, title).should('not.exist');
    });

    it('changes and clears a task status from its details', () => {
      const title = 'Prepare the release checklist';

      createTask(boardId, title, {statusId: statusIds['To Do']});
      cy.visit(viewPath);
      column('To Do').contains(taskCard, title).click();

      statusSelect().should('have.value', String(statusIds['To Do'])).select('Done');
      statusSelect().should('have.value', String(statusIds.Done)).and('be.enabled');
      closeTaskDetails();
      column('Done').contains(taskCard, title).should('be.visible');

      cy.reload();
      column('Done').contains(taskCard, title).click();
      statusSelect().should('have.value', String(statusIds.Done)).select('Unassigned');
      statusSelect().should('have.value', 'unassigned').and('be.enabled');
      closeTaskDetails();
      column('Unassigned').contains(taskCard, title).should('be.visible');

      cy.reload();
      column('Unassigned').contains(taskCard, title).should('be.visible');
    });
  });
});
