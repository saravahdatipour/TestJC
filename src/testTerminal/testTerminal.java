package testTerminal;

import test.test; //import your javacard applet
import com.licel.jcardsim.smartcardio.CardSimulator;
import com.licel.jcardsim.smartcardio.CardTerminalSimulator;
import javacard.framework.AID;


import javax.smartcardio.*;
import java.util.Arrays;


public class testTerminal {
    private final static byte HELLOMSG = (byte) 1; // TEST MESSAGE 1
    private static final long serialVersionUID = 1L;
    static final byte[] TEST_APPLET_AID = {(byte) 0x3B, (byte) 0x29, (byte) 0x63, (byte) 0x61, (byte) 0x6C, (byte) 0x63, (byte) 0x01};
    static final String TEST_APPLET_AID_string = "3B2963616C6301";

    static final CommandAPDU SELECT_APDU = new CommandAPDU((byte) 0x00, (byte) 0xA4, (byte) 0x04, (byte) 0x00, TEST_APPLET_AID);

    CardChannel applet;

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
    public testTerminal() {
        (new SimulatedCardThread()).start();}

    public ResponseAPDU sendHello(){
    CommandAPDU apdu = new CommandAPDU(0,HELLOMSG,0,0,256);

    try {
        ResponseAPDU response = applet.transmit(apdu);
//        System.out.println(response.getSW()); //should be 36864, in hex would be 9000 -> successful status code
        short card_id = 137; //generate this from a file
        byte[] data = response.getData();
        System.out.println("Received Data From Applet in Array: " + Arrays.toString(data));
        System.out.println("Command APDU: " + apdu.toString());
        System.out.println("Raw content of the command: " + toHexString(apdu.getBytes()));
        System.out.println("Response APDU: " + response.toString());
        System.out.println("Raw content of the response: " + toHexString(response.getBytes()));

        return null;
    }
    catch (CardException e){
        return null;
    }//end exception
    }//end sendHello



    public static void main(String[] arg) {
        System.out.println("Main of test Terminal is running");
        testTerminal test_terminal = new testTerminal();

        try {
            Thread.sleep(1000);
        } catch (Exception e) {
            System.err.println("waiting failed?");
        }
        test_terminal.sendHello();
        System.out.println("sendHello is done");
        System.out.println("Card inserted applet selected");

    }
    public static String toHexString(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b: bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString();
    }
}
