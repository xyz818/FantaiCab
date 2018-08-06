package org.net.fantai.model;

/**
 * Created by fantai-xyz on 2017/9/12.
 */
public class DataFormat {
    /**
     * author fantai-xyz
     * brief  十六进制数组转十六进制字符串
     * params
     * time 2017/9/11 9:17
     * return
     */
    public static String bytes2HexString(byte[] src) {
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v);
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString().toUpperCase();


    }

    /**
     * author fantai-xyz
     * brief  获取电压值
     * params
     * time 2017/10/16 19:21
     * return
     */
    public static double getV(byte v) {
        double elect = 0.0;
        switch (v) {
            case (byte) 0x61:
                elect = 3.5;
                break;
            case (byte) 0x62:
                elect = 3.85;
                break;
            case (byte) 0x63:
                elect = 4.10;
                break;
            case (byte) 0x64:
                elect = 4.20;
                break;
        }
        return elect;


    }

    public static String getVRange(byte v)
    {
        String electValue = "0.0";
        switch (v) {
            case 0x30:
                electValue = "<3.5V";
                break;
            case 0x31:
                electValue = "3.5V~3.75V";
                break;
            case 0x32:
                electValue = "3.75V~3.8V";
                break;
            case 0x33:
                electValue = "3.8V~3.9V";
                break;
            case 0x34:
                electValue = "3.9V~4.0V";
                break;
            case 0x35:
                electValue = "4.0V~4.1V";
                break;
            case 0x36:
                electValue = "4.1V~4.2V";
                break;
            case 0x37:
                electValue = ">4.2V";
                break;
        }
        return electValue;

    }



    /**
     * author fantai-xyz
     * brief 十六进制字符串转十六进制数组
     * params
     * time 2017/9/11 11:29
     * return
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }

    /**
     * Convert char to byte
     *
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }


}
