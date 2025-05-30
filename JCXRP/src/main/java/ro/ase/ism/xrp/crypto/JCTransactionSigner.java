package ro.ase.ism.xrp.crypto;

import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import ro.ase.ism.xrp.jcconnector.JCConnector;

public interface JCTransactionSigner<J extends JCConnector> {

    PublicKey getPublicKey(J connector);

    <T extends Transaction> SingleSignedTransaction<T> sign(J connector,T transaction);

    Signature sign(J connector, UnsignedClaim unsignedClaim);




}
