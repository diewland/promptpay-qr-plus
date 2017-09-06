package com.diewland.android.qr_pp_plus;

// https://www.blognone.com/node/95133
// http://introcs.cs.princeton.edu/java/61data/CRC16CCITT.java

public class CRC16 {

    public static String checksum(String input){
        int crc = 0xFFFF;          // initial value
        int polynomial = 0x1021;   // 0001 0000 0010 0001  (0, 5, 12)
        byte[] bytes = input.getBytes();
        for (byte b : bytes) {
            for (int i = 0; i < 8; i++) {
                boolean bit = ((b   >> (7-i) & 1) == 1);
                boolean c15 = ((crc >> 15    & 1) == 1);
                crc <<= 1;
                if (c15 ^ bit) crc ^= polynomial;
            }
        }
        crc &= 0xffff;
        return String.format("%04x", crc).toUpperCase();
    }

}
