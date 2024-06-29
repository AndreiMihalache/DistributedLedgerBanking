import com.owlike.genson.annotation.JsonProperty;
import org.hyperledger.fabric.contract.annotation.DataType;
import org.hyperledger.fabric.contract.annotation.Property;

import java.util.Objects;

@DataType()
public final class Customer {

    @Property()
    private final String customerId;

    @Property()
    private final String name;

    @Property()

    private final String email;

    @Property()

    private final String address;

    public String getCustomerId() {
        return customerId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getAddress() {
        return address;
    }

    public Customer(@JsonProperty("customerID") final String customerId, @JsonProperty("name") final String name,
                    @JsonProperty("email") final String email, @JsonProperty("address") final String address) {
        this.customerId = customerId;
        this.name = name;
        this.email = email;
        this.address = address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(
                new String[] {getCustomerId(), getName(), getEmail(), getAddress()},
                new String[] {customer.getCustomerId(),customer.getName(), customer.getEmail(), customer.getAddress()});
    }

    @Override
    public int hashCode() {
        return Objects.hash(customerId, name, email, address);
    }

    @Override
    public String toString() {
        return this.getClass().getSimpleName() + "@" + Integer.toHexString(hashCode()) + " [customerId=" + customerId + ", name="
                + name + ", email=" + email + ", address=" + address  + "]";
    }
}
