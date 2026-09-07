package umlerr.serviceanalytics.dto;

import java.math.BigDecimal;

public record StatusAggregation(String status, long count, BigDecimal totalAmount) {
}
