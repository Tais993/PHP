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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@IntegrationTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class TaskControllerIntegrationTest extends IntegrationTestBase {

    private final MockMvc mockMvc;

    private final ObjectMapper objectMapper;

    TaskControllerIntegrationTest(JdbcTemplate jdbcTemplate, MockMvc mockMvc, ObjectMapper objectMapper) {
        super(jdbcTemplate);
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
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
        CreateTaskRequest request = new CreateTaskRequest(
                "test1250285028921930",
                "Lorem ipsum",
                "unfinished"
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("test1250285028921930"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status").value("unfinished"))
                .andReturn();

        TaskResponse createdTask = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                TaskResponse.class
        );

        long taskId = createdTask.id();

        mockMvc.perform(
                        delete("/tasks")
                                .param("id", String.valueOf(taskId))
                )
                .andExpect(status().isNoContent());

        mockMvc.perform(
                        get("/tasks/{id}", taskId)
                )
                .andExpect(status().isNotFound());
    }

    @Test
    void createTask_ThenGetById_ReturnsTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
                "test",
                "Lorem ipsum",
                "unfinished"
        );

        MvcResult result = mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status").value("unfinished"))
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
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("test"))
                .andExpect(jsonPath("$.description").value("Lorem ipsum"))
                .andExpect(jsonPath("$.status").value("unfinished"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }



    @Test
    void createTasks_thenGetAll_ReturnsTasks() throws Exception {
        // Arrange
        CreateTaskRequest request1 = new CreateTaskRequest(
                "Task 1",
                "First task description",
                "unfinished"
        );

        CreateTaskRequest request2 = new CreateTaskRequest(
                "Task 2",
                "Second task description",
                "finished"
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
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].description").value("First task description"))
                .andExpect(jsonPath("$[0].status").value("unfinished"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty())

                // Second task
                .andExpect(jsonPath("$[1].id").isNumber())
                .andExpect(jsonPath("$[1].title").value("Task 2"))
                .andExpect(jsonPath("$[1].description").value("Second task description"))
                .andExpect(jsonPath("$[1].status").value("finished"))
                .andExpect(jsonPath("$[1].createdAt").isNotEmpty());
    }

}