package chr.wgx.reactor.plugin;

public interface IPlugin {
    default String className() {
        return this.getClass().getName();
    }
}
