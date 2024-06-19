package ro.ase.ism.xrp.jcconnector;


import com.oracle.javacard.ams.AMService;
import com.sun.javacard.apduio.Apdu;
import lombok.NoArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.smartcardio.*;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;


@NoArgsConstructor
public class JCConnectorImpl implements JCConnector {

    protected static final Logger logger = LogManager.getLogger();

    private  CardTerminal terminal;
    private  Card card;

    private  CardChannel channel;

    private String host;
    private String port;

    private static byte[] SELECT_HEADER = new byte[] {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00};
    private static byte[] WALLET_APPLET = new byte[] {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xAA, (byte)0xA0,(byte)0x01};

    private static byte[] SIGN = new byte[] {(byte)0x80, (byte)0x30, (byte)0x00, (byte)0x00};
    private static byte[] GET_PUBKEY = new byte[] {(byte)0x80, (byte)0x20, (byte)0x00, (byte)0x00};



    public JCConnectorImpl(String host, String port) {
        if(host!=null && port!= null)
        {
            this.host = host;
            this.port = port;
        }
    }

    @Override
    public void connect(String hostString, String portString) {
        try {
            terminal = getTerminal("socket", hostString, portString);
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            logger.info("Connected successfully");
        } catch (CardException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    public void connect(){
        try {
            if(this.host==null || this.port==null) throw new RuntimeException("Connector port and host cannot be null");
            terminal = getTerminal("socket", this.host, this.port);
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            logger.info("Connected successfully");
        } catch (CardException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void disconnect(){
        try{
            if (card!=null){
                card.disconnect(true);
                logger.info("Disconnected successfully");
            }} catch (CardException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void selectApplet(){
        try{
        Apdu selectApdu = new Apdu();
        selectApdu.command = SELECT_HEADER;
        selectApdu.setDataIn(WALLET_APPLET, 0x08);
        selectApdu.le = 0x00;
        CommandAPDU selectCommand  = new CommandAPDU(selectApdu.getCommandApduBytes());
        print(selectCommand);
        ResponseAPDU responseAPDU = channel.transmit(selectCommand);
        print(responseAPDU);
        checkApduCommandResponse(responseAPDU.getSW(), "SELECT APPLET");
        logger.info("Selected signature applet");}
        catch (CardException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public byte[] sign(byte[] data) {
        try{
        Apdu signApdu = new Apdu();
        signApdu.command = SIGN;
        signApdu.setDataIn(data, data.length);
        signApdu.le= 0x00;
        CommandAPDU signCommand = new CommandAPDU(signApdu.getCommandApduBytes());
        print(signCommand);
        ResponseAPDU responseAPDU = channel.transmit(signCommand);
        print(responseAPDU);
        checkApduCommandResponse(responseAPDU.getSW(), "SIGN");
        return responseAPDU.getData();}
        catch (CardException e){
                throw new RuntimeException(e);
            }
    }

    @Override
    public byte[] getPublicKey()
    {
        try{
            Apdu pubKeyApdu = new Apdu();
            pubKeyApdu.command= GET_PUBKEY;
            pubKeyApdu.le= (byte)0x00;
            CommandAPDU getPubKeyCommand = new CommandAPDU(pubKeyApdu.getCommandApduBytes());
            print(getPubKeyCommand);
            ResponseAPDU responseAPDU = channel.transmit(getPubKeyCommand);
            print(responseAPDU);
            checkApduCommandResponse(responseAPDU.getSW(),"GET PUBKEY");
            return responseAPDU.getData();
        } catch (CardException e) {
            throw new RuntimeException(e);
        }

    }

    private void checkApduCommandResponse(int sw, String stage){
        if(sw!=0x9000)
        {
            throw new RuntimeException("Card command failed at stage: +"+stage);
        }
    }

    private static CardTerminal getTerminal(String... connectionParams) throws NoSuchProviderException, CardException, NoSuchAlgorithmException {
        TerminalFactory tf;
        String connectivityType = connectionParams[0];
        if (connectivityType.equals("socket")) {
            String ipaddr = connectionParams[1];
            String port = connectionParams[2];
            tf = TerminalFactory.getInstance("SocketCardTerminalFactoryType",
                    List.of(new InetSocketAddress(ipaddr, Integer.parseInt(port))),
                    "SocketCardTerminalProvider");
        } else {
            tf = TerminalFactory.getDefault();
        }
        return tf.terminals().list().get(0);
    }

    private static void print(CommandAPDU apdu) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%02X%02X%02X%02X %02X[", apdu.getCLA(), apdu.getINS(), apdu.getP1(), apdu.getP2(), apdu.getNc()));
        for (byte b : apdu.getData()) {
            sb.append(String.format("%02X", b));
        }
        sb.append("]");
        System.out.format("[%1$tF %1$tT] [APDU-C] %2$s %n", System.currentTimeMillis(), sb.toString());
    }

    private static void print(ResponseAPDU apdu) {
        byte[] bytes = apdu.getData();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        System.out.format("[%1$tF %1$tT] [APDU-R] [%2$s] SW:%3$04X %n", System.currentTimeMillis(), sb.toString(), apdu.getSW());
    }

}
