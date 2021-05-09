package hu.emraxxor.web.ide.repositories;

import hu.emraxxor.web.ide.entities.UserLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserLogRepository extends JpaRepository<UserLog, Long> {
}