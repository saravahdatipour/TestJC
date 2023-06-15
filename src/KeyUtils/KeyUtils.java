package KeyUtils;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.*;

public class KeyUtils {

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    public static void main(String[] args) {
        try {
            KeyPair keyPair = generateKeyPair();
            saveKeyPairToFile(keyPair, "private.key", "public.key");

//            // Now let's read them back
//            ECPrivateKey privateKey = loadPrivateKeyFromFile("private.key");
//            ECPublicKey publicKey = loadPublicKeyFromFile("public.key");

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

    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator generator = null;
        try {
            generator = KeyPairGenerator.getInstance("EC", "BC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            generator.initialize(ecSpec, new SecureRandom());
        } catch (NoSuchProviderException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
        return generator != null ? generator.generateKeyPair() : null;
    }


    public static ECPrivateKey loadPrivateKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC", "BC");
        return (ECPrivateKey) kf.generatePrivate(spec);
    }

    public static ECPublicKey loadPublicKeyFromFile(String fileName) throws Exception {
        byte[] keyBytes = Files.readAllBytes(Paths.get(fileName));
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("EC", "BC");
        return (ECPublicKey) kf.generatePublic(spec);
    }
}
