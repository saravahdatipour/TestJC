package KeyUtils;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

public class KeyUtils {
    public static void main(String[] args) {
        try {
            KeyPair keyPair = generateKeyPair();
            saveKeyPairToFile(keyPair, "private.key", "public.key");

            // Now let's read them back
            ECPrivateKey privateKey = loadPrivateKeyFromFile("private.key");
            ECPublicKey publicKey = loadPublicKeyFromFile("public.key");


//            ECParameterSpec ecParameterSpec = privateKey.getParams();
//            EllipticCurve curve = ecParameterSpec.getCurve();
//            ECFieldFp field = (ECFieldFp) curve.getField();
//            BigInteger p = field.getP();
//
//            // Print out the parameters and public key coordinates
//            System.out.println("Curve: " +
//                    "a=" + curve.getA() +
//                    ", b=" + curve.getB());
//            System.out.println("Field size: " + field.getFieldSize());
//            System.out.println("Prime field: " + p);
//            System.out.println("Generator (G): " +
//                    "x=" + ecParameterSpec.getGenerator().getAffineX() +
//                    ", y=" + ecParameterSpec.getGenerator().getAffineY());
//            System.out.println("Order (n): " + ecParameterSpec.getOrder());
//            System.out.println("Cofactor (h): " + ecParameterSpec.getCofactor());
//            System.out.println("Public Key Coordinates:");
//            System.out.println("public key x coordinate: " + publicKey.getW().getAffineX());
//            System.out.println("public key y coordinate: " + publicKey.getW().getAffineY());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void saveKeyPairToFile(KeyPair keyPair, String privateKeyFileName, String publicKeyFileName) {
        // Save the private key
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        try {
            Files.write(Paths.get(privateKeyFileName), privateKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Save the public key
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        try {
            Files.write(Paths.get(publicKeyFileName), publicKeyBytes);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
        KeyPairGenerator generator = KeyPairGenerator.getInstance("EC");
        ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256r1");
        generator.initialize(ecSpec, new SecureRandom());
        return generator.generateKeyPair();
    }

    public static ECPrivateKey loadPrivateKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPrivateKey) kf.generatePrivate(spec);
    }

    public static ECPublicKey loadPublicKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC");
        return (ECPublicKey) kf.generatePublic(spec);
    }
}
