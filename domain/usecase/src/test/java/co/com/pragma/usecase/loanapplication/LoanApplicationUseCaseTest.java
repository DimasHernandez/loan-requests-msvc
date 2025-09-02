package co.com.pragma.usecase.loanapplication;

import co.com.pragma.model.exceptions.*;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.gateways.LoanApplicationRepository;
import co.com.pragma.model.loanapplication.gateways.LoggerPort;
import co.com.pragma.model.loantype.LoanType;
import co.com.pragma.model.loantype.gateways.LoanTypeRepository;
import co.com.pragma.model.status.Status;
import co.com.pragma.model.status.gateways.StatusRepository;
import co.com.pragma.model.user.User;
import co.com.pragma.model.user.enums.DocumentType;
import co.com.pragma.model.user.gateways.UserRestConsumerPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
class LoanApplicationUseCaseTest {

//    @Mock
//    private LoanTypeRepository loanTypeRepository;
//
//    @Mock
//    private StatusRepository statusRepository;
//
//    @Mock
//    private LoanApplicationRepository loanApplicationRepository;
//
//    @Mock
//    private UserRestConsumerPort userRestConsumer;
//
//    @Mock
//    private LoggerPort logger;
//
//
//    private LoanApplicationUseCase loanApplicationUseCase;
//
//    @BeforeEach
//    void setup() {
//        loanApplicationUseCase = new LoanApplicationUseCase(loanTypeRepository, statusRepository, loanApplicationRepository,
//                userRestConsumer, logger);
//    }
//
//    @Test
//    void shouldSaveLoanApplication() {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        User user = userMock();
//        LoanType loanType = loanTypeMock();
//        Status status = statusMock();
//
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
//        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
//        when(loanApplicationRepository.existsUserAndLoanTypeAndStatus(any(String.class), any(UUID.class), any(UUID.class)))
//                .thenReturn(Mono.just(false));
//        when(loanApplicationRepository.saveLoanApplication(any(LoanApplication.class))).thenReturn(Mono.just(loanApp));
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectNextMatches(savedLoanApp ->
//                        savedLoanApp.getId().equals(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6")) &&
//                                savedLoanApp.getStatus().getId().equals(UUID.fromString("70e91cdf-07dd-404e-bbc9-27646f3030f9")))
//                .verifyComplete();
//    }
//
//    @Test
//    void shouldReturnErrorWhenUserNotFound() {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.empty());
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof UserNotFoundException &&
//                                throwable.getMessage().equals("Usuario no encontrado"))
//                .verify();
//    }
//
//    @Test
//    void shouldReturnErrorWhenLoanTypeNotFound() {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        User user = userMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.empty());
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof LoanTypeNotFoundException &&
//                                throwable.getMessage().equals("Tipo de prestamo no encontrado"))
//                .verify();
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"10000.00", "50000.00", "6000000.00"})
//    void shouldReturnErrorWhenAmountIsInvalid(String amount) {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        loanApp.setAmount(new BigDecimal(amount));
//        LoanType loanType = loanTypeMock();
//        User user = userMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof AmountOutOfRangeException &&
//                                throwable.getMessage().equals("El monto no es valido"))
//                .verify();
//    }
//
//    @ParameterizedTest
//    @ValueSource(strings = {"-1", "25", "30"})
//    void shouldReturnErrorWhenTermIsInvalid(Integer termMont) {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        loanApp.setTermMonth(termMont);
//        LoanType loanType = loanTypeMock();
//        User user = userMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof TermOutOfRangeException &&
//                                throwable.getMessage().equals("El plazo establecido no es valido"))
//                .verify();
//    }
//
//    @Test
//    void shouldReturnErrorWhenStatusNotFound() {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        User user = userMock();
//        LoanType loanType = loanTypeMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
//        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.empty());
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof StatusNotFoundException &&
//                                throwable.getMessage().equals("Estado del prestamo no encontrado"))
//                .verify();
//    }
//
//    @Test
//    void shouldReturnErrorWhenExistsUserAndLoanTypeAndStatus() {
//        // Arrange
//        LoanApplication loanApp = loanApplicationMock();
//        User user = userMock();
//        LoanType loanType = loanTypeMock();
//        Status status = statusMock();
//
//        // Mock reactive repositories
//        when(userRestConsumer.findUserByDocumentIdentity(any(String.class))).thenReturn(Mono.just(user));
//        when(loanTypeRepository.findByName(any(String.class))).thenReturn(Mono.just(loanType));
//        when(statusRepository.findByName(any(String.class))).thenReturn(Mono.just(status));
//        when(loanApplicationRepository.existsUserAndLoanTypeAndStatus(any(String.class), any(UUID.class), any(UUID.class)))
//                .thenReturn(Mono.just(true));
//
//        // Act
//        Mono<LoanApplication> result = loanApplicationUseCase.saveLoanApplication(loanApp);
//
//        // Assert
//        StepVerifier.create(result)
//                .expectErrorMatches(throwable ->
//                        throwable instanceof LoanRequestStatusAndTypeMismatchException &&
//                                throwable.getMessage().equals("El usuario ya cuenta con una solicitud de préstamo en " +
//                                        "proceso del mismo tipo"))
//                .verify();
//    }
//
//
//    private LoanApplication loanApplicationMock() {
//        return LoanApplication.builder()
//                .id(UUID.fromString("e8c49caa-e6ab-4e58-a0a9-e221bc152ec6"))
//                .documentNumber("123456789")
//                .amount(new BigDecimal("650000.00"))
//                .termMonth(12)
//                .email("pepe@example.com")
//                .loanType(loanTypeMock())
//                .status(statusMock())
//                .build();
//    }
//
//    private LoanType loanTypeMock() {
//        return LoanType.builder()
//                .id(UUID.fromString("5060d735-10c0-4f27-bb39-76a15de8cf5c"))
//                .name("MICROCREDIT")
//                .amountMin(new BigDecimal("100000.00"))
//                .amountMax(new BigDecimal("5000000.00"))
//                .termMonthMin(1)
//                .termMonthMax(24)
//                .interestRate(new BigDecimal("0.0250"))
//                .automaticValidation(true)
//                .build();
//    }
//
//    private Status statusMock() {
//        return Status.builder()
//                .id(UUID.fromString("70e91cdf-07dd-404e-bbc9-27646f3030f9"))
//                .name("PENDING_REVIEW")
//                .description("The request has been submitted and is waiting to be reviewed automatically or manually.")
//                .build();
//    }
//
//    private User userMock() {
//        return User.builder()
//                .id(UUID.fromString("cd0aa3bf-628b-4f71-ac8f-93a280176353"))
//                .name("Pepe")
//                .surname("Perez")
//                .email("pepe@example.com")
//                .documentType(DocumentType.CC)
//                .documentNumber("123456789")
//                .address("Cr 5 - 345")
//                .phoneNumber("3124367589")
//                .baseSalary(2200000)
//                .build();
//    }

}