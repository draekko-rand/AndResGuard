package com.tencent.mm.util;


import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.util.Objects;


/**
 * @author ysbing
 */
public class Md5Util {

    public static String getMD5Str(String str) {
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(str.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return "";
        }
        return bytesToHexString(digest.digest());
    }

    public static String getMD5Str(File file) {
        if (!file.isFile()) {
            return "";
        }
        MessageDigest digest;
        try {
            digest = MessageDigest.getInstance("MD5");
            digest.update(readFileToByteArray(file));
        } catch (Exception e) {
            return "";
        }
        return bytesToHexString(digest.digest());
    }

    public static String bytesToHexString(byte[] src) {
        if (src.length <= 0) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder(src.length);
        for (byte b : src) {
            int v = b & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }

    public static byte[] readFileToByteArray(final File file) throws IOException {
        Objects.requireNonNull(file, "file");
        return Files.readAllBytes(file.toPath());
    }
}