package chr.wgx.render.info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public final class VertexInputInfo {
    public final List<FieldInfo> attributes;
    public final HashMap<String, FieldInfo> attributeMap;
    public final int stride;

    public VertexInputInfo(List<FieldInfoInput> attributes) {
        int currentLocation = 0;
        int currentOffset = 0;

        this.attributes = new ArrayList<>();
        this.attributeMap = new HashMap<>();

        for (FieldInfoInput attribute : attributes) {
            assert !attribute.type.isMat;

            FieldInfo newAttribute = new FieldInfo(attribute.name, attribute.type, currentLocation, currentOffset);
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
