package ro.ase.ism.xrp.jcconnector;


import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.sun.javacard.apduio.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.checkerframework.checker.units.qual.A;

import javax.smartcardio.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
import java.util.Properties;


public class JCConnector {

    protected static final Logger logger = LogManager.getLogger();


    AMService ams;

    private  CardTerminal terminal;
    private  Card card;

    private  CardChannel channel;

    private static byte[] SELECT_HEADER = new byte[] {(byte)0x00, (byte)0xA4, (byte)0x04, (byte)0x00};
    private static byte[] WALLET_APPLET = new byte[] {(byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xAA, (byte)0xA0,(byte)0x01};

    private static byte[] SIGN = new byte[] {(byte)0x80, (byte)0x10, (byte)0x00, (byte)0x00};
    private static byte[] GET_PUBKEY = new byte[] {(byte)0x80, (byte)0x20, (byte)0x00, (byte)0x00};

    public JCConnector(String jcProperties) {
        try (FileInputStream fis = new FileInputStream(jcProperties)){
        Properties props = new Properties();
        props.load(fis);
        ams = AMServiceFactory.getInstance("GP2.2");
        ams.setProperties(props);
        }
        catch (IOException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }


    }

    public void connect(String host, String port) {
        try {
            terminal = getTerminal("socket", host, port);
            card = terminal.connect("*");
            channel = card.getBasicChannel();
            logger.info("Connected successfully");
        } catch (CardException | NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException(e);
        }

    }

    public void disconnect(){
        try{
            if (card!=null){
                card.disconnect(true);
                logger.info("Disconnected successfully");
            }} catch (CardException e) {
            throw new RuntimeException(e);
        }
    }

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

    public byte[] sign( byte[] data) {
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
        return responseAPDU.getBytes();}
        catch (CardException e){
                throw new RuntimeException(e);
            }
    }

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
            return responseAPDU.getBytes();
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
