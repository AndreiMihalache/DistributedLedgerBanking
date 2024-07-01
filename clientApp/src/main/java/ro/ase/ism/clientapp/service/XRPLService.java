package ro.ase.ism.clientapp.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.primitives.UnsignedInteger;
import lombok.extern.log4j.Log4j2;
import okhttp3.HttpUrl;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.xrpl.xrpl4j.client.JsonRpcClientErrorException;
import org.xrpl.xrpl4j.client.XrplClient;
import org.xrpl.xrpl4j.client.faucet.FaucetClient;
import org.xrpl.xrpl4j.client.faucet.FundAccountRequest;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoRequestParams;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.client.common.LedgerIndex;
import org.xrpl.xrpl4j.model.client.common.LedgerSpecifier;
import org.xrpl.xrpl4j.model.client.fees.FeeResult;
import org.xrpl.xrpl4j.model.client.ledger.LedgerRequestParams;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XrpCurrencyAmount;
import ro.ase.ism.clientapp.properties.Props;

import java.math.BigDecimal;

@Service
@Log4j2
public class XRPLService {

    @Autowired
    private WalletService walletService;

    @Autowired
    private Props props;
    HttpUrl rippledUrl;
    XrplClient client;


    public void init()
    {
        rippledUrl = HttpUrl.get(props.getRipple());
        client = new XrplClient(rippledUrl);
    }

    public AccountInfoResult getAccountInfo(Address address)
    {
        AccountInfoRequestParams req = AccountInfoRequestParams.of(address);
        try {
            return client.accountInfo(req);
        } catch (JsonRpcClientErrorException e) {
            fundFromFaucet(address);
            try{
                return client.accountInfo(req);
            }
            catch (JsonRpcClientErrorException second)
            {
                throw new RuntimeException(second.getMessage());
            }
        }
    }

    public void fundFromFaucet(Address address){
        FaucetClient faucetClient = FaucetClient.construct(HttpUrl.get(props.getFaucet()));
        faucetClient.fundAccount(FundAccountRequest.of(address));
    }

    public boolean transferFunds (Address sourceAddress, PublicKey sourcePublic, Long amount, Address destinantion)
    {
        try {
            FeeResult feeResult = null;

            feeResult = client.fee();
            XrpCurrencyAmount transactionFee = feeResult.drops().openLedgerFee();
            LedgerIndex validatedLedger = client.ledger(
                            LedgerRequestParams.builder()
                                    .ledgerSpecifier(LedgerSpecifier.VALIDATED)
                                    .build()
                    )
                    .ledgerIndex()
                    .orElseThrow(() -> new RuntimeException("LedgerIndex not available."));

            UnsignedInteger lastLedgerSequence = validatedLedger.plus(UnsignedInteger.valueOf(4)).unsignedIntegerValue();

            AccountInfoResult sourceAcc = getAccountInfo(sourceAddress);

            Payment payment = Payment.builder()
                    .account(sourceAddress)
                    .amount(XrpCurrencyAmount.ofXrp(BigDecimal.valueOf(amount)))
                    .destination(destinantion)
                    .sequence(sourceAcc.accountData().sequence())
                    .fee(transactionFee)
                    .signingPublicKey(sourcePublic)
                    .lastLedgerSequence(lastLedgerSequence)
                    .build();

            SingleSignedTransaction<Payment> signedTransaction = walletService.signTransaction(payment);
            client.submit(signedTransaction);

            return true;
        } catch (JsonRpcClientErrorException | JsonProcessingException e) {
            log.info(e);
            return false;
        }
    }
}
