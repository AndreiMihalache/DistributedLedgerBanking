package ro.ase.ism.clientapp.properties;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.List;


@Value
@ConfigurationProperties("props")
@Validated
public class Props{
    @NotNull
    String ripple;
    @NotNull
    String faucet;
    @NotNull
    String fabricCryptoPath;
    @NotNull
    String bankAUser;
    @NotNull
    String bankBUser;
    @NotNull
    String bankCUser;
    @NotNull
    String bankAPeerEndpoint;
    @NotNull
    String bankBPeerEndpoint;
    @NotNull
    String bankCPeerEndpoint;
    @NotEmpty
    List<String> bankAChannels;
    @NotEmpty
    List<String> bankBChannels;
    @NotEmpty
    List<String> bankCChannels;
}
