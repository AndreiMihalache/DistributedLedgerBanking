package ro.ase.ism.clientapp.service;

import javacard.framework.CardRuntimeException;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Payment;
import ro.ase.ism.clientapp.connector.JCConnectorImpl;
import ro.ase.ism.clientapp.crypto.BcJcSignatureService;
import ro.ase.ism.clientapp.wallet.JCWallet;

import javax.smartcardio.CardNotPresentException;

@Service
@Log4j2
public class WalletService {

    @Autowired
    private JCConnectorImpl jcConnector;

    @Autowired
    private BcJcSignatureService signatureService;
    public boolean init(String host, String port)
    {
        return jcConnector.init(host, port);
    }

    public void initJCWallet(JCWallet jcwallet)
    {
        jcConnector.connect();
        jcConnector.selectApplet();
        byte[] pubKeyBytes = jcConnector.getPublicKey();
        jcConnector.disconnect();
        jcwallet.init(pubKeyBytes);
    }


    public SingleSignedTransaction<Payment> signTransaction(Payment payment) {

        try {
            if (jcConnector.isInitialized()) {
                return signatureService.sign(jcConnector, payment);
            } else throw new CardNotPresentException("Card not initialized");
        } catch (CardNotPresentException e) {
            log.info(e);
            return null;
        }
    }
}
