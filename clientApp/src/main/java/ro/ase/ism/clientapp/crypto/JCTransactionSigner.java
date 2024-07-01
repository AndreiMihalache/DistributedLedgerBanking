package ro.ase.ism.clientapp.crypto;

import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import ro.ase.ism.clientapp.connector.JCConnector;

public interface JCTransactionSigner<J extends JCConnector> {

    <T extends Transaction> SingleSignedTransaction<T> sign(J connector,T transaction);

    Signature sign(J connector, UnsignedClaim unsignedClaim);




}
