package ro.ase.ism.xrp.jcconnector;

import com.google.common.hash.Hashing;
import com.oracle.javacard.ams.AMService;
import com.oracle.javacard.ams.AMServiceFactory;
import com.oracle.javacard.ams.AMSession;
import com.oracle.javacard.ams.config.AID;
import com.oracle.javacard.ams.config.CAPFile;
import com.oracle.javacard.ams.script.APDUScript;
import com.oracle.javacard.ams.script.ScriptFailedException;
import com.oracle.javacard.ams.script.Scriptable;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.IntStream;
import javax.smartcardio.*;


public class CardCreation {
    static final String isdAID = "aid:A000000151000000";
    static final String sAID_CAP = "aid:AABBCCDDEEAAA000";
    static final String sAID_AppletClass = "aid:AABBCCDDEEAAA001";
    static final String sAID_AppletInstance = "aid:AABBCCDDEEAAA001";

    static final CommandAPDU selectApplet = new CommandAPDU(0x00, 0xA4, 0x04, 0x00, AID.from(sAID_AppletInstance).toBytes(), 256);

    public static void main(String[] args) {
        try{
            CAPFile appFile = CAPFile.from(getArg(args, "cap"));
            Properties props = new Properties();
            props.load(new FileInputStream(getArg(args, "props")));
            AMService ams = AMServiceFactory.getInstance("GP2.2");
            ams.setProperties(props);
            for (String key : ams.getPropertiesKeys()) {
                System.out.println(key + " = " + ams.getProperty(key));
            }

            AMSession deploy = ams.openSession(isdAID)   // select SD & open secure channel
                    .load(sAID_CAP, appFile.getBytes())  // load an application file
                    .install(sAID_CAP,                   // install application
                            sAID_AppletClass, sAID_AppletInstance)
                    .close();

            AMSession close = ams.openSession(isdAID) // select SD & open secure channel
                    //.uninstall(sAID_AppletInstance)      // uninstall the application
                    //.unload(sAID_CAP)
                    .close();

            byte[] test = new byte[]{0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
                    0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x01};
            byte[] testHash = Arrays.copyOfRange(Hashing.sha512().hashBytes(test).asBytes(),0,32);

            byte[] testParam = new byte[testHash.length+1];

            testParam[0] = (byte) testHash.length;
            System.arraycopy(testHash,0,testParam,1,testHash.length);

            TestScript testScript = new TestScript()

                    .append( deploy )
                    .append( selectApplet )
                    //.append(new CommandAPDU(0x80, 0x10,0x00,0x00, testParam))
                    .append(new CommandAPDU(0x80, 0x30,0x00,0x00, new byte[]{0x13, 0x07, 0x60, 0x37, 0x38, (byte) 0xC7, 0x0B, (byte) 0x99, 0x38, (byte) 0xBA, (byte) 0xE9, (byte) 0xCD, (byte) 0xB3, 0x4B, 0x05, (byte) 0xC3, (byte) 0xD7, 0x79, (byte) 0xAD, (byte) 0xBC, 0x49, 0x0C, (byte) 0xE1, 0x55, 0x6E, 0x2C, 0x26, (byte) 0x94, (byte) 0xC9, (byte) 0x8C, 0x0C, (byte) 0xE3}))
                    //.append(new CommandAPDU(0x80, 0x10,0x00,0x00, new byte[]{0x13, 0x07, 0x60, 0x37, 0x38, (byte) 0xC7, 0x0B, (byte) 0x99, 0x38, (byte) 0xBA, (byte) 0xE9, (byte) 0xCD, (byte) 0xB3, 0x4B, 0x05, (byte) 0xC3, (byte) 0xD7, 0x79, (byte) 0xAD, (byte) 0xBC, 0x49, 0x0C, (byte) 0xE1, 0x55, 0x6E, 0x2C, 0x26, (byte) 0x94, (byte) 0xC9, (byte) 0x8C, 0x0C, (byte) 0xE3}))//.append(new CommandAPDU(0x80,0x11,0x00,0x00, testParam )
                     //       )
                    .append(new CommandAPDU(0x80,0x20,0x00,0x00))
                    .append(close);

            CardTerminal t = getTerminal("socket", "127.0.0.1", "9025");
            Card c = t.connect("*");
            List<ResponseAPDU> responses = testScript.run(c.getBasicChannel());
            c.disconnect(true);

            System.out.println("Response count: " + responses.size());


        } catch (IOException | NoSuchProviderException | CardException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    private static String getArg(String[] args, String argName) throws IllegalArgumentException {
        String value = null;

        for (String param : args) {
            if (param.startsWith("-" + argName + "=")) {
                value = param.substring(param.indexOf('=') + 1);
            }
        }

        if(value == null || value.length() == 0) {
            throw new IllegalArgumentException("Argument " + argName + " is missing");
        }
        return value;
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

    private static class TestScript extends APDUScript {
        private List<CommandAPDU> commands = new LinkedList<>();
        private List<ResponseAPDU> responses = new LinkedList<>();
        private int index = 0;

        public List<ResponseAPDU> run(CardChannel channel) throws ScriptFailedException {
            return super.run(channel, c -> lookupIndex(c), r -> !isExpected(r));
        }

        @Override
        public TestScript append(Scriptable<CardChannel, CommandAPDU, ResponseAPDU> other) {
            super.append(other);
            return this;
        }

        public TestScript append(CommandAPDU apdu, ResponseAPDU expected) {
            super.append(apdu);
            this.commands.add(apdu);
            this.responses.add(expected);
            return this;
        }

        public TestScript append(CommandAPDU apdu) {
            super.append(apdu);
            return this;
        }

        private CommandAPDU lookupIndex(CommandAPDU apdu) {
            print(apdu);
            this.index = IntStream.range(0, this.commands.size())
                    .filter(i -> apdu == this.commands.get(i))
                    .findFirst()
                    .orElse(-1);
            return apdu;
        }

        private boolean isExpected(ResponseAPDU response) {

            ResponseAPDU expected = (index < 0)? response : this.responses.get(index);
            if (!response.equals(expected)) {
                System.out.println("Received: ");
                print(response);
                System.out.println("Expected: ");
                print(expected);
                return false;
            }
            print(response);
            return true;
        }

        private static void print(CommandAPDU apdu) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%02X%02X%02X%02X %02X[", apdu.getCLA(), apdu.getINS(), apdu.getP1(), apdu.getP2(), apdu.getNc()));
            for (byte b : apdu.getData()) {
                sb.append(String.format("%02X", b));
            }
            sb.append("]");
            System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-C] %2$s %n", System.currentTimeMillis(), sb.toString());
        }

        private static void print(ResponseAPDU apdu) {
            byte[] bytes = apdu.getData();
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02X", b));
            }
            System.out.format("[%1$tF %1$tT %1$tL %1$tZ] [APDU-R] [%2$s] SW:%3$04X %n", System.currentTimeMillis(), sb.toString(), apdu.getSW());
        }
    }


}
