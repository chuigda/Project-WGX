import chr.wgx.util.NotNullByDefault;

@NotNullByDefault
module chr.wgx {
    requires com.formdev.flatlaf;
    requires com.google.gson;
    requires java.desktop;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires org.joml;
    requires org.openjdk.nashorn;
    requires tech.icey.gles2;
    requires tech.icey.glfw;
    requires tech.icey.panama;
    requires tech.icey.vk4j;
    requires tech.icey.vma;

    requires xjbutil;
    requires jsr305;

    exports chr.wgx.config;
    exports chr.wgx.reactor;
    exports chr.wgx.reactor.plugin;
    exports chr.wgx.render;
    exports chr.wgx.render.common;
    exports chr.wgx.render.data;
    exports chr.wgx.render.info;
    exports chr.wgx.ui;

    exports chr.wgx.drill;
    exports chr.wgx.render.task;
}
