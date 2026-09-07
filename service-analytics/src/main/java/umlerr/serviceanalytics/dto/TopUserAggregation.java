package umlerr.serviceanalytics.dto;

import java.math.BigDecimal;

public record TopUserAggregation(String userId, BigDecimal totalAmount) {
}
