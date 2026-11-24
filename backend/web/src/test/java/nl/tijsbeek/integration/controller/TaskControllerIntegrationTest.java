package nl.tijsbeek.integration.controller;

import nl.tijsbeek.dto.request.CreateTaskRequest;
import nl.tijsbeek.dto.response.TaskResponse;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@IntegrationTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TaskControllerIntegrationTest extends IntegrationTestBase {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;

    TaskControllerIntegrationTest(JdbcTemplate jdbcTemplate, MockMvc mockMvc, ObjectMapper objectMapper) {
        super(jdbcTemplate);
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Test
    void getFakeTaskById_ReturnsNotFound() throws Exception {
        mockMvc.perform(
                        get("/tasks/{id}", "9999")
                )
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void createTask_ThenDeleteTask_GetTaskReturnsNotFound() throws Exception {
        long statusId = createStatus(1L, "unfinished", 0);
        CreateTaskRequest request = new CreateTaskRequest(
                "test1250285028921930",
                1L,
                "Lorem ipsum",
                null,
                statusId
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.boardId").isNumber())
                .andExpect(jsonPath("$.title").value("test1250285028921930"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status.id").value(statusId))
                .andExpect(jsonPath("$.status.name").value("unfinished"))
                .andReturn();

        TaskResponse createdTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        );

        long taskId = createdTask.id();

        mockMvc.perform(
                        delete("/tasks/{id}", taskId)
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/tasks/{id}", taskId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createTaskWithSelectedStatus_ThenGetById_ReturnsTaskWithSelectedStatus() throws Exception {
        createStatus(1L, "unfinished", 0);
        long selectedStatusId = createStatus(1L, "finished", 7);
        CreateTaskRequest request = new CreateTaskRequest(
                "Finished task",
                1L,
                "Uses the selected status",
                null,
                selectedStatusId
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Finished task"))
                .andExpect(jsonPath("$.description").value("Uses the selected status"))
                .andExpect(jsonPath("$.status.id").value(selectedStatusId))
                .andExpect(jsonPath("$.status.name").value("finished"))
                .andExpect(jsonPath("$.status.position").value(7))
                .andReturn();

        long taskId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        ).id();

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Finished task"))
                .andExpect(jsonPath("$.description").value("Uses the selected status"))
                .andExpect(jsonPath("$.status.id").value(selectedStatusId))
                .andExpect(jsonPath("$.status.name").value("finished"))
                .andExpect(jsonPath("$.status.position").value(7))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createTasks_thenGetAll_ReturnsTasks() throws Exception {
        // Arrange
        long unfinishedId = createStatus(1L, "unfinished", 0);
        long finishedId = createStatus(1L, "finished", 1);
        CreateTaskRequest request1 = new CreateTaskRequest(
                "Task 1",
                1L,
                "First task description",
                null,
                unfinishedId
        );

        CreateTaskRequest request2 = new CreateTaskRequest(
                "Task 2",
                1L,
                "Second task description",
                null,
                finishedId
        );

        // Create first task
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1))
                )
                .andExpect(status().isCreated());

        // Create second task
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2))
                )
                .andExpect(status().isCreated());

        // Act
        byte[] response = mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id").value(everyItem(instanceOf(Number.class))))
                .andReturn().getResponse().getContentAsByteArray();
        TaskResponse[] tasks = objectMapper.readValue(response, TaskResponse[].class);

        // Assert
        assertThat(tasks)
                .allSatisfy(task -> {
                    assertThat(task.id()).isNotNull();
                    assertThat(task.createdAt()).isNotNull();
                    assertThat(task.status()).isNotNull();
                })
                .extracting(TaskResponse::boardId, TaskResponse::title, TaskResponse::description,
                        task -> task.status().id(), task -> task.status().name())
                .containsExactlyInAnyOrder(
                        tuple(1L, "Task 1", "First task description", unfinishedId, "unfinished"),
                        tuple(1L, "Task 2", "Second task description", finishedId, "finished"));
    }

    @Test
    void createTaskWithoutStatus_ThenGetById_ReturnsTaskWithoutStatus() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Task without status",
                1L,
                "Status is optional",
                null,
                null
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Task without status"))
                .andExpect(jsonPath("$.description").value("Status is optional"))
                .andExpect(jsonPath("$.status").value(nullValue()))
                .andReturn();

        long taskId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        ).id();

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Task without status"))
                .andExpect(jsonPath("$.description").value("Status is optional"))
                .andExpect(jsonPath("$.status").value(nullValue()))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());

        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].status").value(nullValue()));

        mockMvc.perform(get("/boards/{id}/tasks", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(taskId))
                .andExpect(jsonPath("$[0].status").value(nullValue()));
    }

    @Test
    void createTaskWithoutBoardId_ThenGetById_ReturnsTaskOnDefaultBoard() throws Exception {
        long statusId = createStatus(1L, "unfinished", 0);
        CreateTaskRequest request = new CreateTaskRequest(
                "Task on default board",
                null,
                "Uses the Inbox board",
                null,
                statusId
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Task on default board"))
                .andExpect(jsonPath("$.description").value("Uses the Inbox board"))
                .andExpect(jsonPath("$.status.id").value(statusId))
                .andExpect(jsonPath("$.status.name").value("unfinished"))
                .andExpect(jsonPath("$.status.position").value(0))
                .andReturn();

        long taskId = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        ).id();

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Task on default board"))
                .andExpect(jsonPath("$.description").value("Uses the Inbox board"))
                .andExpect(jsonPath("$.status.id").value(statusId))
                .andExpect(jsonPath("$.status.name").value("unfinished"))
                .andExpect(jsonPath("$.status.position").value(0))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @ParameterizedTest
    @EnumSource(InvalidTaskReference.class)
    void createTask_InvalidBoardOrStatus_RejectsRequestAndPersistsNothing(InvalidTaskReference invalidReference)
            throws Exception {
        // Arrange
        long statusId = createStatus(1L, "To do", 0);
        long otherBoardId = createBoard("Other board", "");
        long otherStatusId = createStatus(otherBoardId, "To do", 0);
        createTask(1L, "Existing task", "Must remain unchanged", statusId);
        List<Map<String, Object>> tasksBefore = readTaskRows();
        long boardId = switch (invalidReference) {
            case MISSING_BOARD, MISSING_BOARD_WITHOUT_STATUS -> Long.MAX_VALUE;
            default -> 1L;
        };
        Long requestedStatusId = switch (invalidReference) {
            case MISSING_BOARD -> statusId;
            case MISSING_BOARD_WITHOUT_STATUS -> null;
            case MISSING_STATUS -> Long.MAX_VALUE;
            case STATUS_FROM_ANOTHER_BOARD -> otherStatusId;
        };
        CreateTaskRequest request = new CreateTaskRequest(
                "Rejected task",
                boardId,
                "Invalid reference",
                null,
                requestedStatusId
        );

        // Act & Assert
        try {
            mockMvc.perform(post("/tasks")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                    .andExpect(jsonPath("$.status").value(400));
        } finally {
            // Verify existing tasks remain unchanged and no task was created
            assertThat(readTaskRows()).isEqualTo(tasksBefore);
        }
    }

    enum InvalidTaskReference {
        MISSING_BOARD, MISSING_BOARD_WITHOUT_STATUS, MISSING_STATUS, STATUS_FROM_ANOTHER_BOARD
    }

    @Test
    void createTask_MalformedJson_ReturnsBadRequestProblemDetail() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void updateTaskStatus_ValidStatus_UpdatesAndReturnsTask() throws Exception {
        long todoStatusId = createStatus(1L, "todo", 0);
        long doneStatusId = createStatus(1L, "done", 1);
        long taskId = createTask(1L, "Test task", "Description", todoStatusId);
        LocalDateTime dueDate = LocalDateTime.parse("2026-12-01T14:30:45");
        jdbcTemplate.update("UPDATE task SET due_date = ? WHERE id = ?", dueDate, taskId);

        mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", doneStatusId))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01T14:30:45"))
                .andExpect(jsonPath("$.status.id").value(doneStatusId))
                .andExpect(jsonPath("$.status.name").value("done"));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test task"))
                .andExpect(jsonPath("$.description").value("Description"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01T14:30:45"))
                .andExpect(jsonPath("$.status.id").value(doneStatusId))
                .andExpect(jsonPath("$.status.name").value("done"));
    }

    @Test
    void updateTask_TitleDescriptionAndDueDate_UpdatesFieldsAndKeepsStatus() throws Exception {
        long statusId = createStatus(1L, "todo", 0);
        long taskId = createTask(1L, "Test task", "Description", statusId);
        Map<String, String> request = Map.of(
                "title", "Updated task",
                "description", "Updated description",
                "dueDate", "2026-12-01T14:30:45"
        );

        mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01T14:30:45"))
                .andExpect(jsonPath("$.status.id").value(statusId));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated task"))
                .andExpect(jsonPath("$.description").value("Updated description"))
                .andExpect(jsonPath("$.dueDate").value("2026-12-01T14:30:45"))
                .andExpect(jsonPath("$.status.id").value(statusId));
    }

    @Test
    void updateTask_NullDueDate_ClearsDueDateAndKeepsStatus() throws Exception {
        long statusId = createStatus(1L, "todo", 0);
        long taskId = createTask(1L, "Test task", "Description", statusId);
        LocalDateTime dueDate = LocalDateTime.parse("2026-12-01T14:30:45");
        jdbcTemplate.update("UPDATE task SET due_date = ? WHERE id = ?", dueDate, taskId);

        mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"dueDate\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.dueDate").value(nullValue()))
                .andExpect(jsonPath("$.status.id").value(statusId));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dueDate").value(nullValue()))
                .andExpect(jsonPath("$.status.id").value(statusId));
    }

    @Test
    void updateTaskStatus_NullStatus_ClearsStatusAndReturnsTask() throws Exception {
        long statusId = createStatus(1L, "todo", 0);
        long taskId = createTask(1L, "Test task", "Description", statusId);

        mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\": null}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.status").value(nullValue()));

        mockMvc.perform(get("/tasks/{id}", taskId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(nullValue()));
    }

    @Test
    void updateTask_NonExistingTask_ReturnsNotFound() throws Exception {
        long statusId = createStatus(1L, "todo", 0);

        mockMvc.perform(patch("/tasks/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", statusId))))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateTaskStatus_StatusFromAnotherBoard_ReturnsBadRequest() throws Exception {
        long statusId = createStatus(1L, "todo", 0);
        long otherBoardId = createBoard("Other board", "");
        long otherStatusId = createStatus(otherBoardId, "other", 0);
        long taskId = createTask(1L, "Test task", "Description", statusId);

        mockMvc.perform(patch("/tasks/{id}", taskId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", otherStatusId))))
                .andExpect(status().isBadRequest());
    }

    private List<Map<String, Object>> readTaskRows() {
        return jdbcTemplate.queryForList("SELECT * FROM task ORDER BY id");
    }
}
