import {
  createBoard, createBoardView, createTask, deleteBoardWithTasks, replaceViewFilterRules, setTaskDueDate,
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

function sortByTitleAscending() {
  cy.get('button[aria-label^="Sort by:"]').click();
  field('Sort field').select('Title');
  field('Direction').select('Ascending');
  cy.contains('button', /^Apply$/).click();
}

describe('Views', {scrollBehavior: 'nearest'}, () => {
  let boardId;
  let hasStatusFixtures;

  function visitBoard() {
    cy.visit(`/boards/${boardId}`);
  }

  function visitNewView() {
    createBoardView(boardId).then(({id}) => cy.visit(`/boards/${boardId}/views/${id}`));
  }

  beforeEach(() => {
    boardId = undefined;
    hasStatusFixtures = false;
    cy.viewport(1280, 900);
    return createBoard().then(({id}) => {
      boardId = id;
    });
  });

  afterEach(() => {
    if (boardId !== undefined) {
      return deleteBoardWithTasks(boardId, {hasStatusFixtures});
    }
  });

  it('creates multiple views on the same board', () => {
    visitBoard();

    addView('Sprint backlog');
    addView('Release planning');

    expectViewOrder(['Sprint backlog', 'Release planning']);
    cy.reload();
    expectViewOrder(['Sprint backlog', 'Release planning']);
  });

  it('applies and clears a title filter', () => {
    createTask(boardId, 'Audit the deployment');
    createTask(boardId, 'Review the proposal');
    visitNewView();
    expectTaskOrder(['Review the proposal', 'Audit the deployment']);

    applyTitleFilter('deployment');

    expectTaskOrder(['Audit the deployment']);
    cy.reload();
    expectTaskOrder(['Audit the deployment']);

    clearFilters();

    expectTaskOrder(['Review the proposal', 'Audit the deployment']);
    cy.reload();
    expectTaskOrder(['Review the proposal', 'Audit the deployment']);
  });

  it('changes the sort field and direction', () => {
    createTask(boardId, 'Approve the release');
    createTask(boardId, 'Write the release notes');
    createTask(boardId, 'Check the build');
    visitNewView();
    expectTaskOrder(['Check the build', 'Write the release notes', 'Approve the release']);

    sortByTitleAscending();

    expectTaskOrder(['Approve the release', 'Check the build', 'Write the release notes']);
    cy.reload();
    cy.get('button[aria-label="Sort by: Title, ascending"]').should('be.visible');
    expectTaskOrder(['Approve the release', 'Check the build', 'Write the release notes']);
  });

  it('switches between views with different saved filters', () => {
    createTask(boardId, 'Backend deployment');
    createTask(boardId, 'Frontend deployment');
    createBoardView(boardId, {name: 'Backend work'}).then(({id}) => {
      return replaceViewFilterRules(id, [{field: 'TITLE', operator: 'CONTAINS', values: ['Backend']}]);
    });
    createBoardView(boardId, {name: 'Frontend work', position: 1}).then(({id}) => {
      return replaceViewFilterRules(id, [{field: 'TITLE', operator: 'CONTAINS', values: ['Frontend']}]);
    });
    visitBoard();

    openView('Backend work');
    expectTaskOrder(['Backend deployment']);
    openView('Frontend work');
    expectTaskOrder(['Frontend deployment']);
    cy.reload();
    cy.get('[data-cy="view-heading"]').should('have.text', 'Frontend work');
    expectTaskOrder(['Frontend deployment']);
    openView('Backend work');
    expectTaskOrder(['Backend deployment']);
  });

  it('duplicates a configured view with its filter and sorting', () => {
    createTask(boardId, 'Approve the release');
    createTask(boardId, 'Write the release notes');
    createTask(boardId, 'Review the proposal');
    visitNewView();

    applyTitleFilter('release');
    expectTaskOrder(['Write the release notes', 'Approve the release']);
    sortByTitleAscending();
    expectTaskOrder(['Approve the release', 'Write the release notes']);

    openViewOptions('Task test view');
    cy.contains('button', /^Duplicate view$/).click();
    openView('Copy of Task test view');

    cy.get('button[aria-label="Sort by: Title, ascending"]').should('be.visible');
    expectTaskOrder(['Approve the release', 'Write the release notes']);
    cy.reload();
    expectTaskOrder(['Approve the release', 'Write the release notes']);
    clearFilters();
    expectTaskOrder(['Approve the release', 'Review the proposal', 'Write the release notes']);
    openView('Task test view');
    expectTaskOrder(['Approve the release', 'Write the release notes']);
  });

  it('reorders views', () => {
    createBoardView(boardId, {name: 'Backlog'});
    createBoardView(boardId, {name: 'Active sprint', position: 1});
    createBoardView(boardId, {name: 'Archive', position: 2});
    visitBoard();
    expectViewOrder(['Backlog', 'Active sprint', 'Archive']);

    openViewOptions('Archive');
    field('Position').select('1');
    cy.contains('button', /^Save changes$/).click();

    expectViewOrder(['Archive', 'Backlog', 'Active sprint']);
    cy.reload();
    expectViewOrder(['Archive', 'Backlog', 'Active sprint']);
  });

  it('renames a view while keeping its route', () => {
    visitNewView();
    cy.location('pathname').then(viewPath => {
      openViewOptions('Task test view');
      field('View name').clear();
      field('View name').type('Release checklist');
      cy.contains('button', /^Save changes$/).click();

      expectViewOrder(['Release checklist']);
      cy.get('[data-cy="view-heading"]').should('have.text', 'Release checklist');
      cy.location('pathname').should('eq', viewPath);
      cy.reload();
      cy.get('[data-cy="view-heading"]').should('have.text', 'Release checklist');
      expectViewOrder(['Release checklist']);
    });
  });

  it('only shows tasks that match both the status and due date filters', () => {
    hasStatusFixtures = true;
    cy.task('seedBoardStatuses', boardId).then(statusIds => {
      createTask(boardId, 'Open task due this week', {statusId: statusIds['To do']})
        .then(({id}) => setTaskDueDate(id, '2027-05-10T12:00:00'));
      createTask(boardId, 'Open task due later', {statusId: statusIds['To do']})
        .then(({id}) => setTaskDueDate(id, '2027-05-20T12:00:00'));
      createTask(boardId, 'Completed task due this week', {statusId: statusIds.Done})
        .then(({id}) => setTaskDueDate(id, '2027-05-10T12:00:00'));
      createTask(boardId, 'Open task without a date', {statusId: statusIds['To do']});
    });
    visitNewView();
    cy.get(taskCard).should('have.length', 4);

    openFilters();
    cy.contains('button', /^Add rule$/).click();
    field('Statuses').click();
    cy.contains('[role="option"]', /^To do$/).click();
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
