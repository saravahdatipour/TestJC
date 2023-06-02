package test;

import javacard.framework.*;

/**
 * @noinspection ClassNamePrefixedWithPackageName, ImplicitCallToSuper, MethodOverridesStaticMethodOfSuperclass, ResultOfObjectAllocationIgnored
 */
public class test extends Applet implements ISO7816 {

    private final static byte HELLOMSG = (byte) 1; // TEST MESSAGE 1

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

            default:
                ISOException.throwIt((short) 137);
        }//end switch
    }//end process


    public static void replyHELLOMSG(APDU apdu) {
        byte[] buffer = apdu.getBuffer();

        byte[] responseData = {5, 6, 87}; // Your response data

// Set the response data in the APDU buffer
        Util.arrayCopyNonAtomic(responseData, (short) 0, buffer, ISO7816.OFFSET_CDATA, (short) responseData.length);

// Send the response APDU to the terminal
        apdu.setOutgoingAndSend(ISO7816.OFFSET_CDATA, (short) responseData.length);



//        byte ins = buffer[OFFSET_INS];
//        short le = -1;
//        le =apdu.setOutgoing();
//        byte bytetosend = 2;
//        apdu.setOutgoingLength((short) 5);
//        apdu.sendBytes((short) 2, (short)5);

    }//end replyHELLOMSG
}//end applet