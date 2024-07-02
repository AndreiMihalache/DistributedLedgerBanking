package ro.ase.ism.clientapp.service;

import jakarta.transaction.Transactional;
import lombok.extern.log4j.Log4j2;
import org.hyperledger.fabric.client.Contract;
import org.hyperledger.fabric.client.Gateway;
import org.hyperledger.fabric.client.GatewayException;
import org.hyperledger.fabric.client.Network;
import org.springframework.stereotype.Service;
import ro.ase.ism.clientapp.entity.Bank;
import ro.ase.ism.clientapp.entity.User;
import ro.ase.ism.clientapp.repo.BankRepository;
import ro.ase.ism.clientapp.repo.UserRepository;
import ro.ase.ism.clientapp.util.Utils;

import java.util.List;
import java.util.Optional;

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
    public void enrollKYC(String bankName, String id,String name, String email, String address)
    {
        Optional<Bank> optionalBank = bankRepository.findByName(bankName);
        Optional<User> optionalUser = userRepository.findByEmail(email);

        if(optionalBank.isEmpty() || optionalUser.isEmpty()) throw new IllegalArgumentException("Invalid bank or user");
        else {
            Bank bank = optionalBank.get();
            List<String> channels = fabricIdentityService.getBankChannels(bank.getName());
            for(String channel: channels)
            {
                if(channel.toLowerCase().contains("kycnetwork")){
                    try{
                        Gateway gateway = fabricIdentityService.getGateway(bank.getName());
                        Network fabricNetwork = gateway.getNetwork(channel);
                        Contract contract = fabricNetwork.getContract("kycchaincode");
                        contract.submitTransaction("CreateCustomer", id, name, email, address);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }


            User user  = optionalUser.get();
            user.setBank(bank.getName());
            userRepository.save(user);
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
            for(String channel: channels){
                if(channel.toLowerCase().contains("kycnetwork")){
                    try{
                        Gateway gateway = fabricIdentityService.getGateway(bank.getName());
                        Network fabricNetwork = gateway.getNetwork(channel);
                        Contract contract = fabricNetwork.getContract("kycchaincode");
                        byte[] result = contract.evaluateTransaction("CustomerExists", String.valueOf(user.getId()));
                        log.info(utils.prettyJson(result));

                        return result.length > 0;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
        return false;
    }
}
