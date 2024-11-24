package chr.wgx.render.info;

import chr.wgx.render.common.CGType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class VertexInputInfo {
    @SuppressWarnings("ClassCanBeRecord")
    public static final class AttributeIn {
        public final String name;
        public final CGType type;

        public AttributeIn(String name, CGType type) {
            this.name = name;
            this.type = type;
        }
    }

    public static final class Attribute {
        public final String name;
        public final CGType type;

        public final int location;
        public final int byteOffset;

        Attribute(String name, CGType type, int location, int byteOffset) {
            this.name = name;
            this.type = type;
            this.location = location;
            this.byteOffset = byteOffset;
        }
    }

    public final List<Attribute> attributes;
    public final HashMap<String, Attribute> attributeMap;
    public final int stride;

    public VertexInputInfo(List<AttributeIn> attributes) {
        int currentLocation = 0;
        int currentOffset = 0;

        this.attributes = new ArrayList<>();
        this.attributeMap = new HashMap<>();

        for (AttributeIn attribute : attributes) {
            Attribute newAttribute = new Attribute(attribute.name, attribute.type, currentLocation, currentOffset);
            this.attributes.add(newAttribute);
            this.attributeMap.put(attribute.name, newAttribute);

            currentLocation++;
            currentOffset += attribute.type.byteSize;
        }
        this.stride = currentOffset;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }

        VertexInputInfo vertexInputInfo = (VertexInputInfo) obj;
        if (this.attributes.size() != vertexInputInfo.attributes.size()) {
            return false;
        }

        for (int i = 0; i < this.attributes.size(); i++) {
            if (this.attributes.get(i).type != vertexInputInfo.attributes.get(i).type) {
                return false;
            }
            if (!this.attributes.get(i).name.equals(vertexInputInfo.attributes.get(i).name)) {
                return false;
            }
        }
        return true;
    }
}
