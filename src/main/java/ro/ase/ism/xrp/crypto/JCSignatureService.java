package ro.ase.ism.xrp.crypto;

import org.xrpl.xrpl4j.crypto.signing.TransactionVerifier;
import ro.ase.ism.xrp.jcconnector.JCConnector;

public interface JCSignatureService <J extends JCConnector> extends JCTransactionSigner<J>, JCTransactionVerifier {
}
