package umlerr.servicenotification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import umlerr.servicenotification.model.Notification;

import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, UUID> {

    List<Notification> findByPaymentId(UUID paymentId);
}
