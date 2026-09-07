package umlerr.serviceanalytics.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import umlerr.serviceanalytics.dto.AnalyticsSummary;
import umlerr.serviceanalytics.dto.MethodAggregation;
import umlerr.serviceanalytics.dto.StatusAggregation;
import umlerr.serviceanalytics.dto.TopUserAggregation;
import umlerr.serviceanalytics.repository.PaymentProjectionRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final PaymentProjectionRepository projectionRepository;

    public AnalyticsSummary summary() {
        List<StatusAggregation> byStatus = projectionRepository.aggregateByStatus();
        List<MethodAggregation> byMethod = projectionRepository.aggregateByMethod();
        List<TopUserAggregation> topUsers = projectionRepository.aggregateTopUsers();
        return new AnalyticsSummary(byStatus, byMethod, topUsers);
    }
}
