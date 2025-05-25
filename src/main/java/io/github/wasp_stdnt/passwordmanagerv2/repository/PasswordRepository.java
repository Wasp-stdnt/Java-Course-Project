package io.github.wasp_stdnt.passwordmanagerv2.repository;

import io.github.wasp_stdnt.passwordmanagerv2.model.Password;
import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface PasswordRepository extends JpaRepository<Password, Long> {
    List<Password> findByUser(User user);
    Optional<Password> findByIdAndUser(Long id, User user);
    void deleteByIdAndUser(Long id, User user);
}