package ro.ase.ism.xrp.crypto;

import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.signing.Signature;

public interface ECDSASignatureService {
    public Signature ecDsaSign(UnsignedByteArray transactionBytes);

    public Signature ecDsaVerify (UnsignedByteArray transactionBytes, Signature signature);
}
