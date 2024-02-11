package org.nature.common.util;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * md5工具类
 * @author Nature
 * @version 1.0.0
 * @since 2024/1/15
 */
public class Md5Util {

    /**
     * 生成MD5字符串
     * @param input 输入
     * @return String
     */
    public static String md5(String... input) {
        return md5(String.join(":", input));
    }

    /**
     * 生成MD5字符串
     * @param input 输入
     * @return String
     */
    public static String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] messageDigest = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : messageDigest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
