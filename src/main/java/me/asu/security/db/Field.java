package me.asu.security.db;

import lombok.Data;

@Data
public class Field {
        String      name;
        String      content;
        public String toXmlString(String indent) {
            StringBuilder buffer = new StringBuilder();
            buffer.append(indent).append("<field name=").append('\"').append(name).append('\"');
            if (content == null || content.trim().isEmpty()) {
                buffer.append(" />\n");
            } else if (content.contains("&")
                    || content.contains("\"")
                    || content.contains("<")
                    || content.contains(">")
                    || content.contains("'")
                    || content.contains("\"")
            ) {
                buffer.append(">\n");
                buffer.append(indent).append("\t<![CDATA[");
                buffer.append(content);
                buffer.append("]]>\n");
                buffer.append(indent).append("</field>\n");
            } else {
                buffer.append(" content=\"").append(content).append("\" />\n");
            }
            return buffer.toString();
        }
    }