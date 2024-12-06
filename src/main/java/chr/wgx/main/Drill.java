package chr.wgx.main;

import chr.wgx.util.gltf.GLTF;

public final class Drill {
    public static void main(String[] args) {
        var loadSuccess = GLTF.loadLibrary();
        assert loadSuccess;

        GLTF gltf = GLTF.load().get();
        var readResult = gltf.read("test.glb").get();

        var model = readResult.models().get(0);
        System.err.println(model.modelName());
        System.err.println(model.vertexCount());
        System.err.println(model.indexCount());
    }
}
