package org.somebody.hcode;

import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;

 public class ChallengeValidator {
    static {
        System.loadLibrary("p4bu");
    }
    
    private static native String jni_getSalt();
    
    // 添加 requestCode 参数
    public static String generateChallenge(String requestCode) {
        String salt = jni_getSalt();
        // 使用传入的 requestCode
        return encrypt(salt.trim() + requestCode);
    }
    
    
    public static String encrypt(String toEncrypt) {
        String md5Hash = md5(toEncrypt);
        String replaced = replaceChars(md5Hash);
        return replaced.substring(0, 6);
    }
    
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private static String replaceChars(String input) {
        return input.replace('a', '1')
                   .replace('b', '2')
                   .replace('c', '3')
                   .replace('d', '4')
                   .replace('e', '5')
                   .replace('f', '6');
    }
}
