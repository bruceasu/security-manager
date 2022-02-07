package me.asu.security.db;

import static me.asu.security.util.PBEUtils.SALT;
import static me.asu.security.util.StringUtils.isBlank;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import lombok.Data;
import me.asu.security.util.PBEUtils;
import me.asu.security.util.Wildcard;
import me.asu.security.util.XmlUtils;

@Data
public class SecurityDb {

    public static final String CURRENT_VERSION     = "1";

    public static final String XML_HEADER          = "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>";

    String path;
    String description;
    String version = CURRENT_VERSION;
    String dbPassword;

    final Map<String, Item> items = new HashMap<>();

    public Map<String, Item> getItems() {
        return items;
    }

    public void addItem(Item item) {
        if (item == null) { return; }
        this.items.put(item.getId(), item);
    }

    public void store() throws Exception {
        Path p      = Paths.get(path);
        Path parent = p.getParent();
        if (!Files.isDirectory(parent)) {
            Files.createDirectories(parent);
        }
        PBEUtils.encryptToFile(toXmlString(), p.toFile(), getDbPassword());
    }

    public void load() {
        XmlUtils.parseDb(this);
    }

    public String toXmlString() {
        StringBuilder buffer = new StringBuilder();

        buffer.append(XML_HEADER).append("\n");
        buffer.append("<db>\n");
        // meta
        appendMeta(buffer, "version", version);
        appendMeta(buffer, "description", description);

        // items
        buffer.append("\t<data>\n");
        items.values().forEach(i -> {
            buffer.append(i.toXmlString("\t\t"));
        });
        buffer.append("\t</data>\n");

        buffer.append("</db>\n");

        return buffer.toString();
    }

    private void appendMeta(StringBuilder buffer, String name, String content) {
        if (content == null) {
            buffer.append("\t<meta name=\"").append(name).append("\" content=\"\" />\n");
        } else {
            if (content.contains("&") || content.contains("\"") || content.contains("<") || content.contains(">")
                    || content.contains("'") || content.contains("\"")) {
                buffer.append("\t<meta name=\"").append(name).append("\">\n");
                buffer.append("\t\t<![CDATA[").append(content).append("]]>\n");
                buffer.append("\t</meta>\n");
            } else {
                buffer.append("\t<meta name=\"").append(name).append("\"")
                      .append(" content=\"").append(content).append("\" />\n");
            }
        }

    }

    public Item removeItemById(String entry) {
        return items.remove(entry);
    }

    public Item getItemById(String entry) {
        return items.get(entry);
    }

    /**
     * filter items by entry and tags
     *
     * @param entry entry id, support wide char ? and *
     * @param tag   entry tag, multi tags by comma.
     * @return acceptable items
     */
    public Set<Item> filterItems(String entry, String tag) {
        if (items.isEmpty()) { return Collections.emptySet(); }
        Collection<Item> values = items.values();
        Set<Item>        result = new HashSet<>();
        if (!isBlank(entry)) {
            values.forEach(i -> {
                String id = i.getId();
                if (Wildcard.matches(entry.trim(), id)) {
                    result.add(i);
                }
            });
        } else {
            result.addAll(values);
        }

        if (!isBlank(tag)) {
            Iterator<Item> iterator = result.iterator();
            String[]       arr      = tag.split(",");
            while (iterator.hasNext()) {
                Item        next   = iterator.next();
                Set<String> tags   = next.getTags();
                boolean     accept = false;
                for (String s : arr) {
                    if (tags.contains(s)) {
                        accept = true;
                        break;
                    }
                }
                if (!accept) {
                    iterator.remove();
                }
            }
        }
        return result;
    }
}
