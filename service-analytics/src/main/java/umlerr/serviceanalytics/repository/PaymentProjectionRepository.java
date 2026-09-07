package umlerr.serviceanalytics.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umlerr.serviceanalytics.model.PaymentProjection;

@Repository
public interface PaymentProjectionRepository extends JpaRepository<PaymentProjection, UUID> {
}
