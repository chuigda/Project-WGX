package chr.wgx.render.gles2.data;

public final class AttributeBinding {
    public final String attributeName;
    public final int binding;

    public AttributeBinding(String attributeName, int location) {
        this.attributeName = attributeName;
        this.binding = location;
    }
}
