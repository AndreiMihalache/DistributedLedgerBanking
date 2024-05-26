package ro.ase.ism.xrp.crypto;


import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.signing.Signature;

import java.util.Objects;

public class ExtendedSignatureService implements ECDSASignatureService{


    @Override
    public Signature ecDsaSign(UnsignedByteArray transactionBytes) {
        Objects.requireNonNull(transactionBytes);

        return null;
    }

    @Override
    public Signature ecDsaVerify(UnsignedByteArray transactionBytes, Signature signature) {
        Objects.requireNonNull(transactionBytes);
        Objects.requireNonNull(signature);


        return null;
    }
}
