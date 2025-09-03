package co.com.pragma.consumer.user;


import co.com.pragma.consumer.exception.ServiceUnavailableException;
import co.com.pragma.consumer.user.mapper.UserMapper;
import co.com.pragma.model.exceptions.UserNotFoundException;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.enums.DocumentType;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.MediaType;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRestConsumerTest {

    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserRestConsumer userRestConsumer;

    private MockWebServer mockBackEnd;

    @BeforeEach
    void setUp() throws IOException {
        mockBackEnd = new MockWebServer();
        mockBackEnd.start();
        var webClient = WebClient.builder().baseUrl(mockBackEnd.url("/").toString()).build();
        userRestConsumer = new UserRestConsumer(webClient, userMapper);
    }

    @Test
    @DisplayName("Validate get user by email")
    void validateTestGetUserByEmail() {

        String email = "pepe@gmail.com";
        String token = tokenMock();
        User userMock = userMock();

        when(userMapper.toEntity(any(UserInfoResponse.class))).thenReturn(userMock);

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.OK.value())
                .setBody("{\"uuid\":\"2072048f-31f9-401c-9358-20195945aeb9\",\"firstName\":\"Pepe\",\"lastName\":\"Perez" +
                        "\",\"email\":\"pepe@gmail.com\",\"documentType\":\"CC\",\"documentNumber\":\"12345678944\",\"address" +
                        "\":\"Cra 7 Cll 4 - N° 34-47\",\"phoneNumber\":\"3158712655\",\"balance\":1000000}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectNextMatches(user -> user.getId().equals(UUID.fromString("2072048f-31f9-401c-9358-20195945aeb9"))
                        && user.getEmail().equals("pepe@gmail.com") && user.getDocumentNumber().equals("12345678944"))
                .verifyComplete();
    }

    @Test
    @DisplayName("User throw exception Not Found")
    void validateTestGetUserByEmailThrowExceptionNotFound() {

        String email = "pepe@gmail.com";
        String token = tokenMock();

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.NOT_FOUND.value())
                .setBody("{\"error\":\"Error de negocio\",\"code\":\"USR_004\",\"detail\":\"Usuario no encontrado\"}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("Usuario no encontrado"))
                .verify();
    }

    @Test
    @DisplayName("User throw exception Bad Request")
    void validateTestGetUserByEmailThrowExceptionBadRequest() {

        String email = "pepe@gmail.com";
        String token = tokenMock();

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.BAD_REQUEST.value())
                .setBody("{\"error\":\"Error de negocio\",\"code\":\"USR_006\",\"detail\":\"dato malformado\"}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof IllegalArgumentException)
                .verify();
    }

    @Test
    @DisplayName("User throw exception Runtime in 4xx error")
    void validateTestGetUserByEmailThrowExceptionRuntimeIn4xxError() {

        String email = "pepe@gmail.com";
        String token = tokenMock();

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.CONFLICT.value())
                .setBody("{\"error\":\"Error de negocio\",\"code\":\"USR_008\",\"detail\":\"Conlicto\"}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                                throwable.getMessage().equals("Conlicto"))
                .verify();
    }

    @Test
    @DisplayName("User throw exception Runtime in 5xx error")
    void validateTestGetUserByEmailThrowExceptionInRuntimeIn5xxError() {

        String email = "pepe@gmail.com";
        String token = tokenMock();

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .setBody("{\"error\":\"Error de negocio\",\"code\":\"GEN_005\",\"detail\":\"Error interno del servidor\"}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof RuntimeException &&
                        throwable.getMessage().equals("Error interno del servidor"))
                .verify();
    }

    @Test
    @DisplayName("User throw 5xx server")
    void validateTestGetUserByEmailThrow5xxServer() {

        String email = "pepe@gmail.com";
        String token = tokenMock();

        mockBackEnd.enqueue(new MockResponse()
                .setHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .setResponseCode(HttpStatus.SERVICE_UNAVAILABLE.value())
                .setBody("{\"error\":\"Error del servidor\",\"code\":\"GEN_003\",\"detail\":\"Servicio no disponible\"}"));
        var response = userRestConsumer.findUserByEmail(email, token);

        StepVerifier.create(response)
                .expectErrorMatches(throwable ->
                        throwable instanceof ServiceUnavailableException &&
                                throwable.getMessage().equals("Servicio no disponible"))
                .verify();
    }

    @AfterEach
    void tearDown() throws IOException {
        mockBackEnd.shutdown();
    }

    private String tokenMock() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlzcyI6ImF1dGhlbnRpY2F0aW9uLW1zdmMiLpYXQiOjE3" +
                "NTY4NTg3NjksImV4cCI6MTc1Njg1OTY2OSwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiJ9." +
                "xPetZo8ZwDf4Z5rs788DW42Uq0JALHTcoewXS1izqw";
    }

    private User userMock() {
        User user = new User();
        user.setId(UUID.fromString("2072048f-31f9-401c-9358-20195945aeb9"));
        user.setName("Pepe");
        user.setSurname("Perez");
        user.setEmail("pepe@gmail.com");
        user.setDocumentType(DocumentType.CC);
        user.setDocumentNumber("12345678944");
        user.setAddress("Cra 7 Cll 4 - N° 34-47");
        user.setPhoneNumber("3158712655");
        user.setBaseSalary(1000000);
        return user;
    }

}