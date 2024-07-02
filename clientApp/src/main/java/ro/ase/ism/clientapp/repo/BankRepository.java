package ro.ase.ism.clientapp.repo;

import org.springframework.data.jpa.repository.JpaRepository;
import ro.ase.ism.clientapp.entity.Bank;

import java.util.Optional;

public interface BankRepository extends JpaRepository<Bank, Long> {

    Optional<Bank> findByName(String name);
}
