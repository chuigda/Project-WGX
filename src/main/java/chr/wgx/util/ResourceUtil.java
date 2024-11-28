package chr.wgx.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class ResourceUtil {
    public static String readTextFile(String path) throws IOException {
        try (InputStream inputStream = ResourceUtil.class.getResourceAsStream(path)) {
            if (inputStream == null) {
                throw new IOException("找不到资源: " + path);
            }

            byte[] bytes = inputStream.readAllBytes();
            return new String(bytes, StandardCharsets.UTF_8);
        }
    }
}
