package test;

import javacard.framework.*;
import javacard.security.*;

/**
 * @noinspection ClassNamePrefixedWithPackageName, ImplicitCallToSuper, MethodOverridesStaticMethodOfSuperclass, ResultOfObjectAllocationIgnored
 */
public class test extends Applet implements ISO7816 {



    final static byte[] privatekeybytes = {
            (byte)0xF9, (byte)0x5E ,(byte)0x49, (byte)0x05, (byte)0xA7, (byte)0x22 ,(byte)0xE6 ,(byte)0xBD, (byte)0xD9,
            (byte)0x1C, (byte)0x16 ,(byte)0x46 ,(byte)0x49 ,(byte)0x09, (byte)0xB9, (byte)0xFB ,(byte)0xCA ,(byte)0xED ,(byte)0xB0,
            (byte)0x04 ,(byte)0xD4 ,(byte)0x81 ,(byte)0x9B ,(byte)0x3A //24

    };

    final static byte[] publickeybytes = {
            (byte)0x04 ,(byte)0x89 ,(byte)0xA6 ,(byte)0x14, (byte)0x14, (byte)0x94 ,(byte)0x2A ,(byte)0xB3, (byte)0x52,(byte)0x38 ,(byte)0x91 ,(byte)0x43 ,(byte)0x22 ,(byte)0x28 ,(byte)0x92,
            (byte)0xD8 ,(byte)0xDC ,(byte)0x46 ,(byte)0x16 ,(byte)0xD1 ,(byte)0xD5 ,(byte)0x4E, (byte)0x22 ,(byte)0x59, (byte)0x4D ,(byte)0x20 ,(byte)0xB3 ,(byte)0xE5 ,(byte)0xD1 ,(byte)0xCB,
            (byte)0x95 ,(byte)0x23 ,(byte)0xAE ,(byte)0x48, (byte)0x92 ,(byte)0x26 ,(byte)0x2C ,(byte)0xD7 ,(byte)0xB2,(byte)0x81 ,(byte)0x47 ,(byte)0x8F ,(byte)0xA2 ,(byte)0x4D ,(byte)0x01,
            (byte)0x80, (byte)0x04 ,(byte)0xEB, (byte)0xD6 //49

    };
//changing the first byte from 0x30 to 0x31 to check --> it threw 6F00
    final static byte[] signaturebytes = {
            (byte)0x30 ,(byte)0x35, (byte)0x02, (byte)0x18, (byte)0x5F ,(byte)0x60 ,(byte)0xB1 ,(byte)0x0E ,
            (byte)0x7A, (byte)0x12, (byte)0x1A ,(byte)0x36 ,(byte)0xF9 ,(byte)0xD4, (byte)0xF1 ,(byte)0xA4 ,
            (byte)0x92, (byte)0xB5, (byte)0x41 ,(byte)0x91 ,(byte)0x3E ,(byte)0x45 ,(byte)0x2F ,(byte)0x35
            ,(byte)0x4A ,(byte)0x61, (byte)0x17 ,(byte)0x25, (byte)0x02, (byte)0x19 ,(byte)0x00 ,(byte)0xE7
            ,(byte)0x2A ,(byte)0x79 ,(byte)0x56 ,(byte)0x70, (byte)0x09 ,(byte)0x2A ,(byte)0x89 ,(byte)0x0F
            ,(byte)0x7B ,(byte)0x6A ,(byte)0xF7 ,(byte)0xB2 ,(byte)0x67 ,(byte)0xCD, (byte)0x0E, (byte)0x9A,
            (byte)0x94 ,(byte)0xD7, (byte)0x38, (byte)0x9E ,(byte)0x31 ,(byte)0x63, (byte)0x8E //55
//changing the last byte from 0x8E to 0x8F to check --> it threw 6982

    };


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
                loadKeysAndVerify(apdu);
//                receiveMaster(apdu);
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

//    public static void receiveMaster(APDU apdu) {
//        byte[] buffer = apdu.getBuffer();
//        short dataLength = apdu.setIncomingAndReceive();
//
//        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
//        keypair.genKeyPair();
//        ECPublicKey publicKey = (ECPublicKey) keypair.getPublic();
//
//        // Initialize the public key with the data in buffer
//        publicKey.setW(buffer, (short) 5, (short) 49);
//        short lenW = publicKey.getW(buffer, (short) 5);
//
//        Signature verifier = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
//
//        // move the signature from buffer
//        short sigOffset = (short) (ISO7816.OFFSET_CDATA + 49);
//        short sigLength = (short) (dataLength - (short) 49);  // Signature length for ECDSA could be 56 55 54 bytes
//        try {
//            verifier.init(publicKey, Signature.MODE_VERIFY);
////            boolean verified = verifier.verify(buffer, ISO7816.OFFSET_CDATA, (short) 49, buffer, sigOffset, sigLength);
////            if (!verified) {
////                // The signature was not valid. Take appropriate action here.
////                ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
////            }
//
//            // Since the verification is successful, set outgoing data and send it
//            apdu.setOutgoingAndSend(sigOffset, sigLength);
//        } catch (CryptoException e) {
//            short reason = e.getReason();
//            buffer[0] = (byte) (reason >> 8);
//            buffer[1] = (byte) (reason);
//            apdu.setOutgoingAndSend((short) 0, (short) 2);
//        }
//    }}


    public static void receiveMaster(APDU apdu) { //signing works now. fix initializing the public key!!
        byte[] buffer = apdu.getBuffer();
        short dataLength = apdu.setIncomingAndReceive();

        // Make EC public key instance with 192 bit public key
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();
        ECPublicKey publicKey = (ECPublicKey) keypair.getPublic();
        ECPrivateKey privateKey = (ECPrivateKey) keypair.getPrivate();
        // Initialize the public key with the data in buffer
//        publicKey.setW(buffer, ISO7816.OFFSET_CDATA, (short) 49);
//
//        // Assume anything after the 49 bytes are the signature
//        short sigOffset = (short)(ISO7816.OFFSET_CDATA + 49);
//        short sigLength = (short)(dataLength - 49);

        //now trying verification
        //test
        Signature m_sign = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
        m_sign.init(privateKey, Signature.MODE_SIGN);
        byte[] message = new byte[] {5}; // Message is a single byte value 5
        byte[] signature = new byte[64]; // Assuming maximum signature size is 64 bytes
        m_sign.sign(message, (short)0, (short)1, signature, (short)0);



        Signature m_verify = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
        // INIT WITH PUBLIC KEY
        m_verify.init(publicKey, Signature.MODE_VERIFY);

        // VERIFY SIGNATURE
        boolean verified = m_verify.verify(message, (short)0, (short)1, signature, (short)0, (short) 64);
        if (!verified) {
            // The signature was not valid. Take appropriate action here.
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        // Send success message
        buffer[ISO7816.OFFSET_CDATA] = (byte)0x90; // Success code
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short)1);
    }



    public void loadKeysAndVerify(APDU apdu) {
        // Create uninitialized key objects
        ECPublicKey publicKey = (ECPublicKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PUBLIC, KeyBuilder.LENGTH_EC_FP_192, false);
        ECPrivateKey privateKey = (ECPrivateKey) KeyBuilder.buildKey(KeyBuilder.TYPE_EC_FP_PRIVATE, KeyBuilder.LENGTH_EC_FP_192, false);

        // Initialize the public key
        publicKey.setW(publickeybytes, (short)0, (short)publickeybytes.length);

        // Initialize the private key
        privateKey.setS(privatekeybytes, (short)0, (short)privatekeybytes.length);

        // Create and initialize a Signature object for verifying
        Signature verifier = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
        verifier.init(publicKey, Signature.MODE_VERIFY);

        // Predefined message
        byte[] message = {(byte)'S'};

        // Verify the signature
        boolean isVerified = verifier.verify(message, (short)0, (short)message.length, signaturebytes, (short)0, (short)signaturebytes.length);

        // Get the APDU buffer
        byte[] buffer = apdu.getBuffer();

        // If verification fails, throw an exception
        if (!isVerified) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        // If verification is successful, send a success message
        buffer[0] = (byte)0x20;  // Success code
        buffer[1] = (byte)0x01;  // Success code
        apdu.setOutgoingAndSend((short)0, (short)2);
    }



}




//end applet