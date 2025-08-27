package co.com.pragma.api;

import co.com.pragma.api.config.LoanPath;
import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.dto.LoanResponse;
import io.swagger.v3.oas.annotations.Operation;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@Configuration
@RequiredArgsConstructor
public class RouterRest {

    private final LoanPath loanPath;

    private final Handler loanHandler;

    @Bean
    @RouterOperations({
            @RouterOperation(
                    path = "/api/v1/loans",
                    produces = {"application/json"},
                    method = RequestMethod.POST,
                    beanClass = Handler.class,
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
                                    @ApiResponse(responseCode = "200", description = "Loan registered successfully",
                                            content = @Content(schema = @Schema(implementation = LoanResponse.class)))
                            }
                    )
            )
    })
    public RouterFunction<ServerResponse> routerFunction(Handler handler) {
        return route(POST(loanPath.getLoans()), loanHandler::listenCreateLoanApplication);
    }
}
