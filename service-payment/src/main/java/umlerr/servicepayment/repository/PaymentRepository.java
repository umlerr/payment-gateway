package umlerr.servicepayment.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umlerr.servicepayment.enums.PaymentStatus;
import umlerr.servicepayment.model.Payment;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    List<Payment> findByStatusAndCreatedAtBefore(PaymentStatus status, LocalDateTime threshold);

    List<Payment> findByStatusAndUpdatedAtBefore(PaymentStatus status, LocalDateTime threshold);
}
