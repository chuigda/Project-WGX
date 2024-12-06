package chr.wgx.main;

import chr.wgx.util.gltf.GLTF;

public final class Drill {
    public static void main(String[] args) {
        var loadSuccess = GLTF.loadLibrary();
        assert loadSuccess;

        GLTF gltf = GLTF.load().get();
        var readResult = gltf.read("test.glb").get();

        System.err.println(readResult.models().get(0).modelName());
    }
}
