package io.github.wasp_stdnt.passwordmanagerv2.repository;

import io.github.wasp_stdnt.passwordmanagerv2.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    void deleteById(Long id);
}