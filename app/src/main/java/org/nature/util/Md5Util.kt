package org.nature.util

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException

/**
 * md5工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/15
 */
object Md5Util {

    /**
     * 生成MD5字符串
     * @param input 输入
     * @return String
     */
    @JvmStatic
    fun md5(vararg input: String): String {
        return md5(input.joinToString(":"))
    }

    /**
     * 生成MD5字符串
     * @param input 输入
     * @return String
     */
    @JvmStatic
    fun md5(input: String): String {
        try {
            val md = MessageDigest.getInstance("MD5")
            val messageDigest = md.digest(input.toByteArray())
            val sb = StringBuilder()
            for (b in messageDigest) {
                sb.append(String.format("%02x", b))
            }
            return sb.toString()
        } catch (e: NoSuchAlgorithmException) {
            throw RuntimeException(e)
        }
    }
}
