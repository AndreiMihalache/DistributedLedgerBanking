package ro.ase.ism.xrp.wallet;

import lombok.Data;
import lombok.extern.log4j.Log4j2;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECNamedDomainParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.bouncycastle.math.ec.ECPoint;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.bc.BcKeyUtils;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XAddress;
import ro.ase.ism.xrp.crypto.BcJcSignatureService;
import ro.ase.ism.xrp.jcconnector.JCConnector;
import ro.ase.ism.xrp.jcconnector.JCConnectorImpl;

import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

@Data
@Log4j2
public class JCardWallet {

    private Address classicAddress;
    private XAddress xAddress;
    private PublicKey publicKey;
    private JCConnector connector;

    public SingleSignedTransaction<Payment> sign(Payment payment)
    {
        return new BcJcSignatureService().sign(this.connector, payment);
    }


    public JCardWallet(String connectorHost, String connectorPort)
    {
        connector = new JCConnectorImpl( connectorHost, connectorPort);
        init();
        createAccount();
    }

    public JCardWallet()
    {
        connector = new JCConnectorImpl();
        init();
        createAccount();
    }



    private void init()
    {
        try {
            connector.connect("localhost", "9025");
            connector.selectApplet();
            byte[] pubKeyBytes = connector.getPublicKey();
            connector.disconnect();

            BouncyCastleProvider bcp = new BouncyCastleProvider();
            Security.addProvider(bcp);


            ECNamedCurveParameterSpec spec = ECNamedCurveTable.getParameterSpec("secp256k1");

            ECPoint wPoint = spec.getCurve().decodePoint(pubKeyBytes);
            ECPublicKeySpec publicKeySpec = new ECPublicKeySpec(wPoint, spec);
            KeyFactory keyFactory = KeyFactory.getInstance("ECDSA", "BC");
            ECPublicKey bcPublicKey = (ECPublicKey) keyFactory.generatePublic(publicKeySpec);

            byte[] keybytes = bcPublicKey.getEncoded();

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

    private void createAccount(){

        boolean hasAccount = true;
        try{
            HttpUrl rippledUrl = HttpUrl.get("https://s.altnet.rippletest.net:51234/");
            log.info("Constructing an XrplClient connected to " + rippledUrl);
            XrplClient xrplClient = new XrplClient(rippledUrl);

            AccountInfoRequestParams requestParamsJc = AccountInfoRequestParams.of(this.getClassicAddress());
            AccountInfoResult jcWalletResult = xrplClient.accountInfo(requestParamsJc);
            log.info("Account found");

        } catch (JsonRpcClientErrorException e) {
            hasAccount =false;
        }
        if(!hasAccount)
        {
            FaucetClient faucetClient = FaucetClient.construct(HttpUrl.get("https://faucet.altnet.rippletest.net"));
            faucetClient.fundAccount(FundAccountRequest.of(this.getClassicAddress()));
        }
    }



}
