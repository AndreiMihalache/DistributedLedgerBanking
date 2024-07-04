package ro.ase.ism.clientapp.service;

import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.log4j.Log4j2;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.Network;
import org.springframework.stereotype.Service;
import ro.ase.ism.clientapp.entity.Bank;
import ro.ase.ism.clientapp.entity.User;
import ro.ase.ism.clientapp.repo.BankRepository;
import ro.ase.ism.clientapp.repo.UserRepository;
import ro.ase.ism.clientapp.util.Utils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Log4j2
public class FabricService {

    private final BankRepository bankRepository;
    private final UserRepository userRepository;
    private final FabricIdentityService fabricIdentityService;

    private final Utils utils;

    public FabricService(BankRepository bankRepository, UserRepository userRepository, FabricIdentityService fabricIdentityService, Utils utils)
    {
        this.bankRepository = bankRepository;
        this.userRepository = userRepository;
        this.fabricIdentityService =fabricIdentityService;
        this.utils = utils;
    }

    @Transactional
    public boolean enrollKYC(String bankName, String id, String name, String email, String address)
    {
        Optional<Bank> optionalBank = bankRepository.findByName(bankName);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalBank.isEmpty() || optionalUser.isEmpty()) throw new IllegalArgumentException("Invalid bank or user");
        else {
            Bank bank = optionalBank.get();
            User user  = optionalUser.get();

            List<String> channels = fabricIdentityService.getBankChannels(bank.getName());

            Integer successCounter=0;
            Integer counter = 0;

            for(String channel: channels)
            {
                if(channel.toLowerCase().contains("kycnetwork")){
                    counter++;
                    try{
                        String networkIndex = channel.contains("1")? "1" :"2";
                        Gateway gateway = fabricIdentityService.getGateway(bank.getName());
                        Network fabricNetwork = gateway.getNetwork(channel);
                        Contract contract = fabricNetwork.getContract("kycchaincode"+networkIndex);
                        byte[] result = contract.submitTransaction("CreateCustomer", id, name, email, address);
                        log.info("Enroll result:" + utils.prettyJson(result).toString());
                        user.setBank(bank.getName());
                        userRepository.save(user);
                        successCounter++;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return counter.equals(successCounter);
        }
    }

    public boolean checkEnrollment(String bankName, String email)
    {
        Optional<Bank> optionalBank = bankRepository.findByName(bankName);
        Optional<User> optionalUser = userRepository.findByEmail(email);
        if(optionalBank.isEmpty() || optionalUser.isEmpty()) throw new IllegalArgumentException("Invalid bank or user");
        else{
            Bank bank = optionalBank.get();
            User user = optionalUser.get();
            List<String> channels = fabricIdentityService.getBankChannels(bankName);
            boolean flag = false;
            for(String channel: channels){
                if(channel.toLowerCase().contains("kycnetwork")){
                    try{
                        String networkIndex = channel.contains("1")? "1" :"2";
                        Gateway gateway = fabricIdentityService.getGateway(bank.getName());
                        Network fabricNetwork = gateway.getNetwork(channel);
                        Contract contract = fabricNetwork.getContract("kycchaincode"+networkIndex);
                        byte[] result = contract.evaluateTransaction("CustomerExists", String.valueOf(user.getId()));
                        if(utils.prettyJson(result).equals("true")) flag=true;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
            return flag;
        }
    }


    public List<SmartContractInfo> getSmartContracts(String email) {
        List<SmartContractInfo> smartContracts = new ArrayList<>();

        List<Bank> banks = bankRepository.findAll();


        for(Bank bank: banks)
        {
            String bankName = bank.getName();
            if(checkEnrollment(bankName, email))
            {
                List<String> channels = fabricIdentityService.getBankChannels(bankName);

                for (String channel : channels) {
                    if (channel.toLowerCase().contains("privatenetwork")) {
                        smartContracts.add(new SmartContractInfo(bankName, bankName+"TransferAsset", channel));
                    }
                }
            }
        }

        return smartContracts;
    }

    public String createAsset(String bankName, String channelName, String assetID, String color, String size, String owner, String appraisedValue) {
        try {
            Gateway gateway = fabricIdentityService.getGateway(bankName);
            Network fabricNetwork = gateway.getNetwork(channelName);
            Contract contract = fabricNetwork.getContract("transfer");
            byte[] result = contract.submitTransaction("CreateAsset", assetID, color, size, owner, appraisedValue);
            return utils.prettyJson(result).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String readAsset(String bankName, String channelName, String assetID) {
        try {
            Gateway gateway = fabricIdentityService.getGateway(bankName);
            Network fabricNetwork = gateway.getNetwork(channelName);
            Contract contract = fabricNetwork.getContract("transfer");
            byte[] result = contract.evaluateTransaction("ReadAsset", assetID);
            return utils.prettyJson(result).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String readAllAssets(String bankName, String channelName) {
        try {
            Gateway gateway = fabricIdentityService.getGateway(bankName);
            Network fabricNetwork = gateway.getNetwork(channelName);
            Contract contract = fabricNetwork.getContract("transfer");
            byte[] result = contract.evaluateTransaction("GetAllAssets");
            return utils.prettyJson(result).toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

