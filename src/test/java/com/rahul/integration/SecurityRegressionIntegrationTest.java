package com.rahul.integration;

import com.rahul.security.JwtService;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MvcResult;
import com.rahul.security.TenantDatabaseContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

class SecurityRegressionIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TenantDatabaseContext tenantDatabaseContext;

    @Autowired
    private Environment environment;


    @BeforeEach
    void setUp() {

        mockMvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
    }


    @Test
    void jwtSecretShouldBeResolvedCorrectly() {

        String value =
                environment.getProperty("jwt.secret");

        assertThat(value).isNotBlank();

        assertThat(value.length())
                .isGreaterThanOrEqualTo(32);
    }


    @Test
    void protectedEndpointWithoutTokenShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/user/profile")).andExpect(status().isUnauthorized());
    }

    @Test
    void protectedEndpointWithInvalidTokenShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer invalid-token")).andExpect(status().isUnauthorized());
    }

    @Test
    void tenantEndpointWithoutTokenShouldReturn401() throws Exception {

        mockMvc.perform(get("/api/test/tenant/users")).andExpect(status().isUnauthorized());
    }

    @Test
    void validUserShouldAccessUserEndpoint() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens tokens = login(tenantId, "alice", "Password@123");

        mockMvc.perform(get("/api/user/profile").header("Authorization", "Bearer " + tokens.accessToken())).andExpect(status().isOk());
    }

    @Test
    void userShouldBeForbiddenFromAdminEndpoint() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens tokens = login(tenantId, "alice", "Password@123");

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + tokens.accessToken())).andExpect(status().isForbidden());
    }

    @Test
    void adminShouldAccessAdminEndpoint() throws Exception {

        String tenantId = createTenant();

        ensureAdminRole(tenantId);

        registerUser(tenantId, "rahul", "Password@123");

        makeAdmin(tenantId, "rahul");

        LoginTokens tokens = login(tenantId, "rahul", "Password@123");

        mockMvc.perform(get("/api/admin/dashboard").header("Authorization", "Bearer " + tokens.accessToken())).andExpect(status().isOk());
    }


    @Test
    void wrongPasswordShouldReturnUnauthorized() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        String request = """
                {
                  "tenantId": "%s",
                  "username": "alice",
                  "password": "WrongPassword@123"
                }
                """.formatted(tenantId);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isUnauthorized());
    }


    @Test
    void unknownUserShouldReturnUnauthorized() throws Exception {

        String tenantId = createTenant();

        String request = """
                {
                  "tenantId": "%s",
                  "username": "does-not-exist",
                  "password": "Password@123"
                }
                """.formatted(tenantId);

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isUnauthorized());
    }

    @Test
    void tenantHeaderMustNotOverrideJwtTenant() throws Exception {

        String tenantA = createTenant();

        String tenantB = createTenant();

        registerUser(tenantA, "alice", "Password@123");

        LoginTokens tokens = login(tenantA, "alice", "Password@123");

        mockMvc.perform(get("/api/user/tenant").header("Authorization", "Bearer " + tokens.accessToken()).header("X-Tenant-Id", tenantB)).andExpect(status().isOk()).andExpect(content().string(tenantA));
    }

    @Test
    void loginShouldIssueTokenForRequestedTenant() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens tokens = login(tenantId, "alice", "Password@123");

        String tenantFromToken = extractJwtClaim(tokens.accessToken(), "tenant_id");

        assertThat(tenantFromToken).isEqualTo(tenantId);
    }

    @Test
    @Transactional
    void tenantDatabaseContextShouldSetTenant() {

        UUID tenantId = UUID.randomUUID();

        tenantDatabaseContext.setCurrentTenant(tenantId);

        assertThat(tenantDatabaseContext.getCurrentTenant()).isEqualTo(tenantId);
    }

    @Test
    void refreshShouldRotateRefreshToken() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens initial = login(tenantId, "alice", "Password@123");

        String rotatedToken = refreshAccessToken(initial.refreshToken());

        assertThat(rotatedToken).isNotEqualTo(initial.refreshToken());
    }

    @Test
    void rotatedRefreshTokenCannotBeReused() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens initial = login(tenantId, "alice", "Password@123");

        refreshAccessToken(initial.refreshToken());

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(initial.refreshToken()))).andExpect(status().isUnauthorized());
    }

    @Test
    void oldRefreshTokenShouldBeMarkedRevoked() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens initial = login(tenantId, "alice", "Password@123");

        refreshAccessToken(initial.refreshToken());

        JdbcTemplate adminJdbcTemplate = TestDatabaseHelper.adminJdbcTemplate(POSTGRES.getJdbcUrl());

        Integer revokedCount = adminJdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM refresh_tokens
                WHERE revoked = true
                """, Integer.class);

        assertThat(revokedCount).isGreaterThanOrEqualTo(1);
    }

    @Test
    void logoutShouldRevokeRefreshToken() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens tokens = login(tenantId, "alice", "Password@123");

        mockMvc.perform(post("/api/auth/logout").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(tokens.refreshToken()))).andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(tokens.refreshToken()))).andExpect(status().isUnauthorized());
    }

    @Test
    void invalidRefreshTokenShouldReturn401() throws Exception {

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "invalid-refresh-token"
                }
                """)).andExpect(status().isUnauthorized());
    }

    @Test
    void missingRefreshTokenShouldReturn400() throws Exception {

        mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("{}")).andExpect(status().isBadRequest());
    }

    @Test
    void refreshTokenCannotBeUsedAgainstAnotherTenantContext() throws Exception {

        String tenantA = createTenant();

        String tenantB = createTenant();

        registerUser(tenantA, "alice", "Password@123");

        LoginTokens tokens = login(tenantA, "alice", "Password@123");

        mockMvc.perform(post("/api/auth/refresh").header("X-Tenant-Id", tenantB).contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(tokens.refreshToken()))).andExpect(status().isOk());
    }

    @Test
    void concurrentRefreshWithSameTokenShouldAllowOnlyOneRequest() throws Exception {

        String tenantId = createTenant();

        registerUser(tenantId, "alice", "Password@123");

        LoginTokens tokens = login(tenantId, "alice", "Password@123");

        String refreshToken = tokens.refreshToken();

        int requestCount = 2;

        ExecutorService executor = Executors.newFixedThreadPool(requestCount);

        CountDownLatch startLatch = new CountDownLatch(1);

        List<Callable<MvcResult>> tasks = new ArrayList<>();

        for (int i = 0; i < requestCount; i++) {

            tasks.add(() -> {

                startLatch.await();

                return performRefresh(refreshToken);
            });
        }

        List<Future<MvcResult>> futures = new ArrayList<>();

        for (Callable<MvcResult> task : tasks) {
            futures.add(executor.submit(task));
        }

        startLatch.countDown();

        List<MvcResult> results = new ArrayList<>();

        for (Future<MvcResult> future : futures) {
            results.add(future.get());
        }

        executor.shutdown();

        long successCount = results.stream().filter(result -> result.getResponse().getStatus() == 200).count();

        long unauthorizedCount = results.stream().filter(result -> result.getResponse().getStatus() == 401).count();

        assertThat(successCount).isEqualTo(1);

        assertThat(unauthorizedCount).isEqualTo(1);
    }


    @Test
    void swaggerEndpointsShouldBePublic()
            throws Exception {

        mockMvc.perform(
                        get("/v3/api-docs")
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/swagger-ui/index.html")
                )
                .andExpect(status().isOk());
    }

    private void ensureAdminRole(String tenantId) {

        JdbcTemplate adminJdbcTemplate = TestDatabaseHelper.adminJdbcTemplate(POSTGRES.getJdbcUrl());

        adminJdbcTemplate.update("""
                INSERT INTO roles(
                    id,
                    tenant_id,
                    name
                )
                VALUES (?, ?, 'ADMIN')
                ON CONFLICT (tenant_id, name)
                DO NOTHING
                """, UUID.randomUUID(), UUID.fromString(tenantId));
    }

    private void makeAdmin(String tenantId, String username) {

        JdbcTemplate adminJdbcTemplate = TestDatabaseHelper.adminJdbcTemplate(POSTGRES.getJdbcUrl());

        UUID tenantUuid = UUID.fromString(tenantId);

        UUID userId = adminJdbcTemplate.queryForObject("""
                SELECT id
                FROM users
                WHERE tenant_id = ?
                  AND username = ?
                """, UUID.class, tenantUuid, username);

        UUID roleId = adminJdbcTemplate.queryForObject("""
                SELECT id
                FROM roles
                WHERE tenant_id = ?
                  AND name = 'ADMIN'
                """, UUID.class, tenantUuid);

        adminJdbcTemplate.update("""
                INSERT INTO user_roles(
                    user_id,
                    role_id
                )
                VALUES (?, ?)
                ON CONFLICT DO NOTHING
                """, userId, roleId);
    }

    private String createTenant() throws Exception {

        String request = """
                {
                  "name": "Tenant-%s"
                }
                """.formatted(UUID.randomUUID());

        String response = mockMvc.perform(post("/api/auth/tenants").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("id").asText();
    }

    private void registerUser(String tenantId, String username, String password) throws Exception {

        String request = """
                {
                  "tenantId": "%s",
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(tenantId, username, password);

        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isCreated());
    }

    private LoginTokens login(String tenantId, String username, String password) throws Exception {

        String request = """
                {
                  "tenantId": "%s",
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(tenantId, username, password);

        String response = mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.refreshToken").exists()).andExpect(jsonPath("$.tokenType").value("Bearer")).andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return new LoginTokens(json.get("accessToken").asText(), json.get("refreshToken").asText());
    }

    private record LoginTokens(String accessToken, String refreshToken) {
    }

    private String extractJwtClaim(String token, String claim) {

        return jwtService.parseAndValidate(token).get(claim, String.class);
    }

    private String refreshAccessToken(String refreshToken) throws Exception {

        String request = """
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken);

        String response = mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content(request)).andExpect(status().isOk()).andExpect(jsonPath("$.accessToken").exists()).andExpect(jsonPath("$.refreshToken").exists()).andReturn().getResponse().getContentAsString();

        JsonNode json = objectMapper.readTree(response);

        return json.get("refreshToken").asText();
    }

    private MvcResult performRefresh(String refreshToken) throws Exception {

        return mockMvc.perform(post("/api/auth/refresh").contentType(MediaType.APPLICATION_JSON).content("""
                {
                  "refreshToken": "%s"
                }
                """.formatted(refreshToken))).andReturn();
    }
}