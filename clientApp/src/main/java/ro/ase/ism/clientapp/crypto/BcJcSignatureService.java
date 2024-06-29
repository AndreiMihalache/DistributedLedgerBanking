package ro.ase.ism.clientapp.crypto;

import org.bouncycastle.asn1.*;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.codec.binary.XrplBinaryCodec;
import org.xrpl.xrpl4j.crypto.HashingUtils;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.Signature;
import org.xrpl.xrpl4j.crypto.signing.SignatureUtils;
import org.xrpl.xrpl4j.crypto.signing.bc.Secp256k1;
import org.xrpl.xrpl4j.model.jackson.ObjectMapperFactory;
import ro.ase.ism.clientapp.connector.JCConnector;

import java.math.BigInteger;

public class BcJcSignatureService extends AbstractJCSignatureService<JCConnector> implements JCSignatureService<JCConnector> {


    public BcJcSignatureService(){
        this(new SignatureUtils(ObjectMapperFactory.create(), XrplBinaryCodec.getInstance()));
    }

    public BcJcSignatureService(SignatureUtils signatureUtils) {
        super(signatureUtils);
    }


    @Override
    protected Signature ecDsaSign(JCConnector connector, UnsignedByteArray signableTransactionBytes) {
        UnsignedByteArray messageHash = HashingUtils.sha512Half(signableTransactionBytes);
        connector.connect();
        connector.selectApplet();
        byte[] signature = connector.sign(messageHash.toByteArray());
        connector.disconnect();


        try(ASN1InputStream asn1is = new ASN1InputStream(signature);) {
            ASN1Sequence seq = (ASN1Sequence) asn1is.readObject();
            BigInteger r = ((ASN1Integer) seq.getObjectAt(0)).getValue();
            BigInteger s = ((ASN1Integer) seq.getObjectAt(1)).getValue();
            BigInteger orderHalf = Secp256k1.EC_DOMAIN_PARAMETERS.getN().shiftRight(1);
            if (s.compareTo(orderHalf) > 0) {
                s = Secp256k1.EC_DOMAIN_PARAMETERS.getN().subtract(s);
            }
            ASN1EncodableVector v = new ASN1EncodableVector();
            v.add(new ASN1Integer(r));
            v.add(new ASN1Integer(s));
            ASN1Sequence der = new DERSequence(v);

            return Signature.builder().value(UnsignedByteArray.of(der.getEncoded())).build();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    protected boolean ecDsaVerify(PublicKey publicKey, UnsignedByteArray transactionBytes, Signature signature) {
        return false;
    }

    @Override
    public PublicKey getPublicKey(JCConnector connector) {
        return null;
    }


}
