package org.somebody.hcode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

public class ChallengeValidator {
    static {
        try {
            System.loadLibrary("p4bu");
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("无法加载p4bu.so库文件", e);
        }
    }

    private static native String jni_getSalt();

    // 核心：合并生成挑战码的重载方法
    public static String generateChallenge(String userInput) {
        return encrypt(getSalt() + checkNonEmpty(userInput));
    }

    public static String generateChallenge() {
        return encrypt(getSalt() + System.currentTimeMillis());
    }

    // 核心：合并验证响应码的重载方法
    public static boolean validateResponse(String challenge, String response) {
        return validateResponse(challenge, response, getSalt());
    }

    public static boolean validateResponse(String challenge, String response, String salt) {
        String expected = encrypt(checkNonEmpty(challenge) + checkNonEmpty(salt));
        return expected.equals(checkNonEmpty(response));
    }

    // 加密逻辑（原封不动，保留核心算法）
    private static String encrypt(String toEncrypt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(toEncrypt.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString().replace('a','1').replace('b','2').replace('c','3')
                   .replace('d','4').replace('e','5').replace('f','6').substring(0,6);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    // 工具方法：统一处理非空校验
    private static String checkNonEmpty(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("输入不能为空");
        }
        return input.trim();
    }

    // 工具方法：统一获取盐值
    private static String getSalt() {
        String salt = jni_getSalt();
        if (salt == null || salt.trim().isEmpty()) {
            throw new RuntimeException("无法获取有效的盐值");
        }
        return salt.trim();
    }
}
