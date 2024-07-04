package ro.ase.ism.clientapp.controller;

import org.springframework.web.bind.annotation.*;
import ro.ase.ism.clientapp.request.CreateAssetRequest;
import ro.ase.ism.clientapp.service.FabricService;

@RestController
@RequestMapping("/contracts")
public class ContractController {

    private final FabricService fabricService;

    public ContractController(FabricService fabricService) {
        this.fabricService = fabricService;
    }

    @PostMapping("/createAsset")
    public String createAsset(@RequestBody CreateAssetRequest request) {
        return fabricService.createAsset(
                request.getBank(),
                request.getChannel(),
                request.getAssetID(),
                request.getColor(),
                request.getSize(),
                request.getOwner(),
                request.getAppraisedValue()
        );
    }

    @GetMapping("/readAsset")
    public String readAsset(@RequestParam String bank, @RequestParam String channel, @RequestParam String assetID) {
        return fabricService.readAsset(bank, channel, assetID);
    }

    @GetMapping("/readAllAssets")
    public String readAllAssets(@RequestParam String bank, @RequestParam String channel) {
        return fabricService.readAllAssets(bank, channel);
    }

}
