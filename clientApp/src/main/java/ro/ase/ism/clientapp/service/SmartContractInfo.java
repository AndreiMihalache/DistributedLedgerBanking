package ro.ase.ism.clientapp.service;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class SmartContractInfo {
    private String bankName;
    private String contractName;
    private String channelName;
}
