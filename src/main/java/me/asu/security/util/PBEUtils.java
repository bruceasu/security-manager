package me.asu.security.util;


import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.Key;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Base64.Encoder;
import java.util.Objects;
import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

/*
PBEWITHHMACSHA1ANDAES_128
PBEWITHHMACSHA1ANDAES_256
PBEWITHHMACSHA224ANDAES_128
PBEWITHHMACSHA224ANDAES_256
PBEWITHHMACSHA256ANDAES_128
PBEWITHHMACSHA256ANDAES_256
PBEWITHHMACSHA384ANDAES_128
PBEWITHHMACSHA384ANDAES_256
PBEWITHHMACSHA512ANDAES_128
PBEWITHHMACSHA512ANDAES_256
PBEWITHMD5ANDDES
PBEWITHMD5ANDTRIPLEDES
PBEWITHSHA1ANDDESEDE
PBEWITHSHA1ANDRC2_128
PBEWITHSHA1ANDRC2_40
PBEWITHSHA1ANDRC4_128
PBEWITHSHA1ANDRC4_40
*/
public class PBEUtils {

    public static final String ALGORITHM = "PBEWITHHMACSHA512ANDAES_256";

    //Security.addProvider(new com.sun.crypto.provider.SunJCE());

    public static final int ITERATION_COUNT = 8;


    public static byte[] initSalt() throws Exception {
        SecureRandom random = new SecureRandom();
        return random.generateSeed(8);
    }

    // 16 bytes, 128bits
    public static byte[] IV   = "1234567890ABCDEF".getBytes();
    // 32 bytes, 256bits
    public static byte[] SALT = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890"
            .getBytes();


    private static Key toKey(String password) throws Exception {
        PBEKeySpec       keySpec    = new PBEKeySpec(password.toCharArray());
        SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
        SecretKey        secretKey  = keyFactory.generateSecret(keySpec);

        return secretKey;
    }

    public static void encryptFile(File fileIn, File fileOut, String password)
    throws Exception {
        encryptFile(fileIn, fileOut, password, SALT);
    }

    public static void encryptFile(File fileIn,
                                   File fileOut,
                                   String password,
                                   byte[] salt) throws Exception {
        Objects.requireNonNull(fileIn);
        Objects.requireNonNull(fileOut);
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        InputStream  in = Files.newInputStream(fileIn.toPath());
        OutputStream os = Files.newOutputStream(fileOut.toPath());
        copy(in, os, cipher);
        System.out.println("完成加密 " + fileIn + " => " + fileOut);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
    }

    public static void decryptFile(File fileIn, File fileOut, String password)
    throws Exception {
        decryptFile(fileIn, fileOut, password, SALT);
    }

    public static void decryptFile(File fileIn,
                                   File fileOut,
                                   String password,
                                   byte[] salt) throws Exception {
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        InputStream  in = Files.newInputStream(fileIn.toPath());
        OutputStream os = Files.newOutputStream(fileOut.toPath());
        copy(in, os, cipher);

        System.out.println("完成解密 " + fileIn + " => " + fileOut);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
    }

    public static void encryptToFile(String input,
                                     File fileOut,
                                     String password) throws Exception {
        encryptToFile(input, fileOut, password, SALT);
    }

    public static void encryptToFile(String input,
                                     File fileOut,
                                     String password,
                                     byte[] salt) throws Exception {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        encryptToFile(bytes, fileOut, password, salt);
    }

    public static void encryptToFile(byte[] input,
                                     File fileOut,
                                     String password,
                                     byte[] salt) throws Exception {
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        InputStream  in = new ByteArrayInputStream(input);
        OutputStream os = Files.newOutputStream(fileOut.toPath());
        copy(in, os, cipher);

        System.out.println("完成加密, 寫入 " + fileOut);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
    }

    public static void decryptToFile(byte[] input,
                                     File fileOut,
                                     String password) throws Exception {
        decryptToFile(input, fileOut, password, SALT);
    }

    public static void decryptToFile(byte[] input,
                                     File fileOut,
                                     String password,
                                     byte[] salt) throws Exception {
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        InputStream  in = new ByteArrayInputStream(input);
        OutputStream os = Files.newOutputStream(fileOut.toPath());
        copy(in, os, cipher);

        System.out.println("完成解密, 寫入 " + fileOut);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
    }

    public static byte[] encryptFromFile(File fileIn, String password)
    throws Exception {
        return encryptFromFile(fileIn, password, SALT);
    }

    public static byte[] encryptFromFile(File fileIn,
                                         String password,
                                         byte[] salt) throws Exception {
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        InputStream           in   = Files.newInputStream(fileIn.toPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        copy(in, baos, cipher);

        System.out.println("完成加密 " + fileIn);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
        return baos.toByteArray();
    }

    public static byte[] decryptFromFile(File fileIn, String password)
    throws Exception {
        return decryptFromFile(fileIn, password, SALT);
    }

    public static byte[] decryptFromFile(File fileIn,
                                         String password,
                                         byte[] salt) throws Exception {
        long            start  = System.currentTimeMillis();
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        InputStream           in   = Files.newInputStream(fileIn.toPath());
        ByteArrayOutputStream baos = new ByteArrayOutputStream(4096);
        copy(in, baos, cipher);

        System.out.println("完成解密 " + fileIn);
        long end = System.currentTimeMillis();
        System.out.println("花費:  " + (end - start) + " 毫秒。");
        return baos.toByteArray();
    }

    public static PipedInputStream encryptToStream(final InputStream in,
                                                   final String password)
    throws Exception {
        return encryptToStream(in, password, SALT);
    }

    public static PipedInputStream encryptToStream(final InputStream in,
                                                   final String password,
                                                   final byte[] salt)
    throws Exception {
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);

        final PipedInputStream  pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        new Thread() {
            {
                setDaemon(true);
                start();
            }

            @Override
            public void run() {
                copy(in, pos, cipher);
            }
        };

        return pis;
    }

    public static PipedInputStream decryptToStream(final InputStream in,
                                                   final String password)
    throws Exception {
        return decryptToStream(in, password, SALT);
    }

    public static PipedInputStream decryptToStream(final InputStream in,
                                                   final String password,
                                                   final byte[] salt)
    throws Exception {
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec = new PBEParameterSpec(salt,
                                                          ITERATION_COUNT,
                                                          ivSpec);
        final Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);

        final PipedInputStream  pis = new PipedInputStream();
        final PipedOutputStream pos = new PipedOutputStream(pis);
        new Thread() {
            {
                setDaemon(true);
                start();
            }

            @Override
            public void run() {
                copy(in, pos, cipher);
            }
        };

        return pis;
    }

    private static void copy(InputStream in, OutputStream pos, Cipher cipher) {
        byte buffer[] = new byte[4096];
        int  len      = 0;
        while (true) {
            try {
                len = in.read(buffer);
                if (len == -1) {
                    break;
                } else if (len == buffer.length) {
                    byte[] bytes = cipher.update(buffer, 0, len);
                    pos.write(bytes);
                } else {
                    byte[] bytes = cipher.doFinal(buffer, 0, len);
                    pos.write(bytes);
                }
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (IllegalBlockSizeException e) {
                e.printStackTrace();
                break;
            } catch (BadPaddingException e) {
                e.printStackTrace();
                break;
            }
        }
        safeClose(in);
        safeClose(pos);
    }

    private static void safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ingored.
            }
        }
    }

    public static String encryptString(String data, String password)
    throws Exception {
        return encryptString(data, password, SALT);
    }

    public static String encryptString(String data,
                                       String password,
                                       byte[] salt) throws Exception {
        Encoder encoder = Base64.getEncoder();
        byte[]  bytes   = data.getBytes(StandardCharsets.UTF_8);
        byte[]  encrypt = encrypt(bytes, password, salt);
        return encoder.encodeToString(encrypt);
    }

    public static String decryptString(String data, String password)
    throws Exception {
        return decryptString(data, password, SALT);
    }

    public static String decryptString(String data,
                                       String password,
                                       byte[] salt) throws Exception {
        Decoder decoder = Base64.getDecoder();
        byte[]  decode  = decoder.decode(data);
        byte[]  decrypt = decrypt(decode, password, salt);
        return new String(decrypt, StandardCharsets.UTF_8);
    }

    public static byte[] encrypt(byte[] data, String password)
    throws Exception {
        return encrypt(data, password, SALT);
    }

    public static byte[] encrypt(byte[] data, String password, byte[] salt)
    throws Exception {
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec =
                new PBEParameterSpec(salt, ITERATION_COUNT, ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] data, String password)
    throws Exception {
        return decrypt(data, password, SALT);
    }

    public static byte[] decrypt(byte[] data, String password, byte[] salt)
    throws Exception {
        Key             key    = toKey(password);
        IvParameterSpec ivSpec = new IvParameterSpec(IV);
        PBEParameterSpec paramSpec =
                new PBEParameterSpec(salt, ITERATION_COUNT, ivSpec);
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        return cipher.doFinal(data);
    }

    public static void main(String[] args) throws Exception {
        String data     = "test";
        String password = "abc";
        byte[] salt     = initSalt();
        System.out.println("origin: " + data);
        String d = encryptString(data, password, salt);
        System.out.println("encrypt: " + d);
        String d2 = decryptString(d, password, salt);
        System.out.println("decrypt: " + d2);
    }

}
