package ro.ase.ism.clientapp.connector;

public interface JCConnector {

    void connect();
    void connect(String host, String port);

    void disconnect();

    void selectApplet();

    byte[] sign(byte[] data);

    byte[] getPublicKey();
}
