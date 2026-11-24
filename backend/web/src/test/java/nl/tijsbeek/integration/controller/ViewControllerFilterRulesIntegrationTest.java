package nl.tijsbeek.integration.controller;

import nl.tijsbeek.integration.fixtures.IntegrationTestBase;
import nl.tijsbeek.testing.annotations.IntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestConstructor;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@IntegrationTest
@AutoConfigureMockMvc
@TestConstructor(autowireMode = TestConstructor.AutowireMode.ALL)
class ViewControllerFilterRulesIntegrationTest extends IntegrationTestBase {

    private final MockMvc mockMvc;

    ViewControllerFilterRulesIntegrationTest(JdbcTemplate jdbcTemplate, MockMvc mockMvc) {
        super(jdbcTemplate);
        this.mockMvc = mockMvc;
    }

    @Test
    void replaceFilterRules_SavesRulesAndFiltersTasksUntilCleared() throws Exception {
        long statusId = createStatus(1L, "To do", 0);
        createTask(1L, "Fix authentication", statusId);
        createTask(1L, "Update documentation", statusId);
        long viewId = createView(1L);

        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"field":"TITLE","operator":"CONTAINS","values":["AUTH"]}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules[0].field").value("TITLE"))
                .andExpect(jsonPath("$.filterRules[0].operator").value("CONTAINS"))
                .andExpect(jsonPath("$.filterRules[0].values[0]").value("AUTH"));

        mockMvc.perform(get("/views/{id}", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules[0].values[0]").value("AUTH"));
        mockMvc.perform(get("/boards/{id}/views", 1L))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.id == " + viewId + ")].filterRules[0].values[0]", contains("AUTH")));
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.*.items[*].title", contains("Fix authentication")));

        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filterRules\":[]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules").isEmpty());
        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.*.items[*].title", contains("Fix authentication", "Update documentation")));
    }

    @Test
    void replaceFilterRules_RejectsInvalidRequest() throws Exception {
        long viewId = createView(1L);

        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"field":"TITLE","operator":"IN","values":["Task"]}]}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").isString())
                .andExpect(jsonPath("$.detail").value(not(blankOrNullString())));
        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"operator":"CONTAINS","values":["Task"]}]}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void replaceFilterRules_StatusFromAnotherBoard_ReturnsBadRequest() throws Exception {
        long viewId = createView(1L);
        long otherBoardId = createBoard("Other board", "");
        long otherStatusId = createStatus(otherBoardId, "To do", 0);

        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"field":"STATUS","operator":"IN","values":["%d"]}]}
                                """.formatted(otherStatusId)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.detail").isString())
                .andExpect(jsonPath("$.detail").value(not(blankOrNullString())));
    }

    @Test
    void replaceFilterRules_MissingView_ReturnsNotFound() throws Exception {
        mockMvc.perform(put("/views/{id}/filter-rules", Long.MAX_VALUE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filterRules\":[]}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void replaceFilterRules_StatusFromSameBoard_SavesRuleAndFiltersTasks() throws Exception {
        // Arrange
        long todoId = createStatus(1L, "To do", 0);
        long doneId = createStatus(1L, "Done", 1);
        createTask(1L, "Open task", todoId);
        createTask(1L, "Completed task", doneId);
        long viewId = createView(1L);

        // Act & Assert
        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"field":"STATUS","operator":"IN","values":["%d"]}]}
                                """.formatted(todoId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules[0].field").value("STATUS"))
                .andExpect(jsonPath("$.filterRules[0].operator").value("IN"))
                .andExpect(jsonPath("$.filterRules[0].values[0]").value(Long.toString(todoId)));

        mockMvc.perform(get("/views/{id}/tasks", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.groups.*.items[*].title", contains("Open task")));
    }

    @Test
    void replaceFilterRules_DueDateNullCheckWithoutValues_SavesEmptyValues() throws Exception {
        // Arrange
        long viewId = createView(1L);

        // Act & Assert
        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[{"field":"DUE_DATE","operator":"IS_NULL"}]}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules[0].field").value("DUE_DATE"))
                .andExpect(jsonPath("$.filterRules[0].operator").value("IS_NULL"))
                .andExpect(jsonPath("$.filterRules[0].values").isArray())
                .andExpect(jsonPath("$.filterRules[0].values").isEmpty());

        mockMvc.perform(get("/views/{id}", viewId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.filterRules[0].values").isArray())
                .andExpect(jsonPath("$.filterRules[0].values").isEmpty());
    }

    @Test
    void replaceFilterRules_NullRule_ReturnsBadRequest() throws Exception {
        // Arrange
        long viewId = createView(1L);

        // Act & Assert
        mockMvc.perform(put("/views/{id}/filter-rules", viewId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"filterRules":[null]}
                                """))
                .andExpect(status().isBadRequest());
    }

    private long createTask(long boardId, String title, Long statusId) {
        return createTask(boardId, title, "Description for " + title, statusId);
    }

    private long createView(long boardId) {
        return createView(boardId, "Board view", "BOARD", "STATUS", "TITLE", "ASC", 3);
    }
}
