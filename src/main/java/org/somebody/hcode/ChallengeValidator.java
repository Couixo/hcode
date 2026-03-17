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

    // 保留生成挑战码的两个重载方法（兼容原有调用）
    public static String generateChallenge(String userInput) {
        return encrypt(getSalt() + checkNonEmpty(userInput));
    }

    public static String generateChallenge() {
        return encrypt(getSalt() + System.currentTimeMillis());
    }

    // 加密逻辑：合并重复的StringBuilder操作
    private static String encrypt(String toEncrypt) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(toEncrypt.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(32); // 预分配容量，优化性能
            for (byte b : digest) sb.append(String.format("%02x", b));
            return sb.toString()
                   .replace('a','1').replace('b','2').replace('c','3')
                   .replace('d','4').replace('e','5').replace('f','6')
                   .substring(0,6);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    // 工具方法：保留非空校验和盐值获取
    private static String checkNonEmpty(String input) {
        if (input == null || input.isBlank()) { // 使用JDK11+的isBlank()简化判断
            throw new IllegalArgumentException("输入不能为空");
        }
        return input.trim();
    }

    private static String getSalt() {
        String salt = jni_getSalt();
        if (salt == null || salt.isBlank()) {
            throw new RuntimeException("无法获取有效的盐值");
        }
        return salt.trim();
    }
}
