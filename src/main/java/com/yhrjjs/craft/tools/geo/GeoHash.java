package com.yhrjjs.craft.tools.geo;

import java.util.BitSet;
import java.util.HashMap;

/**
 * GeoHash编码工具
 * (依何软件 From 2022)
 *
 * @author <a href="huangqi@yhrjjs.com">黄奇</a>
 * <pre>
 *   2022-05-18 * 黄奇创建
 * </pre>
 */
public class GeoHash {
    private static final int NUM_BITS = 6 * 5;
    final static char[] digits = {'0',
            '1',
            '2',
            '3',
            '4',
            '5',
            '6',
            '7',
            '8',
            '9',
            'b',
            'c',
            'd',
            'e',
            'f',
            'g',
            'h',
            'j',
            'k',
            'm',
            'n',
            'p',
            'q',
            'r',
            's',
            't',
            'u',
            'v',
            'w',
            'x',
            'y',
            'z'};

    final static HashMap<Character, Integer> lookup = new HashMap<Character, Integer>();

    static {
        int i = 0;
        for (char c : digits) {
            lookup.put(c, i++);
        }
    }

    /**
     * GeoHash解密
     *
     * @param geoHash GeoHash串
     * @return 经纬度信息
     */
    public double[] decode(String geoHash) {
        StringBuilder buffer = new StringBuilder();
        for (char c : geoHash.toCharArray()) {

            int i = lookup.get(c) + 32;
            buffer.append(Integer.toString(i, 2).substring(1));
        }

        BitSet lonSet = new BitSet();
        BitSet latSet = new BitSet();

        // even bits
        int j = 0;
        for (int i = 0; i < NUM_BITS * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            lonSet.set(j++, isSet);
        }

        // odd bits
        j = 0;
        for (int i = 1; i < NUM_BITS * 2; i += 2) {
            boolean isSet = false;
            if (i < buffer.length()) {
                isSet = buffer.charAt(i) == '1';
            }
            latSet.set(j++, isSet);
        }

        // double lon = decode(lonset, -180, 180);
        // double lat = decode(latset, -90, 90);
        // 中国地理坐标：东经73°至东经135°，北纬4°至北纬53°
        double lon = decode(lonSet, 70, 140);
        double lat = decode(latSet, 0, 60);
        return new double[]{lat, lon};
    }

    private double decode(BitSet bs, double floor, double ceiling) {
        double mid = 0;
        for (int i = 0; i < bs.length(); i++) {
            mid = (floor + ceiling) / 2;
            if (bs.get(i)) {
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return mid;
    }

    /**
     * GeoHash编码
     *
     * @param lat 纬度
     * @param lon 经度
     * @return 编码字符串
     */
    public String encode(double lat, double lon) {
        BitSet latBits = getBits(lat, -90, 90);
        BitSet lonBits = getBits(lon, -180, 180);
        StringBuilder buffer = new StringBuilder();
        for (int i = 0; i < NUM_BITS; i++) {
            buffer.append((lonBits.get(i)) ? '1' : '0');
            buffer.append((latBits.get(i)) ? '1' : '0');
        }
        return base32(Long.parseLong(buffer.toString(), 2));
    }

    private BitSet getBits(double lat, double floor, double ceiling) {
        BitSet buffer = new BitSet(NUM_BITS);
        for (int i = 0; i < NUM_BITS; i++) {
            double mid = (floor + ceiling) / 2;
            if (lat >= mid) {
                buffer.set(i);
                floor = mid;
            } else {
                ceiling = mid;
            }
        }
        return buffer;
    }

    public static String base32(long i) {
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);
        if (!negative) {
            i = -i;
        }
        while (i <= -32) {
            buf[charPos--] = digits[(int) (-(i % 32))];
            i /= 32;
        }
        buf[charPos] = digits[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }
        return new String(buf, charPos, (65 - charPos));
    }

    public static void main(String[] args) {
        double lon1 = 109.0145193757;
        double lat1 = 34.236080797698;

        System.out.println(new GeoHash().encode(lat1, lon1));
    }
}