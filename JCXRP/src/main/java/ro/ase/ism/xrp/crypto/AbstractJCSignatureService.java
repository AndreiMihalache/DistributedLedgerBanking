package ro.ase.ism.xrp.crypto;

import com.google.common.annotations.VisibleForTesting;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.*;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import ro.ase.ism.xrp.jcconnector.JCConnector;

import java.util.Objects;

public abstract class AbstractJCSignatureService<J extends JCConnector> implements JCSignatureService<J>{

    private final AbstractJCTransactionSigner<J> abstractJCTransactionSigner;
    private final AbstractJCTransactionVerifier abstractTransactionVerifier;

    public AbstractJCSignatureService(SignatureUtils signatureUtils) {
        this.abstractJCTransactionSigner = new AbstractJCTransactionSigner<J>(signatureUtils) {

            protected Signature ecDsaSign(J connector, UnsignedByteArray signableTransactionBytes) {
                return AbstractJCSignatureService.this.ecDsaSign(connector, signableTransactionBytes);
            }

            public PublicKey getPublicKey(J connector) {
                return AbstractJCSignatureService.this.getPublicKey(connector);
            }
        };

        this.abstractTransactionVerifier = new AbstractJCTransactionVerifier(signatureUtils) {

            protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
                return AbstractJCSignatureService.this.ecDsaVerify(publicKey, transactionBytes, signature);
            }
        };

    }

    @VisibleForTesting
    protected AbstractJCSignatureService(AbstractJCTransactionSigner<J> abstractJCTransactionSigner, AbstractJCTransactionVerifier abstractJCTransactionVerifier) {
        this.abstractJCTransactionSigner = (AbstractJCTransactionSigner) Objects.requireNonNull(abstractJCTransactionSigner);
        this.abstractTransactionVerifier = (AbstractJCTransactionVerifier)Objects.requireNonNull(abstractJCTransactionVerifier);
    }

    public <T extends Transaction> SingleSignedTransaction<T> sign(J connector, T transaction) {
        return this.abstractJCTransactionSigner.sign(connector, transaction);
    }

    public Signature sign(J connector, UnsignedClaim unsignedClaim) {
        return this.abstractJCTransactionSigner.sign(connector, unsignedClaim);
    }


    public <T extends Transaction> boolean verify(Signer signer, T unsignedTransaction) {
        return this.abstractTransactionVerifier.verify(signer, unsignedTransaction);
    }


    protected abstract Signature ecDsaSign(J connector, UnsignedByteArray signableTransactionBytes);

    protected abstract boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature);

    public abstract PublicKey getPublicKey(J connector);

}
