package ro.ase.ism.xrp;

import com.google.common.primitives.UnsignedInteger;
import okhttp3.HttpUrl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bouncycastle.jce.ECNamedCurveTable;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.jce.spec.ECNamedCurveParameterSpec;
import org.bouncycastle.jce.spec.ECPublicKeySpec;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.codec.addresses.AddressBase58;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.Base58;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.crypto.signing.bc.BcSignatureService;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XAddress;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import ro.ase.ism.xrp.jcconnector.JCConnectorImpl;
import ro.ase.ism.xrp.wallet.JCardWallet;
import ro.ase.ism.xrp.wallet.Wallet;

import java.beans.Encoder;
import java.math.BigDecimal;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.util.Arrays;

public class Main {

    protected static final Logger logger = LogManager.getLogger();



    public static void main(String[] args) {


        JCardWallet jcwallet = new JCardWallet( "localhost", "9025");

        try {

            HttpUrl rippledUrl = HttpUrl.get("https://s.altnet.rippletest.net:51234/");
            logger.info("Constructing an XrplClient connected to " + rippledUrl);
            XrplClient xrplClient = new XrplClient(rippledUrl);


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

            Wallet wallet1 = new Wallet("Agent1EC");
            AccountInfoRequestParams requestParamsWallet1 = AccountInfoRequestParams.of(wallet1.getClassicAddress());
            AccountInfoResult initialWallet1 = xrplClient.accountInfo(requestParamsWallet1);
            logger.info(initialWallet1);

            AccountInfoRequestParams requestParamsJc = AccountInfoRequestParams.of(jcwallet.getClassicAddress());
            AccountInfoResult jcWalletResult = xrplClient.accountInfo(requestParamsJc);
            logger.info(jcWalletResult);


            Payment payment = Payment.builder()
                    .account(jcwallet.getClassicAddress())
                    .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(10)))
                    .destination(wallet1.getClassicAddress())
                    .sequence(jcWalletResult.accountData().sequence())
                    .fee(transactionFee)
                    .signingPublicKey(jcwallet.getPublicKey())
                    .lastLedgerSequence(lastLedgerSequence)
                    .build();


            SingleSignedTransaction<Payment> signedTransaction = jcwallet.sign(payment);
            xrplClient.submit(signedTransaction);

            AccountInfoResult finalNormal = xrplClient.accountInfo(requestParamsWallet1);
            logger.info(finalNormal);

            AccountInfoResult finalJC = xrplClient.accountInfo(requestParamsJc);
            logger.info(finalJC);



        }
        catch (Exception e)
        {
           e.printStackTrace();
        }
    }
    public static void main1(String[] args) {


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

        /*FaucetClient faucetClient = FaucetClient.construct(HttpUrl.get("https://sidechain-net2.devnet.rippletest.net:51234/"));
            faucetClient.fundAccount(FundAccountRequest.of(wallet1.getClassicAddress()));
            faucetClient.fundAccount(FundAccountRequest.of(wallet2.getClassicAddress()));*/

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
            logger.info(e.toString());
        }

    }
}
