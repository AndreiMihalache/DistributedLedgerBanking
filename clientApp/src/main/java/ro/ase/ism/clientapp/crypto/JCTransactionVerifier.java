package ro.ase.ism.clientapp.crypto;

import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

public interface JCTransactionVerifier {

    <T extends Transaction> boolean verify(Signer signer, T transaction);
}
