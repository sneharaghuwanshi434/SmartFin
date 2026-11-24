
package com.smartfin.smartfin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class AuthIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;


    @Test
    void shouldRegisterUserSuccessfully() {
        String url = "http://localhost:" + port + "/api/auth/register";

        Map<String, String> request = new HashMap<>();
        request.put("fullName", "Integration Test User");
        request.put("email", "integration" + UUID.randomUUID() + "@gmail.com");
        request.put("password", "Test@12345");

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }
    @Test
    void shouldLoginUserSuccessfully() {

        String registerUrl = "http://localhost:" + port + "/api/auth/register";
        String loginUrl = "http://localhost:" + port + "/api/auth/login";

        String email = "login-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register user first
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Login Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(registerUrl, registerRequest, Map.class);

        // Login using registered credentials
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody().get("accessToken"));
        assertEquals("Bearer", response.getBody().get("tokenType"));
    }
    @Test
    void shouldRejectInvalidLogin() {

        String url = "http://localhost:" + port + "/api/auth/login";

        Map<String, String> request = new HashMap<>();
        request.put("email", "wronguser@gmail.com");
        request.put("password", "WrongPassword@123");

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, request, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldAccessProfileWithValidToken() {

        String registerUrl = "http://localhost:" + port + "/api/auth/register";
        String loginUrl = "http://localhost:" + port + "/api/auth/login";
        String profileUrl = "http://localhost:" + port + "/api/auth/profile";

        String email = "profile-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Profile Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(registerUrl, registerRequest, Map.class);

        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        org.springframework.http.HttpHeaders headers =
                new org.springframework.http.HttpHeaders();

        headers.setBearerAuth(token);

        org.springframework.http.HttpEntity<Void> entity =
                new org.springframework.http.HttpEntity<>(headers);

        ResponseEntity<Map> profileResponse =
                restTemplate.exchange(
                        profileUrl,
                        org.springframework.http.HttpMethod.GET,
                        entity,
                        Map.class
                );

        assertEquals(HttpStatus.OK, profileResponse.getStatusCode());
        assertEquals(email, profileResponse.getBody().get("email"));
    }

    @Test
    void shouldRejectProfileWithoutToken() {

        String url = "http://localhost:" + port + "/api/auth/profile";

        ResponseEntity<Map> response =
                restTemplate.getForEntity(url, Map.class);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void shouldRejectInvalidJwtToken() {

        String url = "http://localhost:" + port + "/api/auth/profile";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("invalid.fake.jwt.token");

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }
    @Test
    void shouldCreateBankAccountSuccessfully() {

        String registerUrl = "http://localhost:" + port + "/api/auth/register";
        String loginUrl = "http://localhost:" + port + "/api/auth/login";
        String accountUrl = "http://localhost:" + port + "/api/accounts";

        String email = "account-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register user
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Account Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(registerUrl, registerRequest, Map.class);

        // Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        // JWT headers
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);

        // Account request
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        HttpEntity<Map<String, String>> entity =
                new HttpEntity<>(accountRequest, headers);

        ResponseEntity<Map> response = restTemplate.postForEntity(
                accountUrl,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody().get("accountNumber"));
        assertEquals("SAVINGS", response.getBody().get("accountType"));
        assertEquals("INR", response.getBody().get("currency"));
        assertEquals("ACTIVE", response.getBody().get("status"));
    }

    @Test
    void shouldGetMyAccountsSuccessfully() {

        String registerUrl = "http://localhost:" + port + "/api/auth/register";
        String loginUrl = "http://localhost:" + port + "/api/auth/login";
        String accountUrl = "http://localhost:" + port + "/api/accounts";

        String email = "accounts-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Accounts Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(registerUrl, registerRequest, Map.class);

        // Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        // Create account
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        HttpEntity<Map<String, String>> createEntity =
                new HttpEntity<>(accountRequest, headers);

        restTemplate.postForEntity(accountUrl, createEntity, Map.class);

        // Get accounts
        HttpEntity<Void> getEntity = new HttpEntity<>(headers);

        ResponseEntity<List> response = restTemplate.exchange(
                accountUrl,
                HttpMethod.GET,
                getEntity,
                List.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void shouldGetAccountDetailsSuccessfully() {

        String registerUrl = "http://localhost:" + port + "/api/auth/register";
        String loginUrl = "http://localhost:" + port + "/api/auth/login";
        String accountUrl = "http://localhost:" + port + "/api/accounts";

        String email = "detail-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Detail Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(registerUrl, registerRequest, Map.class);

        // Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> loginResponse =
                restTemplate.postForEntity(loginUrl, loginRequest, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        HttpEntity<Map<String, String>> createEntity =
                new HttpEntity<>(accountRequest, headers);

        ResponseEntity<Map> createResponse =
                restTemplate.postForEntity(accountUrl, createEntity, Map.class);

        String accountNumber =
                (String) createResponse.getBody().get("accountNumber");

        // Get account details
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                accountUrl + "/" + accountNumber,
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(accountNumber, response.getBody().get("accountNumber"));
        assertEquals("SAVINGS", response.getBody().get("accountType"));
        assertEquals("INR", response.getBody().get("currency"));
        assertEquals("ACTIVE", response.getBody().get("status"));
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUsersAccount() {

        String baseUrl = "http://localhost:" + port;
        String registerUrl = baseUrl + "/api/auth/register";
        String loginUrl = baseUrl + "/api/auth/login";
        String accountUrl = baseUrl + "/api/accounts";

        // Create User A
        String emailA = "userA-" + UUID.randomUUID() + "@gmail.com";
        String emailB = "userB-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        Map<String, String> registerA = new HashMap<>();
        registerA.put("fullName", "User A");
        registerA.put("email", emailA);
        registerA.put("password", password);

        restTemplate.postForEntity(registerUrl, registerA, Map.class);

        // Login User A
        Map<String, String> loginA = new HashMap<>();
        loginA.put("email", emailA);
        loginA.put("password", password);

        ResponseEntity<Map> loginResponseA =
                restTemplate.postForEntity(loginUrl, loginA, Map.class);

        String tokenA = (String) loginResponseA.getBody().get("accessToken");

        // Create account for User A
        HttpHeaders headersA = new HttpHeaders();
        headersA.setBearerAuth(tokenA);
        headersA.setContentType(MediaType.APPLICATION_JSON);

        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        HttpEntity<Map<String, String>> createEntity =
                new HttpEntity<>(accountRequest, headersA);

        ResponseEntity<Map> accountResponse =
                restTemplate.postForEntity(accountUrl, createEntity, Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Create User B
        Map<String, String> registerB = new HashMap<>();
        registerB.put("fullName", "User B");
        registerB.put("email", emailB);
        registerB.put("password", password);

        restTemplate.postForEntity(registerUrl, registerB, Map.class);

        // Login User B
        Map<String, String> loginB = new HashMap<>();
        loginB.put("email", emailB);
        loginB.put("password", password);

        ResponseEntity<Map> loginResponseB =
                restTemplate.postForEntity(loginUrl, loginB, Map.class);

        String tokenB = (String) loginResponseB.getBody().get("accessToken");

        // User B tries to access User A's account
        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(tokenB);

        HttpEntity<Void> entity = new HttpEntity<>(headersB);

        ResponseEntity<Map> response = restTemplate.exchange(
                accountUrl + "/" + accountNumber,
                HttpMethod.GET,
                entity,
                Map.class
        );

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void shouldDepositMoneySuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "deposit-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> registerRequest = new HashMap<>();
        registerRequest.put("fullName", "Deposit Test User");
        registerRequest.put("email", email);
        registerRequest.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerRequest,
                Map.class
        );

        // Login
        Map<String, String> loginRequest = new HashMap<>();
        loginRequest.put("email", email);
        loginRequest.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginRequest,
                Map.class
        );

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        HttpEntity<Map<String, String>> accountEntity =
                new HttpEntity<>(accountRequest, headers);

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                accountEntity,
                Map.class
        );

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> depositRequest = new HashMap<>();
        depositRequest.put("accountNumber", accountNumber);
        depositRequest.put("amount", 1000);
        depositRequest.put("idempotencyKey", UUID.randomUUID().toString());

        HttpEntity<Map<String, Object>> depositEntity =
                new HttpEntity<>(depositRequest, headers);

        ResponseEntity<Map> depositResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                depositEntity,
                Map.class
        );

        assertEquals(HttpStatus.OK, depositResponse.getStatusCode());
        assertEquals("Deposit successful",
                depositResponse.getBody().get("message"));
        assertEquals("DEPOSIT",
                depositResponse.getBody().get("transactionType"));
        assertEquals("SUCCESS",
                depositResponse.getBody().get("status"));

        // Verify updated account balance
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(0,
                new java.math.BigDecimal(
                        balanceResponse.getBody().get("balance").toString()
                ).compareTo(new java.math.BigDecimal("1000.00")));
    }

    @Test
    void shouldRejectDuplicateDeposit() {

        String baseUrl = "http://localhost:" + port;
        String email = "duplicate-deposit-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Duplicate Deposit User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Same deposit request and same idempotency key
        String key = UUID.randomUUID().toString();

        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", key);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(deposit, headers);

        ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit", entity, Map.class);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit", entity, Map.class);

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());

        // Verify balance
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(0, balance.compareTo(new java.math.BigDecimal("1000.00")));
    }

    @Test
    void shouldWithdrawMoneySuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "withdraw-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Withdraw Test User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000 first
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Withdraw ₹300
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 300);
        withdrawal.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> withdrawResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw",
                new HttpEntity<>(withdrawal, headers),
                Map.class);

        assertEquals(HttpStatus.OK, withdrawResponse.getStatusCode());
        assertEquals("Withdrawal successful",
                withdrawResponse.getBody().get("message"));
        assertEquals("WITHDRAWAL",
                withdrawResponse.getBody().get("transactionType"));
        assertEquals("SUCCESS",
                withdrawResponse.getBody().get("status"));

        // Verify final balance
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(0, balance.compareTo(new java.math.BigDecimal("700.00")));
    }

    @Test
    void shouldRejectDuplicateWithdrawal() {

        String baseUrl = "http://localhost:" + port;
        String email = "duplicate-withdraw-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Duplicate Withdraw User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Prepare withdrawal request
        String key = UUID.randomUUID().toString();

        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 300);
        withdrawal.put("idempotencyKey", key);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(withdrawal, headers);

        // Send same request twice
        ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw", entity, Map.class);

        ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw", entity, Map.class);

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());

        // Verify balance remains ₹700
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(0, balance.compareTo(new java.math.BigDecimal("700.00")));
    }

    @Test
    void shouldRejectWithdrawalWithInsufficientBalance() {

        String baseUrl = "http://localhost:" + port;
        String email = "insufficient-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Insufficient Balance User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹500
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 500);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Attempt to withdraw ₹800
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 800);
        withdrawal.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> withdrawResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw",
                new HttpEntity<>(withdrawal, headers),
                Map.class);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                withdrawResponse.getStatusCode());

        // Verify balance remains ₹500
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(
                0,
                balance.compareTo(new java.math.BigDecimal("500.00")));
    }
    @Test
    void shouldTransferMoneySuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "transfer-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Transfer Test User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create sender account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> senderAccountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String senderAccountNumber =
                (String) senderAccountResponse.getBody().get("accountNumber");

        // Create receiver account
        ResponseEntity<Map> receiverAccountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String receiverAccountNumber =
                (String) receiverAccountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000 into sender
        Map<String, Object> senderDeposit = new HashMap<>();
        senderDeposit.put("accountNumber", senderAccountNumber);
        senderDeposit.put("amount", 1000);
        senderDeposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(senderDeposit, headers),
                Map.class);

        // Deposit ₹500 into receiver
        Map<String, Object> receiverDeposit = new HashMap<>();
        receiverDeposit.put("accountNumber", receiverAccountNumber);
        receiverDeposit.put("amount", 500);
        receiverDeposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(receiverDeposit, headers),
                Map.class);

        // Transfer ₹300
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("sourceAccountNumber", senderAccountNumber);
        transfer.put("destinationAccountNumber", receiverAccountNumber);
        transfer.put("amount", 300);
        transfer.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer",
                new HttpEntity<>(transfer, headers),
                Map.class);

        assertEquals(HttpStatus.OK, transferResponse.getStatusCode());
        assertEquals("Transfer successful",
                transferResponse.getBody().get("message"));
        assertEquals("TRANSFER",
                transferResponse.getBody().get("transactionType"));
        assertEquals("SUCCESS",
                transferResponse.getBody().get("status"));

        // Verify sender balance
        ResponseEntity<Map> senderBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + senderAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal senderBalance = new java.math.BigDecimal(
                senderBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, senderBalance.compareTo(
                new java.math.BigDecimal("700.00")));

        // Verify receiver balance
        ResponseEntity<Map> receiverBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + receiverAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal receiverBalance = new java.math.BigDecimal(
                receiverBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, receiverBalance.compareTo(
                new java.math.BigDecimal("800.00")));
    }

    @Test
    void shouldRejectDuplicateTransfer() {

        String baseUrl = "http://localhost:" + port;
        String email = "duplicate-transfer-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Duplicate Transfer User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create sender account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> senderResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String senderAccountNumber =
                (String) senderResponse.getBody().get("accountNumber");

        // Create receiver account
        ResponseEntity<Map> receiverResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String receiverAccountNumber =
                (String) receiverResponse.getBody().get("accountNumber");

        // Deposit ₹1,000 into sender
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", senderAccountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Same transfer request and same idempotency key
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("sourceAccountNumber", senderAccountNumber);
        transfer.put("destinationAccountNumber", receiverAccountNumber);
        transfer.put("amount", 300);

        String key = UUID.randomUUID().toString();
        transfer.put("idempotencyKey", key);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(transfer, headers);

        // First transfer
        ResponseEntity<Map> firstResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer", entity, Map.class);

        // Duplicate transfer
        ResponseEntity<Map> secondResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer", entity, Map.class);

        assertEquals(HttpStatus.OK, firstResponse.getStatusCode());
        assertEquals(HttpStatus.CONFLICT, secondResponse.getStatusCode());

        // Verify sender balance
        ResponseEntity<Map> senderBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + senderAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal senderBalance = new java.math.BigDecimal(
                senderBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, senderBalance.compareTo(
                new java.math.BigDecimal("700.00")));

        // Verify receiver balance
        ResponseEntity<Map> receiverBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + receiverAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal receiverBalance = new java.math.BigDecimal(
                receiverBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, receiverBalance.compareTo(
                new java.math.BigDecimal("300.00")));
    }
    @Test
    void shouldRejectTransferWithInsufficientBalance() {

        String baseUrl = "http://localhost:" + port;
        String email = "insufficient-transfer-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Insufficient Transfer User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create sender account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> senderResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String senderAccountNumber =
                (String) senderResponse.getBody().get("accountNumber");

        // Create receiver account
        ResponseEntity<Map> receiverResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String receiverAccountNumber =
                (String) receiverResponse.getBody().get("accountNumber");

        // Deposit ₹500 into sender
        Map<String, Object> senderDeposit = new HashMap<>();
        senderDeposit.put("accountNumber", senderAccountNumber);
        senderDeposit.put("amount", 500);
        senderDeposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(senderDeposit, headers),
                Map.class);

        // Deposit ₹200 into receiver
        Map<String, Object> receiverDeposit = new HashMap<>();
        receiverDeposit.put("accountNumber", receiverAccountNumber);
        receiverDeposit.put("amount", 200);
        receiverDeposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(receiverDeposit, headers),
                Map.class);

        // Attempt to transfer ₹800
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("sourceAccountNumber", senderAccountNumber);
        transfer.put("destinationAccountNumber", receiverAccountNumber);
        transfer.put("amount", 800);
        transfer.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer",
                new HttpEntity<>(transfer, headers),
                Map.class);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                transferResponse.getStatusCode());

        // Verify sender balance remains ₹500
        ResponseEntity<Map> senderBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + senderAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal senderBalance = new java.math.BigDecimal(
                senderBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, senderBalance.compareTo(
                new java.math.BigDecimal("500.00")));

        // Verify receiver balance remains ₹200
        ResponseEntity<Map> receiverBalanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + receiverAccountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal receiverBalance = new java.math.BigDecimal(
                receiverBalanceResponse.getBody().get("balance").toString());

        assertEquals(0, receiverBalance.compareTo(
                new java.math.BigDecimal("200.00")));
    }
    @Test
    void shouldRejectTransferToSameAccount() {

        String baseUrl = "http://localhost:" + port;
        String email = "same-account-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Same Account Test User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Attempt transfer to the same account
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("sourceAccountNumber", accountNumber);
        transfer.put("destinationAccountNumber", accountNumber);
        transfer.put("amount", 300);
        transfer.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer",
                new HttpEntity<>(transfer, headers),
                Map.class);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                transferResponse.getStatusCode());

        // Verify balance remains ₹1,000
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(
                0,
                balance.compareTo(new java.math.BigDecimal("1000.00")));
    }
    @Test
    void shouldRejectTransferFromAnotherUsersAccount() {

        String baseUrl = "http://localhost:" + port;
        String password = "Test@12345";

        // Register User A
        String emailA = "owner-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerA = new HashMap<>();
        registerA.put("fullName", "Account Owner");
        registerA.put("email", emailA);
        registerA.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", registerA, Map.class);

        // Login User A
        Map<String, String> loginA = new HashMap<>();
        loginA.put("email", emailA);
        loginA.put("password", password);

        ResponseEntity<Map> loginResponseA = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", loginA, Map.class);

        String tokenA =
                (String) loginResponseA.getBody().get("accessToken");

        HttpHeaders headersA = new HttpHeaders();
        headersA.setBearerAuth(tokenA);
        headersA.setContentType(MediaType.APPLICATION_JSON);

        // Create User A account
        Map<String, String> accountRequestA = new HashMap<>();
        accountRequestA.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponseA = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequestA, headersA),
                Map.class);

        String accountA =
                (String) accountResponseA.getBody().get("accountNumber");

        // Deposit ₹1,000 into User A account
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountA);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headersA),
                Map.class);

        // Register User B
        String emailB = "attacker-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerB = new HashMap<>();
        registerB.put("fullName", "Another User");
        registerB.put("email", emailB);
        registerB.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", registerB, Map.class);

        // Login User B
        Map<String, String> loginB = new HashMap<>();
        loginB.put("email", emailB);
        loginB.put("password", password);

        ResponseEntity<Map> loginResponseB = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", loginB, Map.class);

        String tokenB =
                (String) loginResponseB.getBody().get("accessToken");

        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(tokenB);
        headersB.setContentType(MediaType.APPLICATION_JSON);

        // Create User B's own account
        Map<String, String> accountRequestB = new HashMap<>();
        accountRequestB.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponseB = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequestB, headersB),
                Map.class);

        String accountB =
                (String) accountResponseB.getBody().get("accountNumber");

        // User B attempts to transfer from User A's account
        Map<String, Object> transfer = new HashMap<>();
        transfer.put("sourceAccountNumber", accountA);
        transfer.put("destinationAccountNumber", accountB);
        transfer.put("amount", 300);
        transfer.put("idempotencyKey", UUID.randomUUID().toString());

        ResponseEntity<Map> transferResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts/transfer",
                new HttpEntity<>(transfer, headersB),
                Map.class);

        assertEquals(
                HttpStatus.BAD_REQUEST,
                transferResponse.getStatusCode());

        // Verify User A's balance remains ₹1,000
        ResponseEntity<Map> balanceResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountA,
                HttpMethod.GET,
                new HttpEntity<>(headersA),
                Map.class);

        java.math.BigDecimal balance = new java.math.BigDecimal(
                balanceResponse.getBody().get("balance").toString());

        assertEquals(
                0,
                balance.compareTo(new java.math.BigDecimal("1000.00")));
    }
    @Test
    void shouldGetTransactionHistorySuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "history-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "History Test User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", register, Map.class);

        // Login
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", login, Map.class);

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class);

        // Withdraw ₹300
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 300);
        withdrawal.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw",
                new HttpEntity<>(withdrawal, headers),
                Map.class);

        // Get transaction history
        ResponseEntity<Map> historyResponse = restTemplate.exchange(
                baseUrl + "/api/transactions/history?page=0&size=5",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class);

        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());

        Map<String, Object> body = historyResponse.getBody();

        List<Map<String, Object>> content =
                (List<Map<String, Object>>) body.get("content");

        assertEquals(2, content.size());

        // Latest transaction should be withdrawal
        assertEquals("WITHDRAWAL",
                content.get(0).get("transactionType"));

        assertEquals("SUCCESS",
                content.get(0).get("status"));

        // Earlier transaction should be deposit
        assertEquals("DEPOSIT",
                content.get(1).get("transactionType"));

        assertEquals("SUCCESS",
                content.get(1).get("status"));

        // Verify pagination
        assertEquals(0, ((Number) body.get("number")).intValue());
        assertEquals(5, ((Number) body.get("size")).intValue());
        assertEquals(2, ((Number) body.get("totalElements")).intValue());
    }
    @Test
    void shouldNotShowOtherUsersTransactionHistory() {

        String baseUrl = "http://localhost:" + port;
        String password = "Test@12345";

        // Register User A
        String emailA = "history-a-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerA = new HashMap<>();
        registerA.put("fullName", "History User A");
        registerA.put("email", emailA);
        registerA.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", registerA, Map.class);

        // Login User A
        Map<String, String> loginA = new HashMap<>();
        loginA.put("email", emailA);
        loginA.put("password", password);

        ResponseEntity<Map> loginResponseA = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", loginA, Map.class);

        String tokenA =
                (String) loginResponseA.getBody().get("accessToken");

        HttpHeaders headersA = new HttpHeaders();
        headersA.setBearerAuth(tokenA);
        headersA.setContentType(MediaType.APPLICATION_JSON);

        // Create User A account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headersA),
                Map.class);

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // User A deposits ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headersA),
                Map.class);

        // Register User B
        String emailB = "history-b-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerB = new HashMap<>();
        registerB.put("fullName", "History User B");
        registerB.put("email", emailB);
        registerB.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register", registerB, Map.class);

        // Login User B
        Map<String, String> loginB = new HashMap<>();
        loginB.put("email", emailB);
        loginB.put("password", password);

        ResponseEntity<Map> loginResponseB = restTemplate.postForEntity(
                baseUrl + "/api/auth/login", loginB, Map.class);

        String tokenB =
                (String) loginResponseB.getBody().get("accessToken");

        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(tokenB);

        // User B checks transaction history
        ResponseEntity<Map> historyResponse = restTemplate.exchange(
                baseUrl + "/api/transactions/history?page=0&size=5",
                HttpMethod.GET,
                new HttpEntity<>(headersB),
                Map.class);

        assertEquals(HttpStatus.OK, historyResponse.getStatusCode());

        Map<String, Object> body = historyResponse.getBody();

        assertEquals(
                0,
                ((Number) body.get("totalElements")).intValue());

        List<Map<String, Object>> content =
                (List<Map<String, Object>>) body.get("content");

        assertEquals(0, content.size());
    }


    @Test
    void shouldGetAccountStatementSuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "statement-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register user
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Statement User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                register,
                Map.class
        );

        // Login user
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                login,
                Map.class
        );

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class
        );

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class
        );

        // Withdraw ₹300
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 300);
        withdrawal.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw",
                new HttpEntity<>(withdrawal, headers),
                Map.class
        );

        // Get account statement
        ResponseEntity<List> statementResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber + "/statement",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                List.class
        );

        assertEquals(HttpStatus.OK, statementResponse.getStatusCode());

        List<Map<String, Object>> statement = statementResponse.getBody();

        assertNotNull(statement);
        assertEquals(2, statement.size());

        // Latest transaction should be withdrawal
        assertEquals("DEBIT", statement.get(0).get("entryType"));

        assertEquals(
                0,
                new BigDecimal(statement.get(0).get("amount").toString())
                        .compareTo(new BigDecimal("300.00"))
        );

        // Earlier transaction should be deposit
        assertEquals("CREDIT", statement.get(1).get("entryType"));

        assertEquals(
                0,
                new BigDecimal(statement.get(1).get("amount").toString())
                        .compareTo(new BigDecimal("1000.00"))
        );

        // Balance after withdrawal = ₹700
        // Statement calculates balance before each transaction in reverse
        assertEquals(
                0,
                new BigDecimal(statement.get(0).get("balance").toString())
                        .compareTo(new BigDecimal("1000.00"))
        );

        // Balance before deposit = ₹0
        assertEquals(
                0,
                new BigDecimal(statement.get(1).get("balance").toString())
                        .compareTo(new BigDecimal("0.00"))
        );
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUsersAccountStatement() {

        String baseUrl = "http://localhost:" + port;
        String password = "Test@12345";

        // Register User A
        String emailA = "statement-a-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerA = new HashMap<>();
        registerA.put("fullName", "Statement User A");
        registerA.put("email", emailA);
        registerA.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerA,
                Map.class
        );

        // Login User A
        Map<String, String> loginA = new HashMap<>();
        loginA.put("email", emailA);
        loginA.put("password", password);

        ResponseEntity<Map> loginResponseA = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginA,
                Map.class
        );

        String tokenA =
                (String) loginResponseA.getBody().get("accessToken");

        HttpHeaders headersA = new HttpHeaders();
        headersA.setBearerAuth(tokenA);
        headersA.setContentType(MediaType.APPLICATION_JSON);

        // Create User A account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headersA),
                Map.class
        );

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // User A deposits money
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headersA),
                Map.class
        );

        // Register User B
        String emailB = "statement-b-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerB = new HashMap<>();
        registerB.put("fullName", "Statement User B");
        registerB.put("email", emailB);
        registerB.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerB,
                Map.class
        );

        // Login User B
        Map<String, String> loginB = new HashMap<>();
        loginB.put("email", emailB);
        loginB.put("password", password);

        ResponseEntity<Map> loginResponseB = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginB,
                Map.class
        );

        String tokenB =
                (String) loginResponseB.getBody().get("accessToken");

        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(tokenB);

        // User B tries to access User A's account statement
        ResponseEntity<String> statementResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber + "/statement",
                HttpMethod.GET,
                new HttpEntity<>(headersB),
                String.class
        );

        // Access should be denied
        assertEquals(
                HttpStatus.NOT_FOUND,
                statementResponse.getStatusCode()
        );
    }

    @Test
    void shouldReconcileAccountSuccessfully() {

        String baseUrl = "http://localhost:" + port;
        String email = "reconcile-" + UUID.randomUUID() + "@gmail.com";
        String password = "Test@12345";

        // Register user
        Map<String, String> register = new HashMap<>();
        register.put("fullName", "Reconciliation User");
        register.put("email", email);
        register.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                register,
                Map.class
        );

        // Login user
        Map<String, String> login = new HashMap<>();
        login.put("email", email);
        login.put("password", password);

        ResponseEntity<Map> loginResponse = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                login,
                Map.class
        );

        String token = (String) loginResponse.getBody().get("accessToken");

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        headers.setContentType(MediaType.APPLICATION_JSON);

        // Create account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headers),
                Map.class
        );

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // Deposit ₹1,000
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headers),
                Map.class
        );

        // Withdraw ₹300
        Map<String, Object> withdrawal = new HashMap<>();
        withdrawal.put("accountNumber", accountNumber);
        withdrawal.put("amount", 300);
        withdrawal.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/withdraw",
                new HttpEntity<>(withdrawal, headers),
                Map.class
        );

        // Get reconciliation report
        ResponseEntity<Map> reconciliationResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber + "/reconciliation",
                HttpMethod.GET,
                new HttpEntity<>(headers),
                Map.class
        );

        assertEquals(
                HttpStatus.OK,
                reconciliationResponse.getStatusCode()
        );

        Map<String, Object> result = reconciliationResponse.getBody();

        assertNotNull(result);

        // Verify current balance
        assertEquals(
                0,
                new BigDecimal(result.get("currentBalance").toString())
                        .compareTo(new BigDecimal("700.00"))
        );

        // Verify total credits
        assertEquals(
                0,
                new BigDecimal(result.get("totalCredits").toString())
                        .compareTo(new BigDecimal("1000.00"))
        );

        // Verify total debits
        assertEquals(
                0,
                new BigDecimal(result.get("totalDebits").toString())
                        .compareTo(new BigDecimal("300.00"))
        );

        // Verify calculated balance
        assertEquals(
                0,
                new BigDecimal(result.get("calculatedBalance").toString())
                        .compareTo(new BigDecimal("700.00"))
        );

        assertEquals(true, result.get("reconciled"));
    }

    @Test
    void shouldNotAllowUserToAccessAnotherUsersReconciliation() {

        String baseUrl = "http://localhost:" + port;
        String password = "Test@12345";

        // Register User A
        String emailA = "reconcile-a-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerA = new HashMap<>();
        registerA.put("fullName", "Reconciliation User A");
        registerA.put("email", emailA);
        registerA.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerA,
                Map.class
        );

        // Login User A
        Map<String, String> loginA = new HashMap<>();
        loginA.put("email", emailA);
        loginA.put("password", password);

        ResponseEntity<Map> loginResponseA = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginA,
                Map.class
        );

        String tokenA =
                (String) loginResponseA.getBody().get("accessToken");

        HttpHeaders headersA = new HttpHeaders();
        headersA.setBearerAuth(tokenA);
        headersA.setContentType(MediaType.APPLICATION_JSON);

        // Create User A account
        Map<String, String> accountRequest = new HashMap<>();
        accountRequest.put("accountType", "SAVINGS");

        ResponseEntity<Map> accountResponse = restTemplate.postForEntity(
                baseUrl + "/api/accounts",
                new HttpEntity<>(accountRequest, headersA),
                Map.class
        );

        String accountNumber =
                (String) accountResponse.getBody().get("accountNumber");

        // User A deposits money
        Map<String, Object> deposit = new HashMap<>();
        deposit.put("accountNumber", accountNumber);
        deposit.put("amount", 1000);
        deposit.put("idempotencyKey", UUID.randomUUID().toString());

        restTemplate.postForEntity(
                baseUrl + "/api/accounts/deposit",
                new HttpEntity<>(deposit, headersA),
                Map.class
        );

        // Register User B
        String emailB = "reconcile-b-" + UUID.randomUUID() + "@gmail.com";

        Map<String, String> registerB = new HashMap<>();
        registerB.put("fullName", "Reconciliation User B");
        registerB.put("email", emailB);
        registerB.put("password", password);

        restTemplate.postForEntity(
                baseUrl + "/api/auth/register",
                registerB,
                Map.class
        );

        // Login User B
        Map<String, String> loginB = new HashMap<>();
        loginB.put("email", emailB);
        loginB.put("password", password);

        ResponseEntity<Map> loginResponseB = restTemplate.postForEntity(
                baseUrl + "/api/auth/login",
                loginB,
                Map.class
        );

        String tokenB =
                (String) loginResponseB.getBody().get("accessToken");

        HttpHeaders headersB = new HttpHeaders();
        headersB.setBearerAuth(tokenB);

        // User B tries to access User A's reconciliation report
        ResponseEntity<String> reconciliationResponse = restTemplate.exchange(
                baseUrl + "/api/accounts/" + accountNumber + "/reconciliation",
                HttpMethod.GET,
                new HttpEntity<>(headersB),
                String.class
        );

        // Access should be denied
        assertEquals(
                HttpStatus.NOT_FOUND,
                reconciliationResponse.getStatusCode()
        );
    }

}
