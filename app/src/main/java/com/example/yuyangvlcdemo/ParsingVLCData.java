package com.example.yuyangvlcdemo;

public class ParsingVLCData {

    public static char getFrameID(byte[] src) {
        return (char)src[0];
    }

    public static int getCO2Info(byte[] src) {
        int s1 = 0 & 0xFF;
        int s2 = 0 & 0xFF;
        int s3 = src[4] & 0xFF;
        int s4 = src[5] & 0xFF;
        int dec = ((s1 << 24) + (s2 << 16) + (s3 << 8) + (s4 << 0));
        return dec;
    }

    public static double getTempInfo(byte[] src) {
        double h = src[4] & 0xFF;
        double l = src[5] & 0xFF;
        double carry = 1.0;
        while(l/carry > 1) carry*=10.0;
        return h + (l/carry);
    }

    public static double getHumiInfo(byte[] src) {
        double h = src[6] & 0xFF;
        double l = src[7] & 0xFF;
        double carry = 1.0;
        while(l/carry > 1) carry*=10.0;
        return h + (l/carry);
    }
}
