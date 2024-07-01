package ro.ase.ism.clientapp.properties;

import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;



@Value
@ConfigurationProperties("props")
public class Props{
    String ripple;
    String faucet;
}
