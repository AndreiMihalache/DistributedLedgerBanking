package ro.ase.ism.clientapp.repo;

import ro.ase.ism.clientapp.entity.Card;

import java.util.Optional;

public interface CardRepository {
    Optional<Card> findByPublicKey(String publicKey);
}
