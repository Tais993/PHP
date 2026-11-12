package nl.tijsbeek.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.mock.web.MockServletContext;
import org.springframework.test.context.support.TestPropertySourceUtils;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

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

    @Test
    void allowsJsonPreflightAndTrimsMultipleOrigins() throws Exception {
        try (var context = context("https://frontend.example, https://preview.example ")) {
            MockMvcBuilders.webAppContextSetup(context).build()
                    .perform(options("/api/boards").contextPath("/api")
                            .header("Origin", "https://preview.example")
                            .header("Access-Control-Request-Method", "POST")
                            .header("Access-Control-Request-Headers", "content-type"))
                    .andExpect(status().isOk())
                    .andExpect(header().string("Access-Control-Allow-Origin", "https://preview.example"))
                    .andExpect(header().string("Access-Control-Allow-Methods", "GET,POST,DELETE"))
                    .andExpect(header().doesNotExist("Access-Control-Allow-Credentials"));
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
    }
}
