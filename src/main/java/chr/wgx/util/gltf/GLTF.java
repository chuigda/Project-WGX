package chr.wgx.util.gltf;

import tech.icey.panama.Loader;
import tech.icey.panama.RawFunctionLoader;
import tech.icey.xjbutil.container.Option;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandle;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;

public final class GLTF {
    GLTF(RawFunctionLoader loader) {
        this.HANDLE$gltf_read = RawFunctionLoader.link(loader.apply("gltf_read"), DESCRIPTOR$gltf_read);
        this.HANDLE$gltf_free = RawFunctionLoader.link(loader.apply("gltf_free"), DESCRIPTOR$gltf_free);
    }

    public Option<GLTFCollection> read(String fileName) {
        try (Arena arena = Arena.ofConfined()) {
            MemorySegment fileNameSegment = arena.allocateFrom(fileName, StandardCharsets.UTF_8);
            MemorySegment collectionSegment = (MemorySegment) HANDLE$gltf_read.invokeExact(fileNameSegment);
            if (collectionSegment.address() == 0) {
                return Option.none();
            } else {
                return Option.some(new GLTFCollection(this, collectionSegment));
            }
        } catch (Throwable e) {
            return Option.none();
        }
    }

    void free(GLTFCollection collection) {
        try {
            HANDLE$gltf_free.invokeExact(collection.segment);
        } catch (Throwable e) {
            // do nothing, theoretically it is impossible to fail here
        }
    }

    public static boolean loadLibrary() {
        Path path = Path.of(System.getProperty("user.dir"));
        if (System.getProperty("os.name").toLowerCase().contains("windows")) {
            path = path.resolve("gltf_reader.dll");
        } else {
            path = path.resolve("libgltf_reader.so");
        }

        try {
            System.load(path.toAbsolutePath().toString());
            return true;
        } catch (Throwable e) {
            return false;
        }
    }

    public static Option<GLTF> load() {
        try {
            return Option.some(new GLTF(Loader::loadFunction));
        } catch (Throwable e) {
            return Option.none();
        }
    }

    private static final FunctionDescriptor DESCRIPTOR$gltf_read = FunctionDescriptor.of(
            ValueLayout.ADDRESS, // *mut GLTFModelCollection
            ValueLayout.ADDRESS  // file: *const c_char
    );
    private static final FunctionDescriptor DESCRIPTOR$gltf_free = FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS  // collection: *mut GLTFModelCollection
    );

    private final MethodHandle HANDLE$gltf_read;
    private final MethodHandle HANDLE$gltf_free;
}
