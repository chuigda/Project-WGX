package tech.icey.wgx.core;

import tech.icey.wgx.babel.BabelPlugin;
import tech.icey.wgx.babel.DataManipulator;
import tech.icey.wgx.babel.Masterpiece;

import java.util.List;

final class MatrixGeneratorComponent implements DataManipulator {
    @Override
    public int priority() {
        return 10000;
    }

    @Override
    public void initialise(Masterpiece masterpiece) {
        this.masterpiece = masterpiece;
    }

    @Override
    public void manipulate(Masterpiece masterpiece) {

    }

    private Masterpiece masterpiece;
}

public final class MatrixGeneratorPlugin implements BabelPlugin {
    @Override
    public String getName() {
        return MatrixGeneratorPlugin.class.getName();
    }

    @Override
    public String getDescription() {
        return "根据 ModelPosition 数据生成视图矩阵";
    }

    @Override
    public List<Object> getComponents() {
        return List.of(
                new MatrixGeneratorComponent()
        );
    }
}