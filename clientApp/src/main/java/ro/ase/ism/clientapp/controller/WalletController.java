package ro.ase.ism.clientapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import ro.ase.ism.clientapp.service.WalletService;
import ro.ase.ism.clientapp.service.XRPLService;
import ro.ase.ism.clientapp.wallet.JCWallet;

import java.math.BigDecimal;

@Controller
public class WalletController {


    private final JCWallet jcwallet;


    private final WalletService walletService;


    private final XRPLService xrplService;

    public WalletController(JCWallet jcwallet, WalletService walletService, XRPLService xrplService) {
        this.jcwallet = jcwallet;
        this.walletService = walletService;
        this.xrplService = xrplService;
    }

    @PostMapping("/connect")
    public String connect(@RequestParam String host, @RequestParam String port, RedirectAttributes attributes)
    {
        walletService.init(host, port);
        walletService.initJCWallet(jcwallet);
        xrplService.init();

        AccountInfoResult accountInfo = xrplService.getAccountInfo(jcwallet.getClassicAddress());
        BigDecimal balance = accountInfo.accountData().balance().toXrp();

        attributes.addFlashAttribute("cardConnected", true);
        attributes.addFlashAttribute("host", host);
        attributes.addFlashAttribute("port", port);
        attributes.addFlashAttribute("address", jcwallet.getClassicAddress().value());
        attributes.addFlashAttribute("balance", balance);
        return "redirect:/home";
    }

    @PostMapping("/transaction")
    public String transaction(@RequestParam String destination, @RequestParam String amount, RedirectAttributes attributes)
    {
        xrplService.transferFunds(jcwallet.getClassicAddress(), jcwallet.getPublicKey(), Long.valueOf(amount), Address.of(destination));


        AccountInfoResult accountInfo = xrplService.getAccountInfo(jcwallet.getClassicAddress());

        BigDecimal balance = accountInfo.accountData().balance().toXrp();

        attributes.addFlashAttribute("cardConnected", true);
        attributes.addFlashAttribute("host", walletService.getCurrentHost());
        attributes.addFlashAttribute("port", walletService.getCurrentPort());
        attributes.addFlashAttribute("address", jcwallet.getClassicAddress().value());
        attributes.addFlashAttribute("balance", balance);
        return "redirect:/home";

    }

}
