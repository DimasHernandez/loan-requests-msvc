package co.com.pragma.api.documentation;

import co.com.pragma.model.common.PageResponse;
import co.com.pragma.model.loanreviewitem.LoanReviewItem;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * IMPORTANT -> Class only for documentation with Swagger
 */

@Schema(name = "LoanReviewItemPageResponse", description = "Paginated response to loan applications for review")
public class LoanReviewItemPageResponse extends PageResponse<LoanReviewItem> {

    public LoanReviewItemPageResponse(List<LoanReviewItem> content, int page, int size, long totalElements, int totalPages) {
        super(content, page, size, totalElements, totalPages);
    }
}
