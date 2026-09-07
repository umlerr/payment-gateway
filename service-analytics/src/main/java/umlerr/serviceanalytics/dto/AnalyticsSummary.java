package umlerr.serviceanalytics.dto;

import java.util.List;

public record AnalyticsSummary(
    List<StatusAggregation> byStatus,
    List<MethodAggregation> byMethod,
    List<TopUserAggregation> topUsers
) {
}
