package co.com.pragma.api.mapper;

import co.com.pragma.api.dto.LoanRequest;
import co.com.pragma.api.dto.LoanResponse;
import co.com.pragma.api.dto.UpdateLoanApplicationResponse;
import co.com.pragma.model.loanapplication.LoanApplication;
import co.com.pragma.model.loanapplication.UpdatedLoanApplication;
import co.com.pragma.model.loantype.LoanType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface LoanMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "email", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "loanType", source = "loanTypeName", qualifiedByName = "StringToLoanType")
    LoanApplication toDomain(LoanRequest loanRequest);

    LoanResponse toResponse(LoanApplication loanApplication);

    UpdateLoanApplicationResponse toUpdateLoanApplicationResponse(UpdatedLoanApplication updatedLoanApplication);

    @Named("StringToLoanType")
    default LoanType stringToLoanType(String loanTypeName) {
        if (loanTypeName == null || loanTypeName.trim().isEmpty())
            throw new IllegalArgumentException("El tipo de credito es obligatorio");

        return LoanType.builder()
                .name(loanTypeName.toUpperCase())
                .build();
    }
}
