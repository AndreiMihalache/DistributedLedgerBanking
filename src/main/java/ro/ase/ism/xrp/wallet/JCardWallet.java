package ro.ase.ism.xrp.wallet;

import lombok.Data;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Payment;

@Data
public class JCardWallet {


    public SingleSignedTransaction<Payment> signSingleTransaction(Payment payment)
    {
        return null;
    }


}
