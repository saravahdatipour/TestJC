package personalizationTerminal;

//import javacard.security.Signature;
import test.test; //import your javacard applet
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import javacard.framework.AID;

        import javax.smartcardio.*;

import java.io.*;
import java.math.BigInteger;
        import java.security.SecureRandom;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;
import java.util.Arrays;

import static KeyUtils.KeyUtils.loadPrivateKeyFromFile;
import static KeyUtils.KeyUtils.loadPublicKeyFromFile;
import static POSTerminal.POSTerminal.readCertTerminalFromFile;


public class personalizationTerminal {
    //    =================================== Global vars for instruction bytes to send to the card =====================================

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



    public byte[] startCardPersonalization(){ //wakes card, receives public key

        CommandAPDU apdu = new CommandAPDU(0,PersonalizationTerminal_MSG1,0,0);

        try {
            ResponseAPDU response = applet.transmit(apdu);
            byte[] cardPublicKeybytes = response.getData();
            System.out.println("Raw content of the command: " + toHexString(apdu.getBytes()));
            System.out.println("Raw content of the response: " + toHexString(response.getBytes()));

            return cardPublicKeybytes;
        }
        catch (CardException e){
            return null;
        }
    }//end sendPersonalizationMSG2
    public ResponseAPDU personalizeCard(byte[] cardPublicKeybytes) throws Exception {
        // =================== GENERATE ID ===========================
        CardIdGenerator generator = new CardIdGenerator();
        short cardid = generator.getNextId();
        byte[] cardidbytes = new byte[2];
        cardidbytes[1] = (byte) cardid; //big endian
        cardidbytes[0] = (byte) (cardid >> 8);
        System.out.println("Current Card_Id: " + cardidbytes[0] + cardidbytes[1] );
        // =================== LOAD (master,MASTER) ===========================
        ECPublicKey publicKey = loadPublicKeyFromFile("Keys/MasterKeys/public.key");
        ECPrivateKey privateKey = loadPrivateKeyFromFile("Keys/MasterKeys/private.key");
        // Get public key point W
        ECPoint W = publicKey.getW();
        BigInteger Wx = W.getAffineX();
        BigInteger Wy = W.getAffineY();
        byte[] Wxba = toUnsignedByteArray(Wx);
        byte[] Wyba = toUnsignedByteArray(Wy);
        // Handle the case when Wxba and Wyba are less than 24 bytes
        byte[] Wtosend = new byte[1 + Wxba.length + Wyba.length];
        // add the 0x04 byte -> encoding for uncompressed coords
        Wtosend[0] = (byte) 4;
        //put the x and y in Wtosend
        System.arraycopy(Wxba, 0, Wtosend, 1, Wxba.length);
        System.arraycopy(Wyba, 0, Wtosend, 1 + Wxba.length, Wyba.length);
        // ===========================================================================

        // =================== SIGN card public key with (master) ===========================
        // message = card public key to be signed
        byte[] message = new byte[cardPublicKeybytes.length + cardidbytes.length];

        System.arraycopy(cardPublicKeybytes, 0, message, 0, cardPublicKeybytes.length);
        System.arraycopy(cardidbytes, 0, message, cardPublicKeybytes.length, cardidbytes.length);

        System.out.println("pubkey and id: " + Arrays.toString(message));


        // Sign the message
        Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
        signature.initSign(privateKey, new SecureRandom());
        signature.update(message);
        byte[] sigBytes = signature.sign();

        //putting everything together in buffer to send
        byte[] combined = new byte[Wtosend.length + sigBytes.length + cardidbytes.length];
        System.arraycopy(Wtosend, 0, combined, 0, Wtosend.length);
        System.arraycopy(sigBytes, 0, combined, Wtosend.length, sigBytes.length);
        System.arraycopy(cardidbytes, 0, combined, Wtosend.length + sigBytes.length, cardidbytes.length);
        System.out.println(toHexString(cardidbytes));
        CommandAPDU apdu = new CommandAPDU(0,PersonalizationTerminal_MSG2,0,0,combined);

        //TODO: add cardid to not renting log
//        BYTE LC LENGTH OF COMMAND DATA WILL BE AUTOMATICALLY DETERMINED SO AFTER P1 P2 JUST GIVE DATA BYTE ARRAY
//        MAXIMUM BYTES DATA CAN FIT IS 255 BYTES.

        try {
            ResponseAPDU response = applet.transmit(apdu);
            System.out.println("Raw content of the command: " + toHexString(apdu.getBytes()));
            System.out.println("Raw content of the response: " + toHexString(response.getBytes()));

            return null;
        }
        catch (CardException e){
            return null;
        }
    }//end sendPersonalizationMSG2


    public void personalizeTerminals(ECPrivateKey master) throws Exception { //for now personalizes POS.
        // receive public key from the java terminal
        ECPublicKey POSpublickey = loadPublicKeyFromFile("Keys/POSKeys/public.key");
        ECPoint POS_W =  POSpublickey.getW();
        BigInteger Wx = POS_W.getAffineX();
        BigInteger Wy = POS_W.getAffineY();
        byte[] Wxba = toUnsignedByteArray(Wx);
        byte[] Wyba = toUnsignedByteArray(Wy);
        byte[] POS_Wba = new byte[ Wxba.length + Wyba.length];
        System.arraycopy(Wxba, 0, POS_Wba, 0, Wxba.length);
        System.arraycopy(Wyba, 0, POS_Wba, Wxba.length, Wyba.length);

        //create Cert_Terminal (sign the terminal public key with MasterPrivate.key)
        // Sign the message
        Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
        signature.initSign(master, new SecureRandom());
        signature.update(POS_Wba);
        byte[] Cert_Terminal = signature.sign();

        // Write Cert_Terminal to a file
        try (FileOutputStream fos = new FileOutputStream("Cert_Terminal.txt")) {
            fos.write(Cert_Terminal);
        } catch (IOException e) {
            throw new Exception("Unable to write Cert_Terminal to a file", e);
        }
    }

    public static void main(String[] arg) throws Exception {
        System.out.println("Main of test Terminal is running");
        personalizationTerminal personalization_terminal = new personalizationTerminal();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("waiting failed?");
        }
        //if personalizing a card:
        byte[] cardPublicKeybytes = personalization_terminal.startCardPersonalization();
        personalization_terminal.personalizeCard(cardPublicKeybytes);

        //if personalizing a java terminal:
        ECPrivateKey master = loadPrivateKeyFromFile("Keys/MasterKeys/private.key");
        personalization_terminal.personalizeTerminals(master);

// TODO: has to be adapted to run on command line

//        personalization_terminal.sendPersonalizationMSG2();
        System.out.println("Card inserted applet selected");

    }



    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }


    public static byte[] toUnsignedByteArray(BigInteger value) {
        byte[] originalByteArray = value.toByteArray();
        if (originalByteArray[0] == 0) {
            byte[] unsignedByteArray = new byte[originalByteArray.length - 1];
            System.arraycopy(originalByteArray, 1, unsignedByteArray, 0, unsignedByteArray.length);
            return unsignedByteArray;
        } else {
            return originalByteArray;
        }
    }
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

}

