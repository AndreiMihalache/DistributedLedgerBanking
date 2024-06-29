import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.Contract;
import org.hyperledger.fabric.contract.annotation.Default;
import org.hyperledger.fabric.contract.annotation.Info;
import org.hyperledger.fabric.contract.annotation.Transaction;
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;

import java.util.ArrayList;
import java.util.List;

@Contract(
        name="kyc",
        info = @Info(title = "KYC Chaincode")
)
@Default
public class CustomerTransfer implements ContractInterface{
    private final Genson genson = new Genson();

    private enum CustomerTransferErrors {
        CUSTOMER_NOT_FOUND,
        CUSTOMER_ALREADY_EXISTS
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Customer CreateCustomer(final Context ctx, final String customerId, final String name, final String email,
                                   final String address) {
        ChaincodeStub stub = ctx.getStub();

        if (CustomerExists(ctx, customerId)) {
            String errorMessage = String.format("Customer %s already exists", customerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerTransferErrors.CUSTOMER_ALREADY_EXISTS.toString());
        }

        Customer customer = new Customer(customerId, name, email, address);
        String sortedJson = genson.serialize(customer);
        stub.putStringState(customerId, sortedJson);

        return customer;
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public Customer ReadCustomer(final Context ctx, final String customerId) {
        ChaincodeStub stub = ctx.getStub();
        String customerJson = stub.getStringState(customerId);

        if (customerJson == null || customerJson.isEmpty()) {
            String errorMessage = String.format("Customer %s does not exist", customerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerTransferErrors.CUSTOMER_NOT_FOUND.toString());
        }

        Customer customer = genson.deserialize(customerJson, Customer.class);
        return customer;
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public Customer UpdateCustomer(final Context ctx, final String customerId, final String name, final String email,
                             final String address) {
        ChaincodeStub stub = ctx.getStub();

        if (!CustomerExists(ctx, customerId)) {
            String errorMessage = String.format("Customer %s does not exist", customerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerTransferErrors.CUSTOMER_ALREADY_EXISTS.toString());
        }

        Customer newCustomer = new Customer(customerId, name, email, address);
        String sortedJson = genson.serialize(newCustomer);
        stub.putStringState(customerId, sortedJson);
        return newCustomer;
    }


    @Transaction(intent = Transaction.TYPE.SUBMIT)
    public void DeleteAsset(final Context ctx, final String customerId) {
        ChaincodeStub stub = ctx.getStub();

        if (!CustomerExists(ctx, customerId)) {
            String errorMessage = String.format("Customer %s does not exist", customerId);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, CustomerTransferErrors.CUSTOMER_NOT_FOUND.toString());
        }

        stub.delState(customerId);
    }


    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public boolean CustomerExists(final Context ctx, final String customerId) {
        ChaincodeStub stub = ctx.getStub();
        String assetJSON = stub.getStringState(customerId);

        return (assetJSON != null && !assetJSON.isEmpty());
    }

    @Transaction(intent = Transaction.TYPE.EVALUATE)
    public String GetAllCustomers(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();

        List<Customer> queryResults = new ArrayList<Customer>();

        QueryResultsIterator<KeyValue> results = stub.getStateByRange("", "");

        for (KeyValue result: results) {
            Customer customer = genson.deserialize(result.getStringValue(), Customer.class);
            System.out.println(customer);
            queryResults.add(customer);
        }

        final String response = genson.serialize(queryResults);

        return response;
    }
}
