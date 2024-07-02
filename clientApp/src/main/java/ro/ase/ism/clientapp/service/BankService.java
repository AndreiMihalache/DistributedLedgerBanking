package ro.ase.ism.clientapp.service;

import org.springframework.stereotype.Service;
import ro.ase.ism.clientapp.entity.Bank;
import ro.ase.ism.clientapp.repo.BankRepository;

import java.util.List;
import java.util.Optional;

@Service
public class BankService {

    private final BankRepository bankRepository;

    public BankService(BankRepository bankRepository) {
        this.bankRepository = bankRepository;
    }

    public List<Bank> findAll() {
        return bankRepository.findAll();
    }

    public Optional<Bank> findById(Long id) {
        return bankRepository.findById(id);
    }

    public Optional<Bank> findByName(String name) {
        return bankRepository.findByName(name);
    }
}
