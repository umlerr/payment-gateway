package umlerr.servicenotification.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import umlerr.servicenotification.model.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
}
