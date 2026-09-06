package umlerr.servicepayment.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
public class ErrorResponse {

    private String code;
    private String message;
    private String path;
    private LocalDateTime timestamp;
    private Map<String, String> details;
}
