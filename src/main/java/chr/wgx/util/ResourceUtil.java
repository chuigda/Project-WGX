package chr.wgx.util;

import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceUtil {
    public static String readTextFile(String path) throws IOException {
        return new String(readBinaryFile(path), StandardCharsets.UTF_8);
    }

    public static byte[] readBinaryFile(String path) throws IOException {
        try (@Nullable InputStream inputStream = ResourceUtil.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("找不到资源: " + path);
            }

            return inputStream.readAllBytes();
        }
    }
}
