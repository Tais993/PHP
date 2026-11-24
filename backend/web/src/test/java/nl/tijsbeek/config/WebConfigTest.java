package nl.tijsbeek.config;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class WebConfigTest {
    @ParameterizedTest
    @CsvSource({
            "https://frontend.example, https://frontend.example, 200",
            "https://frontend.example, https://unknown.example, 403",
            "'', https://frontend.example, 403",
            "'', '', 200"
    })
    void checksOrigins(String configured, String origin, int expectedStatus) throws Exception {
        try (var context = context(configured)) {
            var request = get("/api/boards").contextPath("/api");
            if (!origin.isEmpty()) request.header("Origin", origin);
            var result = MockMvcBuilders.webAppContextSetup(context).build()
                    .perform(request).andExpect(status().is(expectedStatus));
            if (expectedStatus == 200 && !origin.isEmpty()) {
                result.andExpect(header().string("Access-Control-Allow-Origin", origin));
            } else {
                result.andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
            }
        }
    }

    @ParameterizedTest
    @CsvSource({
            "POST, /api/boards",
            "PUT, /api/views/1/sorting"
    })
    void allowsJsonPreflightAndTrimsMultipleOrigins(String method, String path) throws Exception {
        try (var context = context("https://frontend.example, https://preview.example ")) {
            var response = MockMvcBuilders.webAppContextSetup(context).build()
                    .perform(options(path).contextPath("/api")
                            .header("Origin", "https://preview.example")
                            .header("Access-Control-Request-Method", method)
                            .header("Access-Control-Request-Headers", "content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "https://preview.example"))
                    .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"))
                    .andReturn().getResponse();

            assertThat(response.getHeader("Access-Control-Allow-Methods"))
                    .isNotNull()
                    .satisfies(methods -> assertThat(methods.split("\\s*,\\s*")).contains(method));
            assertThat(response.getHeader("Access-Control-Allow-Headers"))
                    .isNotNull()
                    .satisfies(headers -> assertThat(headers.split("\\s*,\\s*"))
                            .usingElementComparator(String.CASE_INSENSITIVE_ORDER)
                            .contains("content-type"));
        }
    }

    private AnnotationConfigWebApplicationContext context(String origins) {
        var context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        TestPropertySourceUtils.addInlinedPropertiesToEnvironment(context,
                "app.cors.allowed-origins=" + origins);
        context.register(WebConfig.class, TestMvcConfig.class, TestController.class);
        context.refresh();
        return context;
    }

    @Configuration
    @EnableWebMvc
    static class TestMvcConfig {}

    @RestController
    static class TestController {
        @GetMapping("/boards")
        String boards() {
            return "[]";
        }

        @PostMapping("/boards")
        void createBoard() {}

        @PutMapping("/views/{id}/sorting")
        void updateSorting() {}
    }
}
