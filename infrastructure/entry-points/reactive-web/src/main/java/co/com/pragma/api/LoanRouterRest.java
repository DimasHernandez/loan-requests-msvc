package co.com.pragma.api;

import co.com.pragma.api.config.LoanPath;
import co.com.pragma.api.documentation.LoanReviewItemPageResponse;
import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.dto.LoanResponse;
import co.com.pragma.api.dto.UpdateLoanApplicationRequest;
import co.com.pragma.api.dto.UpdateLoanApplicationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.*;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class LoanRouterRest {

    private final LoanPath loanPath;

    private final LoanHandler loanHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/loans",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = LoanHandler.class,
                    beanMethod = "listenCreateLoanApplication",
                    operation = @Operation(
                            operationId = "createLoanApplication",
                            summary = "Create new loan request",
                            description = "Create a loan in the system with the information provided.",
                            tags = {"LoanApplication"},
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Create a record of a loan application",
                                    content = @Content(schema = @Schema(implementation = LoanRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "201", description = "Loan registered successfully",
                                            content = @Content(schema = @Schema(implementation = LoanResponse.class))),

                                    @ApiResponse(responseCode = "422", description = "validation failed",
                                            content = @Content(schema = @Schema(example = "{ \"error\": \"Fallo validacion\", \"status\": \"422\", \"detail\": \"El documento de identidad es obligatorio\" }")))
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/loans",
                    produces = {"application/json"},
                    method = RequestMethod.GET,
                    beanClass = LoanHandler.class,
                    beanMethod = "listenGetLoanApplications",
                    operation = @Operation(
                            operationId = "getLoanApplicationsReview",
                            summary = "As an administrator user, review loan requests from users",
                            description = "review loan applications",
                            parameters = {
                                    @Parameter(
                                            name = "page",
                                            description = "The number starts at zero, page number you wish to consult",
                                            in = ParameterIn.QUERY
                                    ),
                                    @Parameter(
                                            name = "size",
                                            description = "The number of records displayed per page",
                                            in = ParameterIn.QUERY
                                    ),
                                    @Parameter(
                                            name = "statuses",
                                            description = "Application statuses to filter. [PENDING_REVIEW,REJECTED,MANUAL_REVIEW]",
                                            in = ParameterIn.QUERY
                                    )
                            },
                            responses = {
                                    @ApiResponse(
                                            responseCode = "200",
                                            description = "Loans for review found",
                                            content = @Content(schema = @Schema(implementation = LoanReviewItemPageResponse.class))
                                    )
                            }
                    )
            ),
            @RouterOperation(
                    path = "/api/v1/loans/{loanApplicationId}",
                    produces = {"application/json"},
                    method = RequestMethod.PUT,
                    beanClass = LoanHandler.class,
                    beanMethod = "listenUpdateLoanApplication",
                    operation = @Operation(
                            operationId = "UpdateStatusLoanApplication",
                            summary = "Update the status of a request",
                            description = "The user advisor wants to update the status of a loan application.",
                            parameters = {
                                    @Parameter(
                                            name = "loanApplicationId",
                                            description = "Unique loan application identifier",
                                            in = ParameterIn.PATH
                                    )
                            },
                            tags = {"LoanApplication"},
                            requestBody = @RequestBody(
                                    required = true,
                                    description = "Update status a record of a loan application",
                                    content = @Content(schema = @Schema(implementation = UpdateLoanApplicationRequest.class))
                            ),
                            responses = {
                                    @ApiResponse(responseCode = "200", description = "Loan updated successfully",
                                            content = @Content(schema = @Schema(implementation = UpdateLoanApplicationResponse.class))),

                                    @ApiResponse(responseCode = "401", description = "Unauthorized",
                                            content = @Content(schema = @Schema(example = "{ \"code\": \"AUTH_010\", \"error\": \"No autorizado\", \"message\": \"No tiene credenciales validas\" }"))),

                                    @ApiResponse(responseCode = "403", description = "Forbidden",
                                            content = @Content(schema = @Schema(example = "{ \"code\": \"AUTH_013\", \"error\": \"Forbidden\", \"message\": \"No tiene credenciales validas\" }"))),

                                    @ApiResponse(responseCode = "404", description = "Not Found",
                                            content = @Content(schema = @Schema(example = "{ \"error\": \"Error de negocio\", \"code\": \"LOAN_APP_001\", \"detail\": \"Solicitud de préstamo no encontrada\" }"))),

                                    @ApiResponse(responseCode = "422", description = "validation failed",
                                            content = @Content(schema = @Schema(example = "{ \"error\": \"Fallo validacion\", \"code\": \"SNA_006\", \"detail\": \"La solicitud de préstamo ya se encuentra en estado final APPROVED\" }")))

                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(LoanHandler loanHandler) {
        return route(POST(loanPath.getLoans()), this.loanHandler::listenCreateLoanApplication)
                .andRoute(GET(loanPath.getLoans()), this.loanHandler::listenGetLoanApplications)
                .andRoute(PUT(loanPath.getLoansUpdated()), this.loanHandler::listenUpdateLoanApplication);
    }
}
