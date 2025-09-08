package co.com.pragma.api;

import co.com.pragma.api.config.LoanPath;
import co.com.pragma.api.documentation.LoanReviewItemPageResponse;
import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.dto.LoanResponse;
import co.com.pragma.model.common.PageResponse;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
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
            )
    })
    public RouterFunction<ServerResponse> routerFunction(LoanHandler loanHandler) {
        return route(POST(loanPath.getLoans()), this.loanHandler::listenCreateLoanApplication)
                .andRoute(GET(loanPath.getLoans()), this.loanHandler::listenGetLoanApplications);
    }
}
