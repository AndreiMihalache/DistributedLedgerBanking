package ro.ase.ism.xrp.crypto;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.model.transactions.Signer;
import org.xrpl.xrpl4j.model.transactions.Transaction;

import java.util.Objects;

public abstract class AbstractJCTransactionVerifier implements JCTransactionVerifier{

    private final SignatureUtils signatureUtils;

    public AbstractJCTransactionVerifier(SignatureUtils signatureUtils){
        this.signatureUtils = (SignatureUtils) Objects.requireNonNull(signatureUtils);
    }

    public <T extends Transaction> boolean verify(Signer signer, T unsignedTransaction) {
        Objects.requireNonNull(signer);
        Objects.requireNonNull(unsignedTransaction);
        UnsignedByteArray transactionBytesUba = this.getSignatureUtils().toSignableBytes(unsignedTransaction);
        PublicKey publicKey = signer.signingPublicKey();
        return this.ecDsaVerify(publicKey, transactionBytesUba, signer.transactionSignature());
    }

    private boolean verifyHelper(Signer signer, UnsignedByteArray unsignedTransactionBytes) {
        Objects.requireNonNull(signer);
        Objects.requireNonNull(unsignedTransactionBytes);
        PublicKey signerPublicKey = signer.signingPublicKey();
        Signature signerSignature = signer.transactionSignature();
       return this.ecDsaVerify(signerPublicKey, unsignedTransactionBytes, signerSignature);
    }

    protected abstract boolean ecDsaVerify(PublicKey var1, UnsignedByteArray var2, Signature var3);

    private SignatureUtils getSignatureUtils() {
        return this.signatureUtils;
    }
}
