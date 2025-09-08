package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.common.PageResponse;
import co.com.pragma.model.exceptions.*;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loanreviewitem.LoanReviewItem;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.enums.DocumentType;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import co.com.pragma.model.userbasicinfo.UserBasicInfo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

    @Mock
    private LoanTypeRepository loanTypeRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private UserRestConsumerPort userRestConsumer;

    @Mock
    private LoggerPort logger;


    private LoanApplicationUseCase loanApplicationUseCase;

    @BeforeEach
    void setup() {
        loanApplicationUseCase = new LoanApplicationUseCase(loanTypeRepository, statusRepository, loanApplicationRepository,
                userRestConsumer, logger);
    }

    @Test
    void shouldSaveLoanApplication() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        User user = userMock();
        LoanType loanType = loanTypeMock();
        Status status = statusMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
        when(loanApplicationRepository.existsUserAndLoanTypeAndStatus(any(String.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(false));
        when(loanApplicationRepository.saveLoanApplication(any(LoanApplication.class))).thenReturn(Mono.just(loanApp));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(savedLoanApp ->
                        savedLoanApp.getId().equals(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6")) &&
                                savedLoanApp.getStatus().getId().equals(UUID.fromString("70e91cdf-07dd-404e-bbc9-27646f3030f9")))
                .verifyComplete();
    }

    @Test
    void shouldReturnErrorWhenUserNotFound() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.empty());

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof UserNotFoundException &&
                                throwable.getMessage().equals("Usuario no encontrado"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenUserWantsToCreateLoanInAnotherPerson() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        loanApp.setDocumentNumber("1111");
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        User user = userMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AccessDeniedException &&
                                throwable.getMessage().equals("No puedes crear préstamos a nombre de otro usuario"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenLoanTypeNotFound() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        User user = userMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.empty());

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof LoanTypeNotFoundException &&
                                throwable.getMessage().equals("Tipo de prestamo no encontrado"))
                .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"10000.00", "50000.00", "6000000.00"})
    void shouldReturnErrorWhenAmountIsInvalid(String amount) {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        loanApp.setAmount(new BigDecimal(amount));
        LoanType loanType = loanTypeMock();
        User user = userMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof AmountOutOfRangeException &&
                                throwable.getMessage().equals("El monto no es valido"))
                .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"-1", "25", "30"})
    void shouldReturnErrorWhenTermIsInvalid(Integer termMont) {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        loanApp.setTermMonth(termMont);
        LoanType loanType = loanTypeMock();
        User user = userMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof TermOutOfRangeException &&
                                throwable.getMessage().equals("El plazo establecido no es valido"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenStatusNotFound() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        User user = userMock();
        LoanType loanType = loanTypeMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.empty());

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof StatusNotFoundException &&
                                throwable.getMessage().equals("Estado del prestamo no encontrado"))
                .verify();
    }

    @Test
    void shouldReturnErrorWhenExistsUserAndLoanTypeAndStatus() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        String emailFromToke = "pepe@example.com";
        String token = tokenMock();
        User user = userMock();
        LoanType loanType = loanTypeMock();
        Status status = statusMock();

        // Mock reactive repositories
        when(userRestConsumer.findUserByEmail(any(String.class), any(String.class))).thenReturn(Mono.just(user));
        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
        when(loanApplicationRepository.existsUserAndLoanTypeAndStatus(any(String.class), any(UUID.class), any(UUID.class)))
                .thenReturn(Mono.just(true));

        // Act
        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp, emailFromToke, token);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof LoanRequestStatusAndTypeMismatchException &&
                                throwable.getMessage().equals("El usuario ya cuenta con una solicitud de préstamo en " +
                                        "proceso del mismo tipo"))
                .verify();
    }

    @Test
    void shouldReturnLoanApplicationsForReviewSuccess() {
        // Arrange
        List<String> statuses = statusesMock();
        int page = 0;
        int size = 2;
        String token = tokenMock();
        LoanReviewItem loanReviewItem = loanReviewItemMock();
        UserBasicInfo userBasic = userBasicInfoMock();

        when(loanApplicationRepository.countLoanApplicationByStatusesIn(any(List.class))).thenReturn(Mono.just(2L));
        when(loanApplicationRepository.findLoanApplicationWithDetails(any(List.class), anyInt(), anyInt()))
                .thenReturn(Flux.just(loanReviewItem));
        when(userRestConsumer.findUsersByBatchEmails(any(List.class), any(String.class))).thenReturn(Flux.just(userBasic));

        // Act
        Mono<PageResponse<LoanReviewItem>> result = loanApplicationUseCase.getLoanApplicationsForReview(statuses, page, size, token);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(pageResponse ->
                        pageResponse.getTotalElements() == 2 &&
                                !pageResponse.getContent().isEmpty())
                .verifyComplete();
    }

    @Test
    void shouldReturnLoanApplicationsForReviewEmptySuccess() {
        // Arrange
        List<String> statuses = statusesMock();
        int page = 0;
        int size = 2;
        String token = tokenMock();

        when(loanApplicationRepository.countLoanApplicationByStatusesIn(any(List.class))).thenReturn(Mono.just(0L));
        when(loanApplicationRepository.findLoanApplicationWithDetails(any(List.class), anyInt(), anyInt()))
                .thenReturn(Flux.empty());

        // Act
        Mono<PageResponse<LoanReviewItem>> result = loanApplicationUseCase.getLoanApplicationsForReview(statuses, page, size, token);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(pageResponse ->
                        pageResponse.getTotalElements() == 0 &&
                                pageResponse.getContent().isEmpty())
                .verifyComplete();
    }


    // ------------------------------------------------ Mocks ------------------------------------------------
    private LoanApplication loanApplicationMock() {
        return LoanApplication.builder()
                .id(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6"))
                .documentNumber("123456789")
                .amount(new BigDecimal("650000.00"))
                .termMonth(12)
                .email("pepe@example.com")
                .loanType(loanTypeMock())
                .status(statusMock())
                .build();
    }

    private LoanType loanTypeMock() {
        return LoanType.builder()
                .id(UUID.fromString("5060d735-10c0-4f27-bb39-76a15de8cf5c"))
                .name("MICROCREDIT")
                .amountMin(new BigDecimal("100000.00"))
                .amountMax(new BigDecimal("5000000.00"))
                .termMonthMin(1)
                .termMonthMax(24)
                .interestRate(new BigDecimal("0.0250"))
                .automaticValidation(true)
                .build();
    }

    private Status statusMock() {
        return Status.builder()
                .id(UUID.fromString("70e91cdf-07dd-404e-bbc9-27646f3030f9"))
                .name("PENDING_REVIEW")
                .description("The request has been submitted and is waiting to be reviewed automatically or manually.")
                .build();
    }

    private User userMock() {
        return User.builder()
                .id(UUID.fromString("cd0aa3bf-628b-4f71-ac8f-93a280176353"))
                .name("Pepe")
                .surname("Perez")
                .email("pepe@example.com")
                .documentType(DocumentType.CC)
                .documentNumber("123456789")
                .address("Cr 5 - 345")
                .phoneNumber("3124367589")
                .baseSalary(2200000)
                .build();
    }

    private String tokenMock() {
        return "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBleGFtcGxlLmNvbSIsImlzcyI6ImF1dGhlbnRpY2F0aW9uLW1zdmMiLpYXQiOjE3" +
                "NTY4NTg3NjksImV4cCI6MTc1Njg1OTY2OSwiZW1haWwiOiJhZG1pbkBleGFtcGxlLmNvbSIsInJvbGUiOiJBRE1JTiJ9." +
                "xPetZo8ZwDf4Z5rs788DW42Uq0JALHTcoewXS1izqw";
    }

    private List<String> statusesMock() {
        return List.of("PENDING_REVIEW", "REJECTED", "MANUAL_REVIEW");
    }

    private LoanReviewItem loanReviewItemMock() {
        return LoanReviewItem.builder()
                .amount(new BigDecimal("600000.00"))
                .termMonth(10)
                .email("pepe@example.com")
                .fullName("Pepe Perez")
                .loanTypeName("MICROCREDIT")
                .interestRate(new BigDecimal("0.0250"))
                .statusName("MANUAL_REVIEW")
                .baseSalary(2200000)
                .totalMonthlyDebtApprovedApplications(new BigDecimal("200000"))
                .build();
    }

    private UserBasicInfo userBasicInfoMock() {
        return UserBasicInfo.builder()
                .name("Pepe")
                .surname("Perez")
                .email("pepe@example.com")
                .baseSalary(2200000)
                .build();
    }

}