package me.asu.security.db;

import java.util.*;
import lombok.Data;

@Data
public class Item {

    String id;
    final Set<String>        tags   = new HashSet<>();
    final Map<String, Field> fields = new HashMap<>();

    public String toXmlString(String indent) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(indent)
              .append("<item id=")
              .append('\"')
              .append(id)
              .append('\"');
        if (!tags.isEmpty()) {
            buffer.append(" tags=").append('\"');
            for (String tag : tags) {
                buffer.append(tag).append(' ');
            }
            buffer.append('\"');
        }
        if (fields.isEmpty()) {
            buffer.append(" />\n");
        } else {
            buffer.append(" >\n");
            fields.values().forEach(f -> {
                buffer.append(f.toXmlString(indent + "\t"));
            });
            buffer.append(indent).append("</item>\n");
        }
        return buffer.toString();
    }

    public void addTags(List<String> addTags) {
        if (addTags == null || addTags.isEmpty()) { return; }
        this.tags.addAll(addTags);
    }

    public void removeTags(List<String> rmTags) {
        if (rmTags == null || rmTags.isEmpty()) { return; }
        this.tags.removeAll(rmTags);
    }

    public void addFiled(Field f) {
        if (f == null) { return; }
        fields.put(f.getName(), f);
    }

    public void removeField(String name) {
        fields.remove(name);
    }
}