package me.ego.ezbd.lib.fo;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import lombok.NonNull;

public final class CompressUtil {
    public static byte[] compress(@NonNull String string) {
        try {
            if (string == null) {
                throw new NullPointerException("string is marked non-null but is null");
            } else if (string.length() == 0) {
                return null;
            } else {
                ByteArrayOutputStream obj = new ByteArrayOutputStream();
                GZIPOutputStream gzip = new GZIPOutputStream(obj);
                gzip.write(string.getBytes("UTF-8"));
                gzip.flush();
                gzip.close();
                return obj.toByteArray();
            }
        } catch (Throwable var3) {
            throw var3;
        }
    }

    public static String decompress(@NonNull byte[] compressed) {
        try {
            if (compressed == null) {
                throw new NullPointerException("compressed is marked non-null but is null");
            } else {
                StringBuilder outStr = new StringBuilder();
                if (compressed.length == 0) {
                    return "";
                } else {
                    if (isCompressed(compressed)) {
                        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(compressed));
                        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));

                        String line;
                        while((line = bufferedReader.readLine()) != null) {
                            outStr.append(line);
                        }
                    } else {
                        outStr.append(compressed);
                    }

                    return outStr.toString();
                }
            }
        } catch (Throwable var5) {
            throw var5;
        }
    }

    private static boolean isCompressed(byte[] compressed) {
        return compressed[0] == 31 && compressed[1] == -117;
    }

    private CompressUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}
