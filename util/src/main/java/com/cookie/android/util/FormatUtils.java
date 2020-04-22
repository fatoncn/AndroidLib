package com.cookie.android.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;

/**
 * FormatUtils
 * Author: ZhangLingfei
 * Date : 2018/12/15 0015
 */

public class FormatUtils {
    /**
     * 一亿
     */
    private static final int YI_YI = 100000000;
    /**
     * 一万
     */
    private static final int YI_WAN = 10000;

    public static String formatBig(int number) {
        return formatBig((long) number);
    }

    public static String formatBig(long number) {
        DecimalFormat format;
        String result;
        if (number >= YI_YI) {
            format = new DecimalFormat("0.0");
            float temp = number / (float) YI_YI;
            result = format.format(temp);
            return result + "亿";
        } else if (number >= YI_WAN) {
            format = new DecimalFormat("0");
            return format.format(number / YI_WAN) + "万";
        }
        return String.valueOf(number);
    }

    public static int limit(int min, int max, int value) {
        if (value < min) {
            value = min;
        } else if (value > max) {
            value = max;
        }
        return value;
    }

    public static String formatSmall(int number) {
        if (number > 99) {
            return "99+";
        }
        return String.valueOf(number);
    }

    public static int toInt(String s) {
        return toInt(s, 0);
    }

    public static float toFloat(String s) {
        try {
            return Float.parseFloat(s);
        } catch (NumberFormatException e) {
            Logger.printException(e);
            return 0;
        }
    }

    public static int toInt(String s, int defValue) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException e) {
            Logger.printException(e);
            return defValue;
        }
    }

    public static long toLong(String s) {
        return toLong(s, 0);
    }

    public static short[] bytesToShort(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        short[] shorts = new short[bytes.length / 2];
        ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shorts);
        return shorts;
    }

    public static long toLong(String value, long defValue) {
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            Logger.printException(e);
            return defValue;
        }
    }
}
