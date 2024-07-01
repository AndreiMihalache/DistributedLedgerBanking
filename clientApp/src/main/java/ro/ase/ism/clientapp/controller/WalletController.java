package ro.ase.ism.clientapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.bouncycastle.math.raw.Mod;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.xrpl.xrpl4j.model.client.accounts.AccountInfoResult;
import org.xrpl.xrpl4j.model.transactions.Address;
import ro.ase.ism.clientapp.connector.JCConnector;
import ro.ase.ism.clientapp.entity.Wallet;
import ro.ase.ism.clientapp.service.WalletService;
import ro.ase.ism.clientapp.service.XRPLService;
import ro.ase.ism.clientapp.wallet.JCWallet;

@Controller
public class WalletController {

    @Autowired
    private JCWallet jcwallet;

    @Autowired
    private WalletService walletService;

    @Autowired
    private XRPLService xrplService;

    @PostMapping("/connect")
    public String connect(@RequestParam String host, @RequestParam String port, Model model, RedirectAttributes attributes)
    {
        walletService.init(host, port);
        walletService.initJCWallet(jcwallet);
        xrplService.init();

        AccountInfoResult accountInfo = xrplService.getAccountInfo(jcwallet.getClassicAddress());
        attributes.addFlashAttribute("cardConnected", true);
        attributes.addFlashAttribute("host", host);
        attributes.addFlashAttribute("port", port);
        attributes.addFlashAttribute("address", jcwallet.getClassicAddress().value());
        attributes.addFlashAttribute("balance", accountInfo.accountData().balance().value().toString());
        return "redirect:/home";
    }

    @PostMapping("/transaction")
    public String transaction(@RequestParam String destination, @RequestParam String amount, Model model,RedirectAttributes attributes)
    {
        xrplService.transferFunds(jcwallet.getClassicAddress(), jcwallet.getPublicKey(), Long.valueOf(amount), Address.of(destination));

        AccountInfoResult accountInfo = xrplService.getAccountInfo(jcwallet.getClassicAddress());
        attributes.addFlashAttribute("cardConnected", true);
        attributes.addFlashAttribute("host", model.getAttribute("host"));
        attributes.addFlashAttribute("port", model.getAttribute("port"));
        attributes.addFlashAttribute("address", jcwallet.getClassicAddress().value());
        attributes.addFlashAttribute("balance", accountInfo.accountData().balance().value().toString());
        return "redirect:/home";

    }

}
