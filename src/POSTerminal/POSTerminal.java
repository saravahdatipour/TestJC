package POSTerminal;

import test.test; //import your javacard applet
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import javacard.framework.AID;


import javax.smartcardio.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.Signature;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;

import static KeyUtils.KeyUtils.*;


public class POSTerminal {


    //    =================================== Global vars for applet ======================================================================
    private static final long serialVersionUID = 1L;
    static final byte[] TEST_APPLET_AID = {(byte) 0x3B, (byte) 0x29, (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01};
    static final String TEST_APPLET_AID_string = "3B2963616C6301";

    static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, TEST_APPLET_AID);

    CardChannel applet;
//    =============================================== Global Key Materials ===========================================================

    ECPublicKey terminalPublicKey;
    ECPrivateKey terminalPrivateKey;

    private byte[] Cert_Terminal;




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


    public POSTerminal() {
        (new SimulatedCardThread()).start();}

    public ResponseAPDU verifyCard(){

            return null;

    }//end verifyCard

    public ResponseAPDU rentCar(){
        //how to choose the car?
        //TODO: check if cardid is in not renting log
        //TODO: check if car id is in is_available list
        //TODO: log cardid rented car id and length of time of rental --> expected end-date will be = now + length of time of rental
        //TODO: create and send Certrent = Signb(CertCard, IDCar, e, Certpost)
        return null;

    }//end chooseCar

    public byte[] mutualAuthentication(){
        //1. verify public key of card
        verifyCard();
        //2. put public key of card in A
        //3. diffie-hellman between private key of self and public key of A
        //4.  challenge and response -> ? card proves to terminal it has S
        //4a.
        byte[] S = {1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23,24}; //shared secret will be same size az public key of the curve
        return S;

    }//end chooseCar

    public void getPersonalized() throws Exception {
        // load MasterPublic.key
        ECPublicKey Master = loadPublicKeyFromFile("Keys/MasterKeys/public.key");

        // Check if POS keys already exist
        File terminalPrivateKeyfile = new File("Keys/POSKeys/private.key");
        File terminalPublicKeyfile = new File("Keys/POSKeys/public.key");

        if (!terminalPrivateKeyfile.exists() || !terminalPublicKeyfile.exists()) {
            // Create key pair and save it to file if they do not exist
            KeyPair keyPair = generateKeyPair();
            saveKeyPairToFile(keyPair, "Keys/POSKeys/private.key", "Keys/POSKeys/public.key");
        }
        //loading POS PUB key in a byte array POS_Wba
        this.terminalPublicKey = loadPublicKeyFromFile("Keys/POSKeys/public.key");
//        this.terminalPrivateKey = loadPrivateKeyFromFile("Keys/POSKeys/private.key");


        ECPoint POS_W =  terminalPublicKey.getW();
        BigInteger Wx = POS_W.getAffineX();
        BigInteger Wy = POS_W.getAffineY();
        byte[] Wxba = toUnsignedByteArray(Wx);
        byte[] Wyba = toUnsignedByteArray(Wy);
        byte[] POS_Wba = new byte[ Wxba.length + Wyba.length];
        System.arraycopy(Wxba, 0, POS_Wba, 0, Wxba.length);
        System.arraycopy(Wyba, 0, POS_Wba, Wxba.length, Wyba.length);


        //after saving keys, public key is read by personalization terminal and a certificate of this is made at Cert_Terminal.txt
        try {
            File certTerminalFile = new File("Cert_Terminal.txt");
            if (certTerminalFile.exists()) {
                try {
                    this.Cert_Terminal = readCertTerminalFromFile("Cert_Terminal.txt");
                } catch (Exception e) {
                    System.out.println("Error reading from Cert_Terminal.txt");
                    e.printStackTrace();
                    return; // Return from the method or handle the error appropriately
                }
                //check if the certificate verifies with master public key
                boolean isSignatureValid = verifySignature(Master, POS_Wba, Cert_Terminal);
                System.out.println(" Cert_Terminal Verified: " + isSignatureValid);
            } else {
                System.out.println("Cert_Terminal.txt does not exist. Run the Personalization again.");
            }
        } catch (Exception e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }



    }




    public static void main(String[] arg) throws Exception {
        System.out.println("Main of test Terminal is running");
        POSTerminal posterminal = new POSTerminal();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("waiting failed?");
        }
        posterminal.getPersonalized();
        System.out.println("certificate of own public key loaded");

        posterminal.verifyCard();
        System.out.println("card verified");
        posterminal.rentCar();
        System.out.println("car chosen");


        System.out.println("Card inserted applet selected");

        //TODO: After a POST is personalised it also receives the list of all Certcar of all cars available on a particular location.
        //TODO: When this happens the POST verifies all the Certcar using M aster

    }
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }

    public static byte[] readCertTerminalFromFile(String filePath) throws Exception {
        File file = new File(filePath);
        byte[] Cert_Terminal = new byte[(int) file.length()];
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            fis.read(Cert_Terminal);
        } catch (IOException e) {
            throw new Exception("Unable to read Cert_Terminal from the file", e);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return Cert_Terminal;
    }

    public boolean verifySignature(ECPublicKey publicKey, byte[] message, byte[] signatureBytes) throws Exception {
        Signature signature = Signature.getInstance("SHA1withECDSA", "BC");
        signature.initVerify(publicKey);
        signature.update(message);
        return signature.verify(signatureBytes);
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

}
