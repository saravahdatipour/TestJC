package personalizationTerminal;

import test.test; //import your javacard applet
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import javacard.framework.AID;
import KeyUtils.KeyUtils.*;
import javax.smartcardio.*;

import java.io.*;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;

import static KeyUtils.KeyUtils.loadPrivateKeyFromFile;
import static KeyUtils.KeyUtils.loadPublicKeyFromFile;


public class personalizationTerminal {
    //    =================================== Global vars for instruction bytes to send to the card =====================================
    private final static byte HELLOMSG= (byte) 0; // tell card to generate and send public key

    private final static byte PersonalizationTerminal_MSG1 = (byte) 1; // tell card to generate and send public key
    private final static byte PersonalizationTerminal_MSG2 = (byte) 2; // send signed public key (cert), Master public key, ID of user
    //    =================================================================================================================================
    private final static byte MutualAuth_MSG1 = (byte) 3; // send card public key of the terminal


    //    =================================== Global vars for applet ======================================================================
    private static final long serialVersionUID = 1L;
    static final byte[] TEST_APPLET_AID = {(byte) 0x3B, (byte) 0x29, (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01};
    static final String TEST_APPLET_AID_string = "3B2963616C6301";

    static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, TEST_APPLET_AID);

    CardChannel applet;
//    =================================================================================================================================



    class SimulatedCardThread extends Thread {
        public void run() {
            // Obtain a CardTerminal
            CardTerminals cardTerminals = CardTerminalSimulator.terminals("My terminal 1");
            CardTerminal terminal1 = cardTerminals.getTerminal("My terminal 1");

            // Create simulator and install applet
            CardSimulator simulator = new CardSimulator();
            AID testAppletAID = new AID(TEST_APPLET_AID, (byte) 0, (byte) 7);
            simulator.installApplet(testAppletAID, test.class);

            // Insert Card into "My terminal 1"
            simulator.assignToTerminal(terminal1);

            try {
                Card card = terminal1.connect("*");

                applet = card.getBasicChannel();
                ResponseAPDU resp = applet.transmit(SELECT_APDU);
                if (resp.getSW1() != 144) {
                    throw new Exception("Select failed");
                }
            } catch (Exception e) {
                System.err.println("Card status problem!");
//                System.err.println(e);
            }
            System.err.println("Successfully connected");
        }
    }


    public personalizationTerminal() {
        (new SimulatedCardThread()).start();}

    public class CardIdGenerator {
        private static final String FILE_NAME = "last_id.txt";
        private short lastId;

        public CardIdGenerator() {
            try {
                File file = new File(FILE_NAME);
                if (file.exists()) {
                    BufferedReader reader = new BufferedReader(new FileReader(file));
                    lastId = Short.parseShort(reader.readLine());
                    reader.close();
                } else {
                    lastId = 0;
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to initialize generator", e);
            }
        }

        public short getNextId() {
            if (lastId == Short.MAX_VALUE) {
                throw new IllegalStateException("All IDs have been used");
            }

            short nextId = ++lastId;

            try {
                BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME));
                writer.write(String.valueOf(nextId));
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Failed to save next id", e);
            }
            return nextId;
        }
    }
    public ResponseAPDU sendPersonalizationMSG1(){ //has to be edited to MSG2. MSG1 should only initialize the protocol


//        BYTE LC LENGTH OF COMMAND DATA WILL BE AUTOMATICALLY DETERMINED SO AFTER P1 P2 JUST GIVE DATA BYTE ARRAY
//        MAXIMUM BYTES DATA CAN FIT IS 255 BYTES.
        CommandAPDU apdu = new CommandAPDU(0,PersonalizationTerminal_MSG1,0,0);

        try {
            ResponseAPDU response = applet.transmit(apdu);

            byte[] responsedata = response.getData();
            System.out.println("Raw content of the command: " + toHexString(apdu.getBytes()));
            return null;
        }
        catch (CardException e){
            return null;
        }//end exception
    }//end sendPersonalizationMSG2
    public ResponseAPDU sendPersonalizationMSG2() throws Exception { //has to be edited to MSG2. MSG1 should only initialize the protocol
        // =================== GENERATE ID ===========================
        CardIdGenerator generator = new CardIdGenerator();
        short cardid = generator.getNextId();
        byte[] cardidbytes = new byte[4];
        cardidbytes[0] = (byte) cardid;
        cardidbytes[1] = (byte) (cardid >> 8);
        System.out.println("cardid in bytearray : " + cardidbytes[0] + cardidbytes[1] );
        // =================== LOAD (master,MASTER) ===========================
        ECPublicKey publicKey = loadPublicKeyFromFile("public.key");
        ECPrivateKey privateKey = loadPrivateKeyFromFile("private.key");
        //-------getting all the params-----------
        ECParameterSpec ecParameterSpec = publicKey.getParams();
        BigInteger a = ecParameterSpec.getCurve().getA();
        BigInteger b = ecParameterSpec.getCurve().getB();
        BigInteger p = ((java.security.spec.ECFieldFp) ecParameterSpec.getCurve().getField()).getP();
        BigInteger n = ecParameterSpec.getOrder();
        BigInteger Gx = ecParameterSpec.getGenerator().getAffineX();
        BigInteger Gy = ecParameterSpec.getGenerator().getAffineY();
        int h = ecParameterSpec.getCofactor();
        int keySize = n.bitLength();  // Get key size in bits
        // Get public key point W
        ECPoint W = publicKey.getW();
        BigInteger Wx = W.getAffineX();
        BigInteger Wy = W.getAffineY();
        //-------------------------------------------
        // Create APDU command
// ...
        int totallength = (4 + 2 + toUnsignedByteArray(a, keySize / 8).length + toUnsignedByteArray(b, keySize / 8).length+
                toUnsignedByteArray(p, keySize / 8).length +toUnsignedByteArray(n, keySize / 8).length + toUnsignedByteArray(Gx, keySize / 8).length
        + toUnsignedByteArray(Gy, keySize / 8).length + 1 + toUnsignedByteArray(Wx, keySize / 8).length + toUnsignedByteArray(Wy, keySize / 8).length);
        byte[] apduBytes = ByteBuffer.allocate(totallength)
                .put((byte) 0x00)  // CLA
                .put((byte) 0x22)  // INS
                .putShort((short) keySize)
                .put(toUnsignedByteArray(a, keySize / 8))
                .put(toUnsignedByteArray(b, keySize / 8))
                .put(toUnsignedByteArray(p, keySize / 8))
                .put(toUnsignedByteArray(n, keySize / 8))
                .put(toUnsignedByteArray(Gx, keySize / 8))
                .put(toUnsignedByteArray(Gy, keySize / 8))
                .put((byte) 0x04)  // Indicate that G and W are uncompressed
                .put(toUnsignedByteArray(Wx, keySize / 8))
                .put(toUnsignedByteArray(Wy, keySize / 8))
                .put((byte) h)
                .array();
        System.out.println("this is the length of the buffer: " + totallength);
// Now you can use apduBytes to create CommandAPDU:
        CommandAPDU apdu = new CommandAPDU(0x00, PersonalizationTerminal_MSG2, 0, 0, apduBytes);
        System.out.println("actual sent data is this long: " + apdu.getData().length);

        System.out.println(toUnsignedByteArray(Wy, keySize / 8).length);

        // =================== SIGN card public key with (master) ===========================

//        BYTE LC LENGTH OF COMMAND DATA WILL BE AUTOMATICALLY DETERMINED SO AFTER P1 P2 JUST GIVE DATA BYTE ARRAY
//        MAXIMUM BYTES DATA CAN FIT IS 255 BYTES.
//        CommandAPDU apdu = new CommandAPDU(0,PersonalizationTerminal_MSG2,0,0,cardidbytes,0);

        try {
            ResponseAPDU response = applet.transmit(apdu);
//        System.out.println(response.getSW()); //should be 36864, in hex would be 9000 -> successful status code
            short card_id = 137; //generate this from a file
            byte[] responsedata = response.getData();
            System.out.println("Raw content of the command: " + toHexString(apdu.getBytes()));
            return null;
        }
        catch (CardException e){
            return null;
        }//end exception
    }//end sendPersonalizationMSG2




    public static void main(String[] arg) throws Exception {
        System.out.println("Main of test Terminal is running");
        personalizationTerminal personalization_terminal = new personalizationTerminal();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("waiting failed?");
        }
        personalization_terminal.sendPersonalizationMSG2();
        System.out.println("Card inserted applet selected");

        ECPublicKey publicKey = loadPublicKeyFromFile("public.key");
        ECParameterSpec ecParameterSpec = publicKey.getParams();
        EllipticCurve curve = ecParameterSpec.getCurve();
        ECFieldFp field = (ECFieldFp) curve.getField();
        BigInteger p = field.getP();

//        // Print out the parameters and public key coordinates
//        System.out.println("Curve: " +
//                "a=" + curve.getA() + //need to send 256 bits 32 byte
//                ", b=" + curve.getB()); //need to send 256 bits
//        System.out.println("Field size: " + field.getFieldSize());
//        System.out.println("Prime field: " + p); //need to send 256 bits
//        System.out.println("Generator (G): " +  //need to send
//                "x=" + ecParameterSpec.getGenerator().getAffineX() + //255 bits
//                ", y=" + ecParameterSpec.getGenerator().getAffineY());//255 bits
//        System.out.println("Order (n): " + ecParameterSpec.getOrder()); //need to send 255
//        System.out.println("Cofactor (h): " + ecParameterSpec.getCofactor()); // 1 byte
//        System.out.println("Public Key Coordinates:");
//        System.out.println("public key x coordinate: " + publicKey.getW().getAffineX()); //need to send 256
//        System.out.println("public key y coordinate: " + publicKey.getW().getAffineY()); //need to send 256?



    }
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }



    // Convert BigInteger to unsigned byte array of given size
    private static byte[] toUnsignedByteArray(BigInteger bi, int size) {
        byte[] bytes = bi.toByteArray();
        if (bytes.length == size) {
            return bytes;
        } else if (bytes.length == size + 1 && bytes[0] == 0) {
            return Arrays.copyOfRange(bytes, 1, size + 1);
        } else {
            byte[] result = new byte[size];
            System.arraycopy(bytes, 0, result, size - bytes.length, bytes.length);
            return result;
        }
    }


}
