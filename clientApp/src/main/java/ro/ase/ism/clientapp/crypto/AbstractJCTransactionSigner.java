package ro.ase.ism.clientapp.crypto;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.channels.UnsignedClaim;
import org.xrpl.xrpl4j.model.transactions.Transaction;
import ro.ase.ism.clientapp.connector.JCConnector;

import java.util.Objects;

public abstract class AbstractJCTransactionSigner<J extends JCConnector> implements JCTransactionSigner<J> {

    private final SignatureUtils signatureUtils;

    public AbstractJCTransactionSigner(final SignatureUtils signatureUtils){
        this.signatureUtils = (SignatureUtils) Objects.requireNonNull(signatureUtils);
    }

    @Override
    public <T extends Transaction> SingleSignedTransaction<T> sign(J connector, T transaction) {
        Objects.requireNonNull(connector);
        Objects.requireNonNull(transaction);
        UnsignedByteArray signableTransactionBytes = this.signatureUtils.toSignableBytes(transaction);
        Signature signature = this.signingHelper(connector, signableTransactionBytes);
        return this.signatureUtils.addSignatureToTransaction(transaction, signature);
    }

    @Override
    public Signature sign(J connector, UnsignedClaim unsignedClaim) {
        Objects.requireNonNull(connector);
        Objects.requireNonNull(unsignedClaim);
        UnsignedByteArray signableBytes = this.signatureUtils.toSignableBytes(unsignedClaim);
        return this.signingHelper(connector, signableBytes);
    }

    private Signature signingHelper(J connector, UnsignedByteArray signableTransactionBytes) {
        Objects.requireNonNull(connector);
        Objects.requireNonNull(signableTransactionBytes);
        return this.ecDsaSign(connector, signableTransactionBytes);
    }

    protected abstract Signature ecDsaSign(J connector, UnsignedByteArray signableTransactionBytes);
}
