package co.edu.ucc.aiquota.web;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import co.edu.ucc.aiquota.demo.DemoDataService;
import co.edu.ucc.aiquota.generation.GenerationResponse;
import co.edu.ucc.aiquota.generation.MockAIGenerationService;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = "app.scheduling.enabled=false")
@AutoConfigureMockMvc
class ProxyChainIntegrationTest {

    private static final String PROMPT_BODY = "{\"prompt\":\"Explica el patrón Proxy\"}";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DemoDataService demoDataService;

    @MockitoBean
    private MockAIGenerationService mockAIGenerationService;

    @BeforeEach
    void setUp() {
        demoDataService.restoreDemoData();
        when(mockAIGenerationService.generate(any()))
                .thenReturn(new GenerationResponse("Respuesta simulada", 6, 194, 200, 1L, Instant.now()));
    }

    @Test
    void returnsTheQuotaStatusOfAConsumer() throws Exception {
        mockMvc.perform(get("/api/quota/status").header(ApiHeaders.CONSUMER_ID, "laura-free"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.monthlyTokenLimit").value(50000))
                .andExpect(jsonPath("$.requestsPerMinuteLimit").value(10))
                .andExpect(jsonPath("$.resetDate").exists());
    }

    @Test
    void rejectsRequestsWithoutTheConsumerHeader() throws Exception {
        mockMvc.perform(get("/api/quota/status"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsNotFoundForUnknownConsumers() throws Exception {
        mockMvc.perform(get("/api/quota/status").header(ApiHeaders.CONSUMER_ID, "nobody"))
                .andExpect(status().isNotFound());
    }

    @Test
    void returnsTooManyRequestsWithRetryAfterWhenTheRateLimitIsExceeded() throws Exception {
        for (int attempt = 0; attempt < 10; attempt++) {
            mockMvc.perform(generateAs("laura-free")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(PROMPT_BODY))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(generateAs("laura-free")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PROMPT_BODY))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(jsonPath("$.details.retryAfterSeconds").exists());
    }

    @Test
    void returnsPaymentRequiredWhenTheMonthlyQuotaIsExhausted() throws Exception {
        mockMvc.perform(generateAs("mateo-free").contentType(MediaType.APPLICATION_JSON).content(PROMPT_BODY))
                .andExpect(status().isOk());

        mockMvc.perform(generateAs("mateo-free").contentType(MediaType.APPLICATION_JSON).content(PROMPT_BODY))
                .andExpect(status().isPaymentRequired())
                .andExpect(jsonPath("$.details.tokensRemaining").value(0));
    }

    @Test
    void upgradingFromFreeToProRestoresAccessToTheService() throws Exception {
        mockMvc.perform(generateAs("mateo-free").contentType(MediaType.APPLICATION_JSON).content(PROMPT_BODY));
        mockMvc.perform(generateAs("mateo-free").contentType(MediaType.APPLICATION_JSON).content(PROMPT_BODY));

        mockMvc.perform(post("/api/quota/upgrade")
                        .header(ApiHeaders.CONSUMER_ID, "mateo-free")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetPlan\":\"PRO\",\"paymentReference\":\"SIM-TEST-001\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.plan").value("PRO"));

        mockMvc.perform(generateAs("mateo-free").contentType(MediaType.APPLICATION_JSON).content(PROMPT_BODY))
                .andExpect(status().isOk());
    }

    @Test
    void rejectsDowngrades() throws Exception {
        mockMvc.perform(post("/api/quota/upgrade")
                        .header(ApiHeaders.CONSUMER_ID, "sofia-pro")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"targetPlan\":\"FREE\",\"paymentReference\":\"SIM-TEST-002\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void returnsSevenDaysOfHistory() throws Exception {
        mockMvc.perform(get("/api/quota/history").header(ApiHeaders.CONSUMER_ID, "sofia-pro"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    private org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder generateAs(String consumerId) {
        return post("/api/ai/generate").header(ApiHeaders.CONSUMER_ID, consumerId);
    }
}
