package test;

import javacard.framework.*;
import javacard.security.*;

/**
 * @noinspection ClassNamePrefixedWithPackageName, ImplicitCallToSuper, MethodOverridesStaticMethodOfSuperclass, ResultOfObjectAllocationIgnored
 */
public class test extends Applet implements ISO7816 {

    private static ECPublicKey publicKey;
    private static ECPublicKey MasterpublicKey;

    private static ECPrivateKey privateKey;

    private static short cardid;
    private static byte[] cardidbytes = new byte[2];
    private static byte STATE;


    private final static byte HELLOMSG = (byte) 0; // TEST MESSAGE 1

    private final static byte PersonalizationTerminal_MSG1 = (byte) 1; // tell card to generate and send public key
    private final static byte PersonalizationTerminal_MSG2 = (byte) 2; // send signed public key (cert), Master public key, ID of user


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
            case test.PersonalizationTerminal_MSG1:
                generateKeypair();
                sendPublicKey(apdu);
                break;
            case test.PersonalizationTerminal_MSG2:
//                loadKeysAndVerify(apdu);
                receiveMaster(apdu);
                break;
            default:
                ISOException.throwIt((short) 137);
        }
    }


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


    public static void generateKeypair() {
        //Make EC Public key
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();
        privateKey = (ECPrivateKey) keypair.getPrivate();
        publicKey = (ECPublicKey) keypair.getPublic();
    }

    public static void sendPublicKey(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short lenW = publicKey.getW(buffer, ISO7816.OFFSET_CDATA);
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, lenW);
    }//end sendPublicKeySS



    public static void receiveMaster(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short dataLength = apdu.setIncomingAndReceive();
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();
        MasterpublicKey = (ECPublicKey) keypair.getPublic();
        MasterpublicKey.setW(buffer, ISO7816.OFFSET_CDATA, (short) 49);

        // set length and offset of the signature
        short sigOffset = (short)(ISO7816.OFFSET_CDATA + 49);
        short sigLength = (short)(dataLength - 49);

        //set the card id
        short cardidOffset = (short) (ISO7816.OFFSET_CDATA + dataLength - 2);
        Util.arrayCopy(buffer, cardidOffset, cardidbytes, (short) 0, (short) 2);

        Signature m_verify = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
        m_verify.init(MasterpublicKey, Signature.MODE_VERIFY);
        // Predefined message to be the public key on the card + card id
        byte[] message = new byte[51];
        publicKey.getW(message,(short) 0);

        // Add the card id to the message
        Util.arrayCopy(cardidbytes, (short) 0, message, (short) 49, (short) 2);

        // VERIFY SIGNATURE
        boolean IsVerified = m_verify.verify(message, (short)0, (short)message.length, buffer, sigOffset, sigLength);
        if (!IsVerified) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //set state

        STATE = (byte) 0x01;



        buffer[ISO7816.OFFSET_CDATA] = (byte) 0x4F;
        buffer[ISO7816.OFFSET_CDATA+1] = (byte) 0x4B; //ASCII for OK

        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short)2);

    }

    public static void verifyTerminal(APDU apdu) {
        byte[] buffer = apdu.getBuffer();
        short dataLength = apdu.setIncomingAndReceive();
        KeyPair keypair = new KeyPair(KeyPair.ALG_EC_FP, KeyBuilder.LENGTH_EC_FP_192);
        keypair.genKeyPair();
        MasterpublicKey = (ECPublicKey) keypair.getPublic();
        MasterpublicKey.setW(buffer, ISO7816.OFFSET_CDATA, (short) 49);

        // set length and offset of the signature
        short sigOffset = (short)(ISO7816.OFFSET_CDATA + 49);
        short sigLength = (short)(dataLength - 49);

        //set the card id
        short cardidOffset = (short) (ISO7816.OFFSET_CDATA + dataLength - 2);
        Util.arrayCopy(buffer, cardidOffset, cardidbytes, (short) 0, (short) 2);

        Signature m_verify = Signature.getInstance(Signature.ALG_ECDSA_SHA, false);
        m_verify.init(MasterpublicKey, Signature.MODE_VERIFY);
        // Predefined message to be the public key on the card + card id
        byte[] message = new byte[51];
        publicKey.getW(message,(short) 0);

        // Add the card id to the message
        Util.arrayCopy(cardidbytes, (short) 0, message, (short) 49, (short) 2);

        // VERIFY SIGNATURE
        boolean IsVerified = m_verify.verify(message, (short)0, (short)message.length, buffer, sigOffset, sigLength);
        if (!IsVerified) {
            ISOException.throwIt(ISO7816.SW_SECURITY_STATUS_NOT_SATISFIED);
        }

        //set state

        STATE = (byte) 0x01;



        buffer[ISO7816.OFFSET_CDATA] = (byte) 0x4F;
        buffer[ISO7816.OFFSET_CDATA+1] = (byte) 0x4B; //ASCII for OK

        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short)2);

    }








}




//end applet