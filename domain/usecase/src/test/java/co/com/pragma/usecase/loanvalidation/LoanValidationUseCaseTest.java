package co.com.pragma.usecase.loanvalidation;

import co.com.pragma.model.exceptions.StatusNotFoundException;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loanvalidation.events.request.ActiveLoanInfo;
import co.com.pragma.model.loanvalidation.events.request.LoanValidationRequest;
import co.com.pragma.model.loanvalidation.events.response.LoanValidationResponse;
import co.com.pragma.model.loanvalidation.events.response.PaymentPlanItem;
import co.com.pragma.model.loanvalidation.gateway.LoanValidationGateway;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.enums.DocumentType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LoanValidationUseCaseTest {

    @Mock
    private LoanValidationGateway loanValidationGateway;

    @Mock
    private LoanApplicationRepository loanApplicationRepository;

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private LoggerPort logger;

    @InjectMocks
    private LoanValidationUseCase loanValidationUseCase;

    @Test
    void shouldReturnLoanApplicationSuccessfully() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        User user = userMock();
        ActiveLoanInfo activeLoanInfo = activeLoanInfoMock();
        Status status = statusPendingValidationMock();
        String messageId = messageIdMock();

        // When reactive mock
        when(loanApplicationRepository.findLoanApplicationByDocumentNumberAndStatus(any(String.class), any(String.class)))
                .thenReturn(Flux.just(activeLoanInfo));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
        when(loanApplicationRepository.saveLoanApplication(any(LoanApplication.class))).thenReturn(Mono.just(loanApp));
        when(loanValidationGateway.sendToQueue(any(LoanValidationRequest.class))).thenReturn(Mono.just(messageId));

        // Act
        Mono<LoanApplication> result = loanValidationUseCase.enqueueLoanValidation(loanApp, user);

        // Assert
        StepVerifier.create(result)
                .expectNextMatches(loanAppResponse ->
                        loanAppResponse.getId().equals(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6")))
                .verifyComplete();
    }

    @Test
    void shouldReturnStatusNotFoundExceptionWhenLoanValidationFails() {
        // Arrange
        LoanApplication loanApp = loanApplicationMock();
        User user = userMock();
        ActiveLoanInfo activeLoanInfo = activeLoanInfoMock();

        // when reactive mock
        when(loanApplicationRepository.findLoanApplicationByDocumentNumberAndStatus(any(String.class), any(String.class)))
                .thenReturn(Flux.just(activeLoanInfo));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.empty());

        // Act
        Mono<LoanApplication> result = loanValidationUseCase.enqueueLoanValidation(loanApp, user);

        // Assert
        StepVerifier.create(result)
                .expectErrorMatches(throwable ->
                        throwable instanceof StatusNotFoundException &&
                                throwable.getMessage().equals("Estado del prestamo no encontrado"))
                .verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"APPROVED", "REJECTED", "MANUAL_REVIEW"})
    void shouldProcessTheMessageSuccessfully(String statusFinal) {
        // Arrange
        LoanValidationResponse loanValidationResponse = loanValidationResponseMock();
        LoanApplication loanApp = loanApplicationMock();
        Status status = statusFinalMock(statusFinal);

        // when reactive mocks
        when(loanApplicationRepository.findLoanApplicationById(any(UUID.class))).thenReturn(Mono.just(loanApp));
        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
        when(loanApplicationRepository.saveLoanApplication(any(LoanApplication.class))).thenReturn(Mono.just(loanApp));

        // Assert
        StepVerifier.create(loanValidationUseCase.processMessageResultQueue(loanValidationResponse))
                .verifyComplete();

        Mockito.verify(loanApplicationRepository, times(1)).findLoanApplicationById(any(UUID.class));
        Mockito.verify(statusRepository, times(1)).findByName(any(String.class));
        Mockito.verify(loanApplicationRepository, times(1)).saveLoanApplication(any(LoanApplication.class));

    }

    @ParameterizedTest
    @MethodSource("statusAndLoanId")
    void shouldProcessTheMessageWhenIntegrityDataIsNotValid(String statusFinal, UUID loanId) {
        // Arrange
        LoanValidationResponse loanValidationResponse = LoanValidationResponse.builder()
                .loanId(loanId)
                .result(statusFinal)
                .build();

        // Assert
        StepVerifier.create(loanValidationUseCase.processMessageResultQueue(loanValidationResponse))
                .verifyComplete();
    }

    @ParameterizedTest
    @ValueSource(strings = {"PENDING_REVIEW", "PENDING_VALIDATION"})
    void shouldProcessTheMessageWhenStatusIsNotValid(String statusFinal) {
        // Arrange
        LoanValidationResponse loanValidationResponse = LoanValidationResponse.builder()
                .loanId(UUID.fromString("d2327ccf-424e-4788-a553-57f5902b550e"))
                .result(statusFinal)
                .build();

        // Assert
        StepVerifier.create(loanValidationUseCase.processMessageResultQueue(loanValidationResponse))
                .verifyComplete();
    }

    static Stream<Arguments> statusAndLoanId() {
        return Stream.of(
                Arguments.of("   ", null),
                Arguments.of(null, null),
                Arguments.of("APPROVED", null)
        );
    }

    private LoanApplication loanApplicationMock() {
        return LoanApplication.builder()
                .id(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6"))
                .documentNumber("123456789")
                .amount(new BigDecimal("250000.00"))
                .termMonth(7)
                .email("pepe@example.com")
                .loanType(loanTypeWithIsAutomaticValidationMock())
                .status(statusPendingValidationMock())
                .build();
    }

    private LoanValidationResponse loanValidationResponseMock() {
        return LoanValidationResponse.builder()
                .loanId(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6"))
                .documentNumber("123456789")
                .fullName("Pepe Perez")
                .result("APPROVED")
                .paymentPlanItems(paymentPlanItemsMock())
                .build();
    }

    private Status statusFinalMock(String statusFinal) {
        return Status.builder()
                .id(UUID.randomUUID())
                .name(statusFinal)
                .description("Status request")
                .build();
    }

    private List<PaymentPlanItem> paymentPlanItemsMock() {
        return List.of(
                new PaymentPlanItem(1, new BigDecimal("33123.86"), new BigDecimal("6250.00"), new BigDecimal("39373.86")),
                new PaymentPlanItem(2, new BigDecimal("33951.95"), new BigDecimal("5421.90"), new BigDecimal("39373.86")),
                new PaymentPlanItem(3, new BigDecimal("34800.75"), new BigDecimal("4573.10"), new BigDecimal("39373.86")),
                new PaymentPlanItem(4, new BigDecimal("35670.77"), new BigDecimal("3703.09"), new BigDecimal("39373.86")),
                new PaymentPlanItem(5, new BigDecimal("36562.54"), new BigDecimal("2811.32"), new BigDecimal("39373.86")),
                new PaymentPlanItem(6, new BigDecimal("37476.60"), new BigDecimal("1897.25"), new BigDecimal("39373.86")),
                new PaymentPlanItem(7, new BigDecimal("38413.52"), new BigDecimal("960.34"), new BigDecimal("39373.86")));
    }

    private ActiveLoanInfo activeLoanInfoMock() {
        return ActiveLoanInfo.builder()
                .loanId(UUID.fromString("906091a0-ef46-490f-bef3-b0cee2aabb73"))
                .amount(new BigDecimal("250000.00"))
                .interestRate(new BigDecimal("0.0250"))
                .termMonth(6)
                .build();
    }

    private LoanType loanTypeWithIsAutomaticValidationMock() {
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

    private Status statusPendingValidationMock() {
        return Status.builder()
                .id(UUID.fromString("ff9afbd5-9305-4e49-b862-e9f197c88cf9"))
                .name("PENDING_VALIDATION")
                .description("The request remains pending validation while the lambda updates its status or is reviewed by an administrator/advisor.")
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

    private String messageIdMock() {
        return "433ce17f-9e77-495a-9b4f-2f1af353f84b";
    }

}