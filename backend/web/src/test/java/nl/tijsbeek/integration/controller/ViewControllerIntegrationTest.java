package nl.tijsbeek.integration.controller;

import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@IntegrationTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ViewControllerIntegrationTest extends IntegrationTestBase {

    private final MockMvc mockMvc;

    ViewControllerIntegrationTest(JdbcTemplate jdbcTemplate, MockMvc mockMvc) {
        super(jdbcTemplate);
        this.mockMvc = mockMvc;
    }

    @Test
    void createBoardView_ValidRequest_SavesViewOnBoard() throws Exception {
        // Arrange
        long boardId = createBoard("Project board", "");

        // Act & Assert
        mockMvc.perform(post("/views")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardId": %d,
                                  "name": "Project view",
                                  "type": "BOARD",
                                  "groupBy": "STATUS",
                                  "sortBy": "CREATED_AT",
                                  "sortDirection": "DESC",
                                  "position": 2
                                }
                                """.formatted(boardId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.boardId").value(boardId))
                .andExpect(jsonPath("$.name").value("Project view"))
                .andExpect(jsonPath("$.type").value("BOARD"))
                .andExpect(jsonPath("$.groupBy").value("STATUS"))
                .andExpect(jsonPath("$.sortBy").value("CREATED_AT"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"))
                .andExpect(jsonPath("$.position").value(2))
                .andExpect(jsonPath("$.filterRules").isArray())
                .andExpect(jsonPath("$.filterRules").isEmpty());

        mockMvc.perform(get("/boards/{id}/views", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].name", contains("Project view")))
                .andExpect(jsonPath("$[0].type").value("BOARD"))
                .andExpect(jsonPath("$[0].groupBy").value("STATUS"))
                .andExpect(jsonPath("$[0].sortBy").value("CREATED_AT"))
                .andExpect(jsonPath("$[0].sortDirection").value("DESC"))
                .andExpect(jsonPath("$[0].position").value(2));
    }

    @Test
    void createBoardView_InvalidType_ReturnsBadRequest() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/views")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "boardId": 1,
                                  "name": "Board view",
                                  "type": "INVALID",
                                  "groupBy": "STATUS",
                                  "sortBy": "TITLE",
                                  "sortDirection": "ASC",
                                  "position": 0
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSortingView_ValidRequest_SavesSortingAndReordersTasks() throws Exception {
        // Arrange
        long statusId = createStatus(1L, "To do", 0);
        createTask(1L, "B task", statusId);
        createTask(1L, "A task", statusId);
        createTask(1L, "C task", statusId);
        long viewId = createView(1L, "Board view", "BOARD", "STATUS", "CREATED_AT", "ASC", 3);

        // Act & Assert
        mockMvc.perform(put("/views/{id}/sorting", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sortBy":"TITLE","sortDirection":"DESC"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(viewId))
                .andExpect(jsonPath("$.sortBy").value("TITLE"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));

        mockMvc.perform(get("/views/{id}", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sortBy").value("TITLE"))
                .andExpect(jsonPath("$.sortDirection").value("DESC"));
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.*.items[*].title", contains("C task", "B task", "A task")));
    }

    @Test
    void updateSortingView_InvalidSortDirection_ReturnsBadRequest() throws Exception {
        // Arrange
        long viewId = createView(1L);

        // Act & Assert
        mockMvc.perform(put("/views/{id}/sorting", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sortBy":"TITLE","sortDirection":"INVALID"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateSortingView_MissingView_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/views/{id}/sorting", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"sortBy":"TITLE","sortDirection":"DESC"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Board view with ID " + Long.MAX_VALUE + " was not found"))
                .andExpect(jsonPath("$.instance").value("/views/" + Long.MAX_VALUE + "/sorting"));
    }

    @Test
    void getView_ExistingView_ReturnsSettings() throws Exception {
        // Arrange
        long viewId = createView(1L);

        // Act & Assert
        mockMvc.perform(get("/views/{id}", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(viewId))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.name").value("Board view"))
                .andExpect(jsonPath("$.type").value("BOARD"))
                .andExpect(jsonPath("$.groupBy").value("STATUS"))
                .andExpect(jsonPath("$.sortBy").value("TITLE"))
                .andExpect(jsonPath("$.sortDirection").value("ASC"))
                .andExpect(jsonPath("$.position").value(3))
                .andExpect(jsonPath("$.filterRules").isArray())
                .andExpect(jsonPath("$.filterRules").isEmpty());
    }

    @Test
    void getView_MissingView_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/views/{id}", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.type").value("about:blank"))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Board view with ID " + Long.MAX_VALUE + " was not found"))
                .andExpect(jsonPath("$.instance").value("/views/" + Long.MAX_VALUE));
    }

    @Test
    void getViewTasks_MissingView_ReturnsNotFoundProblemDetail() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/views/{id}/tasks", Long.MAX_VALUE))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("Board view with ID " + Long.MAX_VALUE + " was not found"))
                .andExpect(jsonPath("$.instance").value("/views/" + Long.MAX_VALUE + "/tasks"));
    }

    @Test
    void getViewTasks_ReturnsOrderedGroupsAndTasksFromCorrectBoard() throws Exception {
        // Arrange
        long doneId = createStatus(1L, "Done", 20);
        long todoId = createStatus(1L, "To do", 5);
        long doneZ = createTask(1L, "Z done", doneId);
        long todoB = createTask(1L, "B todo", todoId);
        long doneA = createTask(1L, "A done", doneId);
        long todoA = createTask(1L, "A todo", todoId);
        long otherBoardId = createBoard("Other board", "");
        long otherStatusId = createStatus(otherBoardId, "To do", 0);
        createTask(otherBoardId, "Other board task", otherStatusId);
        long viewId = createView(1L);

        String todoGroupPath = "$.groups['" + todoId + "']";
        String doneGroupPath = "$.groups['" + doneId + "']";

        // Act & Assert
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedBy").value("STATUS"))
                .andExpect(jsonPath("$.groups.*.id", contains((int) todoId, (int) doneId)))

                // Verify the To do group and its task order
                .andExpect(jsonPath(todoGroupPath + ".id").value(todoId))
                .andExpect(jsonPath(todoGroupPath + ".name").value("To do"))
                .andExpect(jsonPath(todoGroupPath + ".position").value(5))
                .andExpect(jsonPath(todoGroupPath + ".items[*].id", contains((int) todoA, (int) todoB)))
                .andExpect(jsonPath(todoGroupPath + ".items[*].title", contains("A todo", "B todo")))
                .andExpect(jsonPath(todoGroupPath + ".items[*].boardId", contains(1, 1)))
                .andExpect(jsonPath(todoGroupPath + ".items[*].description", contains(
                        "Description for A todo", "Description for B todo")))
                .andExpect(jsonPath(todoGroupPath + ".items[0].createdAt").isNotEmpty())
                .andExpect(jsonPath(todoGroupPath + ".items[1].createdAt").isNotEmpty())
                .andExpect(jsonPath(todoGroupPath + ".items[*].status.id", contains((int) todoId, (int) todoId)))
                .andExpect(jsonPath(todoGroupPath + ".items[*].status.name", contains("To do", "To do")))
                .andExpect(jsonPath(todoGroupPath + ".items[*].status.position", contains(5, 5)))

                // Verify the Done group and its task order
                .andExpect(jsonPath(doneGroupPath + ".id").value(doneId))
                .andExpect(jsonPath(doneGroupPath + ".name").value("Done"))
                .andExpect(jsonPath(doneGroupPath + ".position").value(20))
                .andExpect(jsonPath(doneGroupPath + ".items[*].id", contains((int) doneA, (int) doneZ)));
    }

    @Test
    void getViewTasks_StatusWithoutTasks_ReturnsEmptyGroup() throws Exception {
        // Arrange
        long todoId = createStatus(1L, "To do", 0);
        long doneId = createStatus(1L, "Done", 1);
        long taskId = createTask(1L, "Task", todoId);
        long viewId = createView(1L);

        String todoGroupPath = "$.groups['" + todoId + "']";
        String doneGroupPath = "$.groups['" + doneId + "']";

        // Act & Assert
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.*.id", contains((int) todoId, (int) doneId)))
                .andExpect(jsonPath(todoGroupPath + ".items[*].id", contains((int) taskId)))
                .andExpect(jsonPath(doneGroupPath + ".id").value(doneId))
                .andExpect(jsonPath(doneGroupPath + ".name").value("Done"))
                .andExpect(jsonPath(doneGroupPath + ".position").value(1))
                .andExpect(jsonPath(doneGroupPath + ".items").isArray())
                .andExpect(jsonPath(doneGroupPath + ".items").isEmpty());
    }

    @Test
    void getViewTasks_TaskWithoutStatus_HandlesUnassignedTask() throws Exception {
        // Arrange
        long statusId = createStatus(1L, "To do", 0);
        long taskB = createTask(1L, "B unassigned", null);
        long assignedTask = createTask(1L, "Assigned task", statusId);
        long taskA = createTask(1L, "A unassigned", null);
        long viewId = createView(1L);

        String assignedGroupPath = "$.groups['" + statusId + "']";

        // Act & Assert
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groupedBy").value("STATUS"))
                .andExpect(jsonPath("$.items").doesNotExist())
                .andExpect(jsonPath("$.groups.*.name", contains("To do", "Unassigned")))
                .andExpect(jsonPath(assignedGroupPath + ".id").value(statusId))
                .andExpect(jsonPath(assignedGroupPath + ".items[*].id", contains((int) assignedTask)))

                // Verify the Unassigned group appears last and preserves task order
                .andExpect(jsonPath("$.groups['0'].id").value(nullValue()))
                .andExpect(jsonPath("$.groups['0'].name").value("Unassigned"))
                .andExpect(jsonPath("$.groups['0'].position").value(Integer.MAX_VALUE))
                .andExpect(jsonPath("$.groups['0'].items[*].id", contains((int) taskA, (int) taskB)))
                .andExpect(jsonPath("$.groups['0'].items[0].status").value(nullValue()))
                .andExpect(jsonPath("$.groups['0'].items[1].status").value(nullValue()));
    }

    private long createTask(long boardId, String title, Long statusId) {
        return createTask(boardId, title, "Description for " + title, statusId);
    }

    private long createView(long boardId) {
        return createView(boardId, "Board view", "BOARD", "STATUS", "TITLE", "ASC", 3);
    }
}
