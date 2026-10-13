package com.techforge.util;

/**
 * 按文件头（魔数）识别图片真实类型
 */
public final class ImageTypeDetector {

    private ImageTypeDetector() {
    }

    /**
     * 返回规范扩展名 jpg/png/webp；非这三种返回 null
     */
    public static String detect(byte[] c) {
        if (c == null) return null;
        if (c.length >= 3
                && (c[0] & 0xFF) == 0xFF && (c[1] & 0xFF) == 0xD8 && (c[2] & 0xFF) == 0xFF) {
            return "jpg";
        }
        if (c.length >= 8
                && (c[0] & 0xFF) == 0x89 && c[1] == 'P' && c[2] == 'N' && c[3] == 'G'
                && (c[4] & 0xFF) == 0x0D && (c[5] & 0xFF) == 0x0A
                && (c[6] & 0xFF) == 0x1A && (c[7] & 0xFF) == 0x0A) {
            return "png";
        }
        if (c.length >= 12
                && c[0] == 'R' && c[1] == 'I' && c[2] == 'F' && c[3] == 'F'
                && c[8] == 'W' && c[9] == 'E' && c[10] == 'B' && c[11] == 'P') {
            return "webp";
        }
        return null;
    }
}
