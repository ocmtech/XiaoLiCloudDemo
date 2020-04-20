package com.ocm.xiaoli_cloud_demo.utils;

/**
 * Created by ocm on 2017-06-20.
 */

public class StringHelper {
    //字节数组转字符串
    public static String bytesToHex(byte[] bytes) {
        StringBuilder buf = new StringBuilder(bytes.length * 2);
        for(byte b : bytes) { // 使用String的format方法进行转换
            buf.append(String.format("%02x", new Integer(b & 0xff)));
        }
        return buf.toString();
    }

    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
//        hexString = hexString.toUpperCase();
//        int length = hexString.length() / 2;
        int length = hexString.length() >> 1;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
//            int pos = i * 2;
            int pos = i << 1;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
    private static byte charToByte(char c) {
        int dex =  "0123456789ABCDEF".indexOf(c);
        if(dex<0)dex ="0123456789abcdef".indexOf(c);
        return (byte) dex;
    }
}
