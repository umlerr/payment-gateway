package umlerr.serviceanalytics.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import umlerr.serviceanalytics.dto.MethodAggregation;
import umlerr.serviceanalytics.dto.StatusAggregation;
import umlerr.serviceanalytics.dto.TopUserAggregation;
import umlerr.serviceanalytics.model.PaymentProjection;

import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentProjectionRepository extends JpaRepository<PaymentProjection, UUID> {

    @Query("select new umlerr.serviceanalytics.dto.StatusAggregation(p.status, count(p), sum(p.amount)) "
        + "from PaymentProjection p group by p.status")
    List<StatusAggregation> aggregateByStatus();

    @Query("select new umlerr.serviceanalytics.dto.MethodAggregation(p.method, count(p), sum(p.amount)) "
        + "from PaymentProjection p group by p.method")
    List<MethodAggregation> aggregateByMethod();

    @Query(value = "select p.user_id as userId, sum(p.amount) as totalAmount "
        + "from payment_projections p group by p.user_id order by totalAmount desc limit 5", nativeQuery = true)
    List<TopUserAggregation> aggregateTopUsers();
}
