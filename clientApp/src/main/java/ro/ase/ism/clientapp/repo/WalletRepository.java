package ro.ase.ism.clientapp.repo;

import ro.ase.ism.clientapp.entity.Wallet;

import java.util.Optional;

public interface WalletRepository {
    Optional<Wallet> findByPublicKey(String publicKey);
}
