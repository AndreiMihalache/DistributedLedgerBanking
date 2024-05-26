package ro.ase.ism.xrp;

import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import ro.ase.ism.xrp.jcconnector.JCConnector;
import ro.ase.ism.xrp.wallet.Wallet;

import java.math.BigDecimal;

public class Main {

    protected static final Logger logger = LogManager.getLogger();



    public static void main1() {
        JCConnector connector = new JCConnector("C:\\Software\\jcdk\\jcsimu\\samples\\client.config.properties");
        connector.connect("localhost","9025");
        connector.selectApplet();
        //byte [] pub = connector.getPublicKey();
        byte[] pubKeyBytes = connector.getPublicKey();
        connector.disconnect();
    }
    public static void main(String[] args) {

        Wallet wallet1 = new Wallet("Agent1EC");
        logger.info("Classic Address Wallet 1: " + wallet1.getClassicAddress());
        logger.info("X-Address Wallet 1: " + wallet1.getXAddress());

        Wallet wallet2 = new Wallet("Agent2EC");
        logger.info("Classic Address Wallet 2: " + wallet2.getClassicAddress());
        logger.info("X-Address Wallet 2: " + wallet2.getXAddress());

        try{
        HttpUrl rippledUrl = HttpUrl.get("https://s.altnet.rippletest.net:51234/");
        logger.info("Constructing an XrplClient connected to " + rippledUrl);
        XrplClient xrplClient = new XrplClient(rippledUrl);

        //Creates account and funds it

        FaucetClient faucetClient = FaucetClient.construct(HttpUrl.get("https://faucet.altnet.rippletest.net"));
            faucetClient.fundAccount(FundAccountRequest.of(wallet1.getClassicAddress()));
            faucetClient.fundAccount(FundAccountRequest.of(wallet2.getClassicAddress()));

        AccountInfoRequestParams requestParamsWallet1 = AccountInfoRequestParams.of(wallet1.getClassicAddress());
        AccountInfoResult initialWallet1 = xrplClient.accountInfo(requestParamsWallet1);

        AccountInfoRequestParams requestParamsWallet2 = AccountInfoRequestParams.of(wallet2.getClassicAddress());
        AccountInfoResult initialWallet2= xrplClient.accountInfo(requestParamsWallet2);


            logger.info("Wallet1 balance in drops:" + initialWallet1.accountData().balance());
            logger.info("Wallet2 balance in drops:" + initialWallet2.accountData().balance());

        FeeResult feeResult = xrplClient.fee();
        XrpCurrencyAmount transactionFee = feeResult.drops().openLedgerFee();

        LedgerIndex validatedLedger = xrplClient.ledger(
                        LedgerRequestParams.builder()
                                .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                                .build()
                )
                .ledgerIndex()
                .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

        UnsignedInteger lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

        Payment payment = Payment.builder()
                .account(wallet1.getClassicAddress())
                .amount(XrpCurrencyAmount.ofXrp(BigDecimal.ONE))
                .destination(wallet2.getClassicAddress())
                .sequence(initialWallet1.accountData().sequence())
                .fee(transactionFee)
                .signingPublicKey(wallet1.getPublicKey())
                .lastLedgerSequence(lastLedgerSequence)
                .build();

            SingleSignedTransaction<Payment> signedTransaction = wallet1.signSingleTransaction(payment, new BcSignatureService());
            xrplClient.submit(signedTransaction);

            AccountInfoResult finalWallet1 = xrplClient.accountInfo(requestParamsWallet1);
            AccountInfoResult finalWallet2= xrplClient.accountInfo(requestParamsWallet2);

            logger.info("Wallet1 balance in drops:" + finalWallet1.accountData().balance());
            logger.info("Wallet2 balance in drops:" + finalWallet2.accountData().balance());


        }
        catch (Exception e){
            e.printStackTrace();
        }

    }
}
