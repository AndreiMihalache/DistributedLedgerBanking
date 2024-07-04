package ro.ase.ism.clientapp.request;

import lombok.Data;

@Data
public class CreateAssetRequest {
    private String bank;
    private String channel;
    private String assetID;
    private String color;
    private String size;
    private String owner;
    private String appraisedValue;
}
