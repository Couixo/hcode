package org.somebody.hcode;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

/**
 * 独立的挑战码验证器
 * 从AdminActivity中提取的验证算法，保持100%兼容性
 * 需要链接p4bu.so库文件以使用jni_getSalt()函数
 */
public class ChallengeValidator {
    
    // 加载本地库
    static {
        try {
            System.loadLibrary("p4bu");
        } catch (UnsatisfiedLinkError e) {
            throw new RuntimeException("无法加载p4bu.so库文件: " + e.getMessage(), e);
        }
    }
    
    // 声明native方法
    private static native String jni_getSalt();
    
    /**
     * 生成挑战码（使用JNI获取盐值，保持100%兼容性）
     * @return 6位挑战码
     * @throws RuntimeException 如果无法获取盐值
     */
    public static String generateChallenge() {
        String salt = jni_getSalt();
        if (salt == null || salt.trim().isEmpty()) {
            throw new RuntimeException("无法获取有效的盐值");
        }
        return encrypt(salt.trim() + System.currentTimeMillis());
    }
    
    /**
     * 验证响应码是否正确（使用JNI获取盐值，保持100%兼容性）
     * @param challenge 挑战码
     * @param response 响应码
     * @return 验证结果
     * @throws IllegalArgumentException 如果challenge或response为null或空字符串
     */
    public static boolean validateResponse(String challenge, String response) {
        if (challenge == null || challenge.trim().isEmpty()) {
            throw new IllegalArgumentException("Challenge cannot be null or empty");
        }
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Response cannot be null or empty");
        }
        
        String expectedResponse = encrypt(challenge.trim() + jni_getSalt());
        return expectedResponse.equals(response.trim());
    }
    
    /**
     * 生成挑战码（兼容版本，允许传入盐值）
     * @param salt 盐值
     * @return 6位挑战码
     * @throws IllegalArgumentException 如果salt为null或空字符串
     */
    public static String generateChallenge(String salt) {
        if (salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        return encrypt(salt.trim() + System.currentTimeMillis());
    }
    
    /**
     * 验证响应码是否正确（兼容版本，允许传入盐值）
     * @param challenge 挑战码
     * @param response 响应码
     * @param salt 盐值
     * @return 验证结果
     * @throws IllegalArgumentException 如果challenge、response或salt为null或空字符串
     */
    public static boolean validateResponse(String challenge, String response, String salt) {
        if (challenge == null || challenge.trim().isEmpty()) {
            throw new IllegalArgumentException("Challenge cannot be null or empty");
        }
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Response cannot be null or empty");
        }
        if (salt == null || salt.trim().isEmpty()) {
            throw new IllegalArgumentException("Salt cannot be null or empty");
        }
        
        String expectedResponse = encrypt(challenge.trim() + salt.trim());
        return expectedResponse.equals(response.trim());
    }
    
    /**
     * 加密函数 - 将挑战码转换为响应码
     * @param toEncrypt 待加密的字符串
     * @return 加密后的6位字符串
     */
    public static String encrypt(String toEncrypt) {
        String md5Hash = md5(toEncrypt);
        String replaced = replaceChars(md5Hash);
        return replaced.substring(0, 6);
    }
    
    /**
     * MD5哈希函数
     * @param input 输入字符串
     * @return MD5哈希值
     */
    private static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(Integer.toHexString((b & 0xFF) | 0x100).substring(1, 3));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }
    
    /**
     * 字符替换函数
     * 将a-f替换为1-6
     * @param input 输入字符串
     * @return 替换后的字符串
     */
    private static String replaceChars(String input) {
        return input.replace('a', '1')
                   .replace('b', '2')
                   .replace('c', '3')
                   .replace('d', '4')
                   .replace('e', '5')
                   .replace('f', '6');
    }
}