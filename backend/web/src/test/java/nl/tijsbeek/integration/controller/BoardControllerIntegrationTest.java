
package nl.tijsbeek.integration.controller;

import nl.tijsbeek.dto.BoardResponse;
import nl.tijsbeek.dto.CreateBoardRequest;
import nl.tijsbeek.dto.CreateTaskRequest;
import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.ObjectMapper;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@IntegrationTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
public class BoardControllerIntegrationTest extends IntegrationTestBase {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    BoardControllerIntegrationTest(
            JdbcTemplate jdbcTemplate,
            MockMvc mockMvc,
            ObjectMapper objectMapper
    ) {
        super(jdbcTemplate);
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    @Test
    public void getFakeBoard_ReturnsNotFound() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/boards/{id}", 0L))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createBoard_ThenDeleteBoard_GetReturnsNotFound() throws Exception {
        // Arrange
        CreateBoardRequest request = new CreateBoardRequest(
                "BoardOne",
                "Board one for testing"
        );

        // Act & Assert
        MvcResult result = mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("BoardOne"))
                .andExpect(jsonPath("$.description").value("Board one for testing"))
                .andReturn();

        BoardResponse createdBoard = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                BoardResponse.class
        );

        long boardId = createdBoard.id();

        mockMvc.perform(delete("/boards/{id}", boardId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/boards/{id}", boardId))
                .andExpect(status().isNotFound());
    }

    @Test
    public void createBoard_ThenGetById_ReturnsBoard() throws Exception {
        // Arrange
        CreateBoardRequest request = new CreateBoardRequest(
                "BoardOne",
                "Board one for testing"
        );

        // Act & Assert
        MvcResult createResult = mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("BoardOne"))
                .andExpect(jsonPath("$.description").value("Board one for testing"))
                .andReturn();

        BoardResponse createdBoard = objectMapper.readValue(
                createResult.getResponse().getContentAsString(),
                BoardResponse.class
        );

        long boardId = createdBoard.id();

        mockMvc.perform(get("/boards/{id}", boardId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(boardId))
                .andExpect(jsonPath("$.title").value("BoardOne"))
                .andExpect(jsonPath("$.description").value("Board one for testing"));
    }


    @Test
    public void createBoards_ThenGetAll_ReturnsBoards() throws Exception {
        // Arrange
        CreateBoardRequest request1 = new CreateBoardRequest(
                "BoardOne",
                "First board description"
        );

        CreateBoardRequest request2 = new CreateBoardRequest(
                "BoardTwo",
                "Second board description"
        );

        // Create first board
        mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request1))
                )
                .andExpect(status().isCreated());

        // Create second board
        mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request2))
                )
                .andExpect(status().isCreated());

        // Act & Assert
        mockMvc.perform(get("/boards"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].id", hasSize(3)))
                .andExpect(jsonPath("$[*].title", containsInAnyOrder(
                        "Inbox",
                        "BoardOne",
                        "BoardTwo"
                )))
                .andExpect(jsonPath("$[*].description", containsInAnyOrder(
                        "Existing tasks",
                        "First board description",
                        "Second board description"
                )));
    }

    // create boards, add tasks for boards, get tasks and verify or its the right baord
    @Test
    public void createBoards_AddTasks_ReturnsTasksOfSameBoard() throws Exception {
        // Arrange
        CreateBoardRequest requestBoardOne = new CreateBoardRequest(
                "BoardOne",
                "Board one for testing"
        );

        CreateBoardRequest requestBoardTwo = new CreateBoardRequest(
                "BoardTwo",
                "Board two for testing"
        );

        // Act & Assert
        MvcResult createBoardOneResult = mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBoardOne))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("BoardOne"))
                .andExpect(jsonPath("$.description").value("Board one for testing"))
                .andReturn();

        MvcResult createBoardTwoResult = mockMvc.perform(
                        post("/boards")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestBoardTwo))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("BoardTwo"))
                .andExpect(jsonPath("$.description").value("Board two for testing"))
                .andReturn();

        long boardOneId = objectMapper.readValue(
                createBoardOneResult.getResponse().getContentAsString(),
                BoardResponse.class
        ).id();

        long boardTwoId = objectMapper.readValue(
                createBoardTwoResult.getResponse().getContentAsString(),
                BoardResponse.class
        ).id();


        CreateTaskRequest requestTaskOne = new CreateTaskRequest(
                "Task 1",
                boardOneId,
                "First task description",
                "unfinished"
        );

        CreateTaskRequest requestTaskTwo = new CreateTaskRequest(
                "Task 2",
                boardTwoId,
                "Second task description",
                "finished"
        );


        // Create first task
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestTaskOne))
                )
                .andExpect(status().isCreated());

        // Create second task
        mockMvc.perform(
                        post("/tasks")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestTaskTwo))
                )
                .andExpect(status().isCreated());

        mockMvc.perform(get("/boards/{id}/tasks", boardOneId, boardTwoId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))

                // Verify first task, of board one
                .andExpect(jsonPath("$[0].id").isNumber())
                .andExpect(jsonPath("$[0].boardId").value(boardOneId))
                .andExpect(jsonPath("$[0].title").value("Task 1"))
                .andExpect(jsonPath("$[0].description").value("First task description"))
                .andExpect(jsonPath("$[0].status").value("unfinished"))
                .andExpect(jsonPath("$[0].createdAt").isNotEmpty());
    }
}
