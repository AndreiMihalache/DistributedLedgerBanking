package ro.ase.ism.clientapp.crypto;

import ro.ase.ism.clientapp.connector.JCConnector;

public interface JCSignatureService <J extends JCConnector> extends JCTransactionSigner<J>, JCTransactionVerifier {
}
