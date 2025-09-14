package co.com.pragma.api.errorhandler;

import co.com.pragma.model.exceptions.*;
import co.com.pragma.model.exceptions.enums.ErrorMessages;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

@Slf4j
@Component
@Order(-2)
public class GlobalHandler implements WebExceptionHandler {

    private static final String VALIDATION_FAILED = "Fallo validacion";
    private static final String BUSINESS_ERROR = "Error de negocio";

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        ServerHttpResponse response = exchange.getResponse();
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        if (ex instanceof ConstraintViolationException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, VALIDATION_FAILED, ErrorMessages.FIELD_EMPTY.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "ConstraintViolationException")));
        }

        if (ex instanceof WebExchangeBindException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.writeWith(
                    Mono.just(toBuffer(response, VALIDATION_FAILED, ErrorMessages.BAD_REQUEST.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.BAD_REQUEST,
                                    false, "WebExchangeBindException")));
        }

        if (ex instanceof UserNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.USER_NOT_FOUND.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.NOT_FOUND,
                                    false, "UserNotFoundException")));
        }

        if (ex instanceof LoanTypeNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.LOAN_TYPE_NOT_FOUND.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.NOT_FOUND,
                                    false, "LoanTypeNotFoundException")));
        }

        if (ex instanceof AmountOutOfRangeException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.AMOUNT_OUT_RANGE.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "AmountOutOfRangeException")));
        }

        if (ex instanceof TermOutOfRangeException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.TERM_OUT_RANGE.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "TermOutOfRangeException")));
        }

        if (ex instanceof LoanRequestStatusAndTypeMismatchException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.LOAN_REQUEST_STATUS_MISMATCH.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "LoanRequestStatusAndTypeMismatchException")));
        }

        if (ex instanceof StatusNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.STATUS_NOT_FOUND.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.NOT_FOUND,
                                    false, "StatusNotFoundException")));
        }

        if (ex instanceof AccessDeniedException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, VALIDATION_FAILED, ErrorMessages.ACCESS_DENIED.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "StatusNotFoundException")));
        }

        if (ex instanceof FinalStateNotAllowedException) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            return response.writeWith(
                    Mono.just(toBuffer(response, VALIDATION_FAILED, ErrorMessages.FINAL_STATE_NOT_ALLOWED.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.UNPROCESSABLE_ENTITY,
                                    false, "StatusNotAllowedException")));
        }

        if (ex instanceof LoanApplicationNotFoundException) {
            response.setStatusCode(HttpStatus.NOT_FOUND);
            return response.writeWith(
                    Mono.just(toBuffer(response, BUSINESS_ERROR, ErrorMessages.LOAN_APPLICATION_NOT_FOUND.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.NOT_FOUND,
                                    false, "LoanApplicationNotFoundException")));
        }

        if (ex instanceof IllegalArgumentException) {
            response.setStatusCode(HttpStatus.BAD_REQUEST);
            return response.writeWith(
                    Mono.just(toBuffer(response, VALIDATION_FAILED, ErrorMessages.BAD_REQUEST.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.BAD_REQUEST,
                                    false, "IllegalArgumentException")));
        }

        if (ex instanceof RuntimeException r) {
            String message = r.getMessage() != null ? r.getMessage() : "";

            // Validate errors from database
            System.out.println("Message error database or constrains: " + message);
        }

        if (ex instanceof WebClientRequestException) {
            response.setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
            return response.writeWith(
                    Mono.just(toBuffer(response, "Internal Server Error", ErrorMessages.SERVICE_UNAVAILABLE.getCode(),
                                    ex.getMessage()))
                            .doOnNext(buffer -> logException(exchange, ex, HttpStatus.SERVICE_UNAVAILABLE,
                                    false, "Service Unavailable")));
        }

        response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
        return response.writeWith(
                Mono.just(toBuffer(response, "Internal error", ErrorMessages.GENERIC_SERVER_ERROR.getCode(),
                                ex.getMessage()))
                        .doOnNext(buffer -> logException(exchange, ex, HttpStatus.INTERNAL_SERVER_ERROR,
                                true, "Internal Server Error")));
    }

    private DataBuffer toBuffer(ServerHttpResponse response, String error, String code, String detail) {
        String json = "{"
                + "\"error\":\"" + error + "\","
                + "\"code\":\"" + code + "\","
                + "\"detail\":\"" + detail + "\""
                + "}";
        return response.bufferFactory().wrap(json.getBytes(StandardCharsets.UTF_8));
    }

    private void logException(ServerWebExchange exchange, Throwable ex, HttpStatus status, boolean isError, String customLabel) {
        String message = String.format(
                "%s [%s %s]: %s - Returning HTTP %d",
                customLabel,
                exchange.getRequest().getMethod(),
                exchange.getRequest().getPath(),
                ex.getMessage(),
                status.value()
        );

        if (isError) {
            log.error(message, ex);
        } else {
            log.warn(message);
        }
    }
}
