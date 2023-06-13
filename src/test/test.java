package test;

import javacard.framework.*;
import javacard.security.CryptoException;
import javacard.security.ECPublicKey;
import javacard.security.KeyBuilder;
import javacard.security.KeyPair;

/**
 * @noinspection ClassNamePrefixedWithPackageName, ImplicitCallToSuper, MethodOverridesStaticMethodOfSuperclass, ResultOfObjectAllocationIgnored
 */
public class test extends Applet implements ISO7816 {

    static byte[] prime = {
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFE,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFE,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFF
    };

    // Coefficients a and b
    static byte[] a = {
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFE,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFE, (byte)0xFFFFFFFF,
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFC
    };
    static byte[] b = {
            (byte)0x6421, (byte)0x05E9, (byte)0xCE, (byte)0x18,
            (byte)0x21, (byte)0x96, (byte)0xD5, (byte)0x0A,
            (byte)0x90, (byte)0xE1, (byte)0x9A, (byte)0x39,
            (byte)0x3B, (byte)0x4B, (byte)0x67, (byte)0x08,
            (byte)0x60, (byte)0xD9, (byte)0x18, (byte)0x2B,
            (byte)0x21, (byte)0x00, (byte)0xD4, (byte)0xAF
    };
    static byte[] g = {
            (byte)0x04,
            (byte)0x18, (byte)0x8D, (byte)0xA8, (byte)0x0E, (byte)0xB0, (byte)0x30, (byte)0x90, (byte)0xF6,
            (byte)0x7C, (byte)0xBF, (byte)0x20, (byte)0xEB, (byte)0x43, (byte)0xA1, (byte)0x88, (byte)0x00,
            (byte)0xF4, (byte)0xFF, (byte)0x0A, (byte)0xFD, (byte)0x82, (byte)0xFF, (byte)0x10, (byte)0x12,
            (byte)0x07, (byte)0x19, (byte)0x2B, (byte)0x95, (byte)0xFF, (byte)0xC8, (byte)0xDA, (byte)0x78,
            (byte)0x63, (byte)0x10, (byte)0x11, (byte)0xED, (byte)0x6B, (byte)0x24, (byte)0xCD, (byte)0xD5,
            (byte)0x73, (byte)0xF9, (byte)0x77, (byte)0xA1, (byte)0x1E, (byte)0x79, (byte)0x48, (byte)0x11,
            (byte)0x9F, (byte)0x44, (byte)0x85, (byte)0x13, (byte)0x0D, (byte)0x24, (byte)0x2D, (byte)0x5C,
            (byte)0xB8, (byte)0x2F, (byte)0xF8, (byte)0x29, (byte)0x5C, (byte)0x14, (byte)0x14, (byte)0x94,
            (byte)0x7D, (byte)0x13, (byte)0xD3, (byte)0x9E, (byte)0x36, (byte)0xC9, (byte)0x54, (byte)0x20
    };
    // The order of the base point r
    static byte[] r = {
            (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0xFFFFFFFF, (byte)0x99,
            (byte)0xEF, (byte)0x8C, (byte)0xA1, (byte)0x61, (byte)0x10, (byte)0x3F,
            (byte)0x3E, (byte)0x63, (byte)0x7A, (byte)0x26, (byte)0x5E, (byte)0xFF,
            (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF,
            (byte)0xFF, (byte)0xFF
    };

    // The cofactor h
    static byte k = (byte)0x01;

    private final static byte HELLOMSG = (byte) 0; // TEST MESSAGE 1
    private final static byte GETMASTER = (byte) 5; // TEST MESSAGE 1


    protected test() {
        register();
    }

    public static void install(byte[] bArray, short bOffset, byte bLength) {
        new test();
    }

    /**
     * @noinspection UnusedDeclaration
     */
    public void process(APDU apdu) throws ISOException, APDUException {
        byte[] buffer = apdu.getBuffer();
        byte ins = buffer[OFFSET_INS];
        short le = -1;

        if (selectingApplet()) {
            // this is for the first message don't take it out
            // DON'T, otherwise you get the failed select error
            return;
        }




        switch (ins) {
            case test.HELLOMSG:
                replyHELLOMSG(apdu);
                break;
            case test.GETMASTER:
                receiveMaster(apdu);
                break;
            default:
                ISOException.throwIt((short) 137);
        }//end switch
    }//end process


    public static void replyHELLOMSG(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();

        ECPublicKey publicKey = (ECPublicKey) keypair.getPublic();
        short lenW = publicKey.getW(buffer, ISO7816.OFFSET_CDATA);


        byte[] responseData = {5, 6, 87}; // Your response data

// Set the response data in the APDU buffer
//        Util.arrayCopyNonAtomic(responseData, (short) 0, buffer, ISO7816.OFFSET_CDATA, (short) lenW);

// Send the response APDU to the terminal
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, lenW);

    }//end replyHELLOMSG


    public static void sendPublicKey(APDU apdu) {
        //Make EC Public key
        byte[] buffer = apdu.getBuffer();
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();

        ECPublicKey publicKey = (ECPublicKey) keypair.getPublic();
        short lenW = publicKey.getW(buffer, ISO7816.OFFSET_CDATA);


        // Send the response APDU to the terminal
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, lenW);

    }//end sendPublicKey

    public static void receiveMaster(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
            // Make EC public key instance with 192 bit public key
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        ECPublicKey publicKey = (ECPublicKey) keypair.getPublic();

        // Initialize the public key with the data in buffer
//            publicKey.setFieldFP(prime, (short)0, (short)prime.length);
//            publicKey.setA(a, (short)0, (short)a.length);
//            publicKey.setB(b, (short)0, (short)b.length);
//            publicKey.setG(g, (short)0, (short)g.length);
//            publicKey.setR(r, (short)0, (short)r.length);
//            publicKey.setK(k);
        publicKey.setW(buffer, ISO7816.OFFSET_CDATA, (short) 49);

        // Create a new buffer for outgoing data
        short lenW = publicKey.getW(buffer, ISO7816.OFFSET_CDATA);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, lenW);

        //now trying verification
    }
}//end applet