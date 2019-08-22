package ru.vasiliev.sandbox.sovestoauth.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAHash {

    public static byte[] getHashBytes(byte[] input) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
        digest.update(input);
        return digest.digest();
    }


    public static byte[] getHashBytes(String input) {
        return getHashBytes(input.getBytes());
    }


    public static String getHash(String input) {
        if (input == null) {
            return null;
        }
        byte[] messageDigest = getHashBytes(input);
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < messageDigest.length; i++) {
            String h = Integer.toHexString(0xFF & messageDigest[i]);
            while (h.length() < 2) {
                h = "0" + h;
            }
            hexString.append(h);
        }
        return hexString.toString();
    }
}