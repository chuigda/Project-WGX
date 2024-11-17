package chr.wgx.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public final class SharedObjectLoader {
    public static void loadFromResources(String resourcePath, String librarySuffix) {
        try (InputStream in = SharedObjectLoader.class.getResourceAsStream(resourcePath)) {
            if (in == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            File tempFile = File.createTempFile("temp_lib_", librarySuffix);
            tempFile.deleteOnExit();
            Files.copy(in, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            System.load(tempFile.getAbsolutePath());
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
