package umlerr.serviceanalytics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import umlerr.serviceanalytics.dto.AnalyticsSummary;
import umlerr.serviceanalytics.service.AnalyticsService;

@RestController
@RequestMapping("/summary")
@RequiredArgsConstructor
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping
    public AnalyticsSummary summary() {
        return analyticsService.summary();
    }
}
