package ro.ase.ism.xrp.wallet;

import lombok.AccessLevel;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.xrpl.xrpl4j.codec.addresses.AddressCodec;
import org.xrpl.xrpl4j.codec.addresses.UnsignedByteArray;
import org.xrpl.xrpl4j.crypto.keys.KeyPair;
import org.xrpl.xrpl4j.crypto.keys.PrivateKey;
import org.xrpl.xrpl4j.crypto.keys.PublicKey;
import org.xrpl.xrpl4j.crypto.keys.Seed;
import org.xrpl.xrpl4j.crypto.signing.SignatureService;
import org.xrpl.xrpl4j.crypto.signing.SingleSignedTransaction;
import org.xrpl.xrpl4j.model.transactions.Address;
import org.xrpl.xrpl4j.model.transactions.Payment;
import org.xrpl.xrpl4j.model.transactions.XAddress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Arrays;


@Data
public class Wallet {

    @Getter(value= AccessLevel.PRIVATE)
    @Setter(value=AccessLevel.PRIVATE)
    private KeyPair kp;

    private Address classicAddress;
    private XAddress xAddress;

    public PublicKey getPublicKey(){
        return kp.publicKey();
    }


    private KeyPair generateKeyPair()
    {
        //KeyPair keyPair = Seed.ed25519Seed().deriveKeyPair();
        KeyPair keyPair = Seed.secp256k1Seed().deriveKeyPair();
        System.out.println("Generated KeyPair: "+keyPair);
        return keyPair;
    }

    private void readKeys(String keyFile)
    {
        try(FileInputStream fisPvt = new FileInputStream(keyFile+"_pvt_ec"); FileInputStream fisPub = new FileInputStream(keyFile+"_pub_ec"))
        {
            byte[] pvtBytes = fisPvt.readAllBytes();
            PrivateKey prv = PrivateKey.of(UnsignedByteArray.of(pvtBytes));

            byte[] pubBytes = fisPub.readAllBytes();
            PublicKey pub = PublicKey.builder().value(UnsignedByteArray.of(pubBytes)).build();

            if(getKp() == null) setKp( KeyPair.builder().privateKey(prv).publicKey(pub).build());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void saveKeys(String keyFile){
        if(kp!=null)
        {
            try (FileOutputStream fosPvt = new FileOutputStream(keyFile + "_pvt_Ec")){
                UnsignedByteArray privBytes = getKp().privateKey().value();
                fosPvt.write(privBytes.toByteArray());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


            try(FileOutputStream fosPub = new FileOutputStream(keyFile + "_pub_Ec")){
                fosPub.write(getKp().publicKey().value().toByteArray());
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }


        }
    }

    public SingleSignedTransaction<Payment> signSingleTransaction(Payment payment, SignatureService<PrivateKey> signatureService)
    {
        return signatureService.sign(this.getKp().privateKey(), payment);
    }

    public Wallet(String identity) {
        try {
            File file = new File(identity);
            if (file.exists()) {
                readKeys(identity);
            }
            else {
                file.createNewFile();
                setKp(generateKeyPair());
                saveKeys(identity);
            }

            if(kp!=null)
            {
                classicAddress = kp.publicKey().deriveAddress();
                xAddress = AddressCodec.getInstance().classicAddressToXAddress(classicAddress, true);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
