@chr.wgx.util.NotNullByDefault
module chr.wgx {
    requires com.formdev.flatlaf;
    requires com.google.gson;
    requires java.desktop;
    requires java.logging;
    requires org.jetbrains.annotations;
    requires org.joml;
    requires org.openjdk.nashorn;
    requires club.doki7.gles2;
    requires club.doki7.glfw;
    requires club.doki7.ffm;
    requires club.doki7.vulkan;
    requires club.doki7.vma;

    requires xjbutil;
    requires jsr305;

    exports chr.wgx.config;
    exports chr.wgx.reactor;
    exports chr.wgx.reactor.plugin;
    exports chr.wgx.render;
    exports chr.wgx.render.common;
    exports chr.wgx.render.data;
    exports chr.wgx.render.info;
    exports chr.wgx.render.task;
    exports chr.wgx.ui;
    exports chr.wgx.util;
    exports chr.wgx.drill;
}
