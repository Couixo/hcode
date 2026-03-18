package org.somebody.hcode

import java.nio.charset.StandardCharsets
import java.security.MessageDigest

class ChallengeValidator {
    companion object {
        init {
            System.loadLibrary("p4bu")
        }

        private external fun jni_getSalt(): String

        // 添加 requestCode 参数
        fun generateChallenge(requestCode: String): String {
            val salt = jni_getSalt()
            // 使用传入的 requestCode
            return encrypt(salt.trim() + requestCode)
        }

        fun encrypt(toEncrypt: String): String {
            val md5Hash = md5(toEncrypt)
            val replaced = replaceChars(md5Hash)
            return replaced.substring(0, 6)
        }

        private fun md5(input: String): String {
            return try {
                val md = MessageDigest.getInstance("MD5")
                val digest = md.digest(input.toByteArray(StandardCharsets.UTF_8))
                val sb = StringBuilder()
                for (b in digest) {
                    sb.append(String.format("%02x", b))
                }
                sb.toString()
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }

        private fun replaceChars(input: String): String {
            return input.replace('a', '1')
                .replace('b', '2')
                .replace('c', '3')
                .replace('d', '4')
                .replace('e', '5')
                .replace('f', '6')
        }
    }
}
