package ro.ase.ism.clientapp.wallet;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.springframework.stereotype.Component;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.security.KeyFactory;
import java.security.Security;
import java.security.interfaces.ECPublicKey;

@Component
@Data
@NoArgsConstructor
@Log4j2
@AllArgsConstructor
public class JCWallet {


    private Address classicAddress;
    private XAddress xAddress;
    private PublicKey publicKey;

    public void init(byte[] pubKeyBytes)
    {
        try {
            BouncyCastleProvider bcp = new BouncyCastleProvider();
            Security.addProvider(bcp);
            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");
            org.bouncycastle.math.ec.ECPoint wPoint = spec.getCurve().decodePoint(pubKeyBytes);
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(wPoint, spec);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            ECPublicKey bcPublicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);
            ECDomainParameters params = new ECDomainParameters(spec.getCurve(), spec.getG(), spec.getN(), spec.getH());
            ECPublicKeyParameters pubParams = new ECPublicKeyParameters(wPoint, params);
            this.publicKey = BcKeyUtils.toPublicKey(pubParams);
            this.classicAddress = this.publicKey.deriveAddress();
            this.xAddress = AddressCodec.getInstance().classicAddressToXAddress(this.classicAddress, true);
            Security.removeProvider("BC");
        }
        catch(Exception e)
        {
            log.info(e.toString());
        }
    }

    public String getEncodedPublicKey()
    {
        return publicKey.base16Value();
    }

    public String getEncodedAddress()
    {
        return classicAddress.value();
    }

    public String getEncodedXAddress()
    {
        return xAddress.value();
    }


}
