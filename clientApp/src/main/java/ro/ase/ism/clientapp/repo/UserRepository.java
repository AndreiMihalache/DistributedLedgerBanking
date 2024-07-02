package ro.ase.ism.clientapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.clientapp.entity.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);
}
