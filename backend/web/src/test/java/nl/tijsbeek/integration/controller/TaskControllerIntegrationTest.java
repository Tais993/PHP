package nl.tijsbeek.integration.controller;

import nl.tijsbeek.dto.CreateTaskRequest;
import nl.tijsbeek.dto.TaskResponse;

import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.nullValue;
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
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_ThenDeleteTask_GetTaskReturnsNotFound() throws Exception {
        long statusId = createStatus(1L, "unfinished", 0);
        CreateTaskRequest request = new CreateTaskRequest(
                "test1250285028921930",
                1L,
                "Lorem ipsum",
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
    void createTask_ThenGetById_ReturnsTask() throws Exception {
        long statusId = createStatus(1L, "unfinished", 0);
        CreateTaskRequest request = new CreateTaskRequest(
                "test",
                1L,
                "Lorem ipsum",
                statusId
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.boardId").isNumber())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status.id").value(statusId))
                .andExpect(jsonPath("$.status.name").value("unfinished"))
                .andExpect(jsonPath("$.status.position").value(0))
                .andReturn();

        TaskResponse createdTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        );

        long taskId = createdTask.id();

        mockMvc.perform(
                        get("/tasks/{id}", taskId)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.boardId").value(1L))
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status.id").value(statusId))
                .andExpect(jsonPath("$.status.name").value("unfinished"))
                .andExpect(jsonPath("$.status.position").value(0))
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
                unfinishedId
        );

        CreateTaskRequest request2 = new CreateTaskRequest(
                "Task 2",
                1L,
                "Second task description",
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

        // Act & Assert: Retrieve all tasks
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))

                // First task
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].boardId").value(1L))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].description").value("First task description"))
                .andExpect(jsonPath("$[0].status.id").value(unfinishedId))
                .andExpect(jsonPath("$[0].status.name").value("unfinished"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())

                // Second task
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].boardId").value(1L))
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].description").value("Second task description"))
                .andExpect(jsonPath("$[1].status.id").value(finishedId))
                .andExpect(jsonPath("$[1].status.name").value("finished"))
                .andExpect(jsonPath("$[1].createdAt").isNotEmpty());
    }

    @Test
    void createTaskWithoutStatus_ThenGetById_ReturnsTaskWithoutStatus() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "Task without status",
                1L,
                "Status is optional",
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

    @Test
    void createTaskWithSecondBoardStatus_ThenGetById_ReturnsSelectedStatus() throws Exception {
        createStatus(1L, "unfinished", 0);
        long selectedStatusId = createStatus(1L, "finished", 7);
        CreateTaskRequest request = new CreateTaskRequest(
                "Finished task",
                1L,
                "Uses the selected status",
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

    private long createStatus(long boardId, String name, int position) {
        return jdbcTemplate.queryForObject(
                "INSERT INTO board_status (board_id, name, position) VALUES (?, ?, ?) RETURNING id",
                Long.class, boardId, name, position);
    }

}
