package mpesa.b2b;

import base.Helpers;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import lombok.Data;

import java.io.IOException;
import java.util.List;

class ReferenceItemDeserializer extends StdDeserializer<ReferenceData> {
    public ReferenceItemDeserializer() {
        this(null);
    }

    protected ReferenceItemDeserializer(Class<?> vc) {
        super(vc);
    }

    @Override
    public ReferenceData deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException, JacksonException {
        ReferenceData referenceData = new ReferenceData();
        TreeNode referenceItemNode = jsonParser.readValueAsTree().get("ReferenceItem");
        if (referenceItemNode.isArray()) {
            referenceData.setReferenceItem(Helpers.jsonToList(ReferenceItem.class, referenceItemNode.toString()));
        } else {
            List<ReferenceItem> listItem = List.of(Helpers.jsonToPOJO(ReferenceItem.class, referenceItemNode.toString()));
            referenceData.setReferenceItem(listItem);
        }
        return referenceData;
    }
}

@Data
@JsonDeserialize(using = ReferenceItemDeserializer.class)
public class ReferenceData {
    @JsonProperty("ReferenceItem")
    private List<ReferenceItem> referenceItem;
}
