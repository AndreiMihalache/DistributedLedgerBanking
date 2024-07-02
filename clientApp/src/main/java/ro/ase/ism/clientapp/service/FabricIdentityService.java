package ro.ase.ism.clientapp.service;

import io.grpc.*;
import org.apache.commons.lang3.StringUtils;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.identity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ro.ase.ism.clientapp.properties.Props;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.InvalidKeyException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class FabricIdentityService {

    private final Props props;

    static String BANKA="banka";
    static String BANKB="bankb";
    static String BANKC="bankc";

    static String peerOrgs = "peerOrganizations";
    private final Map<String, Gateway.Builder> gateways = new HashMap<>();

    private final Map<String, String> banks = new HashMap<>();
    private final Map<String, List<String>> channels = new HashMap<>();

    private final Map<String,String> peerEndpoints = new HashMap<>();


    public FabricIdentityService(Props props){
        this.props = props;
        initializeIdentities();
    }

    private void initializeIdentities(){
        banks.put(BANKA, props.getBankAUser());
        banks.put(BANKB, props.getBankBUser());
        banks.put(BANKC, props.getBankCUser());

        channels.put(BANKA, props.getBankAChannels());
        channels.put(BANKB, props.getBankBChannels());
        channels.put(BANKC, props.getBankCChannels());

        peerEndpoints.put(BANKA, props.getBankAPeerEndpoint());
        peerEndpoints.put(BANKB, props.getBankBPeerEndpoint());
        peerEndpoints.put(BANKC, props.getBankCPeerEndpoint());


        for(Map.Entry<String, String> entry : banks.entrySet())
        {
            String bank = entry.getKey();
            String user = entry.getValue();


            String bankFullName = bank + ".ism.ase.ro";
            String peer = "peer0." + bankFullName;

            Path certificatePath = Paths.get(props.getFabricCryptoPath(), peerOrgs, bankFullName, "users",
                    user + "@" + bankFullName, "msp", "signcerts", user + "@" + bankFullName+ "-cert.pem");


            Path privateKeyPath = Paths.get(props.getFabricCryptoPath(), peerOrgs, bankFullName, "users",
                    user + "@" + bankFullName, "msp", "keystore", "priv_sk");

            Path tlsCertPath = Paths.get(props.getFabricCryptoPath(),peerOrgs, bankFullName, "peers",
                    peer, "tls", "ca.crt");

            Identity identity = new X509Identity(StringUtils.capitalize(bank)+"MSP", readCertificate(certificatePath));
            Signer signer = Signers.newPrivateKeySigner(readPrivateKey(privateKeyPath));


            ManagedChannel channel = newGrpcConnection(tlsCertPath, peer, peerEndpoints.get(bank));

             Gateway.Builder builder = Gateway.newInstance()
                    .identity(identity)
                    .signer(signer)
                    .connection(channel);

            gateways.put(bank, builder);
        }
    }

    private static ManagedChannel newGrpcConnection(Path tlsCertPath, String peer, String peerEndpoint) {
        try {
            ChannelCredentials credentials =TlsChannelCredentials.newBuilder().
                    trustManager(tlsCertPath.toFile()).build();
            return Grpc.newChannelBuilder(peerEndpoint, credentials)
                    .overrideAuthority(peer).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private X509Certificate readCertificate(Path certificatePath) {
        try(BufferedReader certReader = Files.newBufferedReader(certificatePath)) {
            return Identities.readX509Certificate(certReader);
        } catch (IOException | CertificateException e) {
            throw new RuntimeException(e);
        }
    }

    private PrivateKey readPrivateKey(Path privateKeyPath){
        try(BufferedReader keyReader = Files.newBufferedReader(privateKeyPath)){
            return Identities.readPrivateKey(keyReader);
        } catch (IOException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    public Gateway getGateway(String bankName) {
        if (!gateways.containsKey(bankName.toLowerCase())) {
            throw new IllegalArgumentException("Invalid bank name");
        }
        return gateways.get(bankName.toLowerCase()).connect();
    }

    public List<String> getBankChannels(String bankName) {
        return channels.get(bankName.toLowerCase());
    }


}
