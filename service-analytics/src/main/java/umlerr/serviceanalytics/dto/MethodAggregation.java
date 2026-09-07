package umlerr.serviceanalytics.dto;

import java.math.BigDecimal;
import umlerr.common.enums.PaymentMethod;

public record MethodAggregation(PaymentMethod method, long count, BigDecimal totalAmount) {
}
