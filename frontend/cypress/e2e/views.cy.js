import {
  createBoard,
  createBoardView,
  createTask,
  deleteBoardWithTasks,
  getDefaultView,
  getViewStatusIds,
  replaceViewFilterRules,
  setTaskDueDate,
} from '../support/board-fixtures.js';

const viewLink = '[data-cy="view-link"]';
const taskCard = '[data-cy="task-card"]';

function field(label) {
  return cy.contains('label', new RegExp(`^${label}(\\s*\\*)?$`)).invoke('attr', 'for')
    .then(id => cy.get(`[id="${id}"]`));
}

function openView(name) {
  cy.contains(viewLink, new RegExp(`^${name}$`)).click();
  cy.get('[data-cy="view-heading"]').should('have.text', name);
}

function openViewOptions(name) {
  cy.get(`button[aria-label="Options for view ${name}"]`).click();
}

function expectViewOrder(names) {
  cy.get(viewLink).should(links => {
    expect([...links].map(link => link.textContent)).to.deep.equal(names);
  });
}

function expectTaskOrder(titles) {
  cy.get(taskCard).should(cards => {
    expect(cards).to.have.length(titles.length);
    titles.forEach((title, index) => {
      expect(cards[index].textContent).to.include(title);
    });
  });
}

function addView(name) {
  cy.get('[data-cy="add-view"]').should('be.enabled').click();
  cy.get('[data-cy="view-name"]').type(name);
  cy.get('[data-cy="create-view"]').click();
  cy.contains(viewLink, name).should('be.visible');
}

function openFilters() {
  cy.contains('button:not([data-disabled])', /^Filter( \(\d+\))?$/).click();
}

function applyTitleFilter(value) {
  openFilters();
  cy.contains('button', /^Add rule$/).click();
  field('Field').select('Title');
  field('Title value').type(value);
  cy.get('[aria-label="Filter tasks"]').contains('button', /^Apply$/).click();
}

function clearFilters() {
  openFilters();
  cy.contains('button', /^Clear filters$/).click();
}

function sortByNewestFirst() {
  cy.get('button[aria-label^="Sort by:"]:not([data-disabled])').click();
  field('Sort field').select('Date created');
  field('Direction').select('Descending').closest('form').contains('button', /^Apply$/).click();
  cy.get('button[aria-label="Sort by: Date created, descending"]').should('be.visible');
}

describe('Views', {scrollBehavior: 'nearest'}, () => {
  let boardId;

  function visitBoard() {
    cy.visit(`/boards/${boardId}`);
  }

  beforeEach(() => {
    boardId = undefined;
    cy.viewport(1280, 900);
    return createBoard().then(({id}) => {
      boardId = id;
    });
  });

  afterEach(() => {
    if (boardId !== undefined) {
      return deleteBoardWithTasks(boardId);
    }
  });

  it('creates multiple views on the same board', () => {
    visitBoard();

    addView('Sprint backlog');
    addView('Release planning');

    expectViewOrder(['Default', 'Sprint backlog', 'Release planning']);
    cy.reload();
    expectViewOrder(['Default', 'Sprint backlog', 'Release planning']);
  });

  it('applies and clears a title filter', () => {
    createTask(boardId, 'Audit the deployment');
    createTask(boardId, 'Review the proposal');
    visitBoard();
    expectTaskOrder(['Audit the deployment', 'Review the proposal']);

    applyTitleFilter('deployment');

    expectTaskOrder(['Audit the deployment']);
    cy.reload();
    expectTaskOrder(['Audit the deployment']);

    clearFilters();

    expectTaskOrder(['Audit the deployment', 'Review the proposal']);
    cy.reload();
    expectTaskOrder(['Audit the deployment', 'Review the proposal']);
  });

  it('changes the sort field and direction', () => {
    createTask(boardId, 'Approve the release');
    createTask(boardId, 'Write the release notes');
    createTask(boardId, 'Check the build');
    visitBoard();
    expectTaskOrder(['Approve the release', 'Check the build', 'Write the release notes']);

    sortByNewestFirst();

    expectTaskOrder(['Check the build', 'Write the release notes', 'Approve the release']);
    cy.reload();
    cy.get('button[aria-label="Sort by: Date created, descending"]').should('be.visible');
    expectTaskOrder(['Check the build', 'Write the release notes', 'Approve the release']);
  });

  it('switches between views with different saved filters', () => {
    createTask(boardId, 'Backend deployment');
    createTask(boardId, 'Frontend deployment');
    getDefaultView(boardId).then(({id}) => {
      return replaceViewFilterRules(id, [{field: 'TITLE', operator: 'CONTAINS', values: ['Backend']}]);
    });
    createBoardView(boardId, {name: 'Frontend work', position: 1}).then(({id}) => {
      return replaceViewFilterRules(id, [{field: 'TITLE', operator: 'CONTAINS', values: ['Frontend']}]);
    });
    visitBoard();

    openView('Default');
    expectTaskOrder(['Backend deployment']);
    openView('Frontend work');
    expectTaskOrder(['Frontend deployment']);
    cy.reload();
    cy.get('[data-cy="view-heading"]').should('have.text', 'Frontend work');
    expectTaskOrder(['Frontend deployment']);
    openView('Default');
    expectTaskOrder(['Backend deployment']);
  });

  it('duplicates a configured view with its filter and sorting', () => {
    createTask(boardId, 'Approve the release');
    createTask(boardId, 'Write the release notes');
    createTask(boardId, 'Review the proposal');
    visitBoard();

    applyTitleFilter('release');
    expectTaskOrder(['Approve the release', 'Write the release notes']);
    sortByNewestFirst();
    expectTaskOrder(['Write the release notes', 'Approve the release']);

    openViewOptions('Default');
    cy.contains('button', /^Duplicate view$/).click();
    openView('Copy of Default');

    cy.get('button[aria-label="Sort by: Date created, descending"]').should('be.visible');
    expectTaskOrder(['Write the release notes', 'Approve the release']);
    cy.reload();
    expectTaskOrder(['Write the release notes', 'Approve the release']);
    clearFilters();
    expectTaskOrder(['Review the proposal', 'Write the release notes', 'Approve the release']);
    openView('Default');
    expectTaskOrder(['Write the release notes', 'Approve the release']);
  });

  it('reorders views', () => {
    createBoardView(boardId, {name: 'Backlog', position: 1});
    createBoardView(boardId, {name: 'Archive', position: 2});
    visitBoard();
    expectViewOrder(['Default', 'Backlog', 'Archive']);

    openViewOptions('Archive');
    field('Position').select('1');
    cy.contains('button', /^Save changes$/).click();

    expectViewOrder(['Archive', 'Default', 'Backlog']);
    cy.reload();
    expectViewOrder(['Archive', 'Default', 'Backlog']);
  });

  it('renames a view while keeping its route', () => {
    const renamedView = 'Sprint';
    getDefaultView(boardId).then(({id}) => cy.visit(`/boards/${boardId}/views/${id}`));
    cy.location('pathname').then(viewPath => {
      openViewOptions('Default');
      field('View name').clear();
      field('View name').type(renamedView);
      cy.contains('button', /^Save changes$/).click();

      expectViewOrder([renamedView]);
      cy.get('[data-cy="view-heading"]').should('have.text', renamedView);
      cy.location('pathname').should('eq', viewPath);
      cy.reload();
      cy.get('[data-cy="view-heading"]').should('have.text', renamedView);
      expectViewOrder([renamedView]);
    });
  });

  it('only shows tasks that match both the status and due date filters', () => {
    getDefaultView(boardId).then(({id}) => getViewStatusIds(id)).then(statusIds => {
      createTask(boardId, 'Open task due this week', {statusId: statusIds['To Do']})
        .then(({id}) => setTaskDueDate(id, '2027-05-10T12:00:00'));
      createTask(boardId, 'Open task due later', {statusId: statusIds['To Do']})
        .then(({id}) => setTaskDueDate(id, '2027-05-20T12:00:00'));
      createTask(boardId, 'Completed task due this week', {statusId: statusIds.Done})
        .then(({id}) => setTaskDueDate(id, '2027-05-10T12:00:00'));
      createTask(boardId, 'Open task without a date', {statusId: statusIds['To Do']});
    });
    visitBoard();
    cy.get(taskCard).should('have.length', 4);

    openFilters();
    cy.contains('button', /^Add rule$/).click();
    field('Statuses').click();
    cy.contains('[role="option"]', /^To Do$/).click();
    cy.press(Cypress.Keyboard.Keys.TAB);
    cy.contains('button', /^Add rule$/).click();
    cy.contains('legend', /^Rule 2$/).closest('fieldset').within(() => {
      field('Field').select('Due date');
      field('Operator').should('have.value', 'LESS_THAN_OR_EQUAL');
      field('Local date and time').type('2027-05-10T12:00');
    });
    cy.get('[aria-label="Filter tasks"]').contains('button', /^Apply$/).click();

    expectTaskOrder(['Open task due this week']);
    cy.reload();
    cy.contains('button', /^Filter \(2\)$/).should('be.visible');
    expectTaskOrder(['Open task due this week']);
  });
});
