package me.asu.security.util;

import static me.asu.security.util.PBEUtils.decryptToStream;
import static me.asu.security.util.StringUtils.isEmpty;

import java.io.InputStream;
import java.io.PipedInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import me.asu.security.db.Field;
import me.asu.security.db.Item;
import me.asu.security.db.SecurityDb;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class XmlUtils {

    public static void parseDb(SecurityDb db) {
        SAXParserFactory factory = SAXParserFactory.newInstance();
        try {
            SAXParser        parser  = factory.newSAXParser();
            SAXParserHandler handler = new SAXParserHandler(db);
            InputStream      in      = Files.newInputStream(Paths.get(db.getPath()));
            String           pw      = db.getDbPassword();
            if (isEmpty(pw)) {
                parser.parse(in, handler);
            } else {
                PipedInputStream source = decryptToStream(in, pw);
                parser.parse(source, handler);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class SAXParserHandler extends DefaultHandler {

        SecurityDb db;
        String     currentMetaName;
        Item       currentItem;
        Field      currentField;

        public SAXParserHandler(SecurityDb db) {
            this.db = db;
        }

        private StringBuilder currentValue = new StringBuilder();

        public SecurityDb getDb() {
            return db;
        }

        /**
         * 用来标识解析开始
         */
        @Override
        public void startDocument() throws SAXException {
            super.startDocument();
        }

        /**
         * 用来标识解析结束
         */
        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
        }

        /**
         * 解析xml元素
         */
        @Override
        public void startElement(String uri,
                                 String localName,
                                 String qName,
                                 Attributes attributes) throws SAXException {
            //调用DefaultHandler类的startElement方法
            super.startElement(uri, localName, qName, attributes);
            currentValue.setLength(0);
            if (qName.equals("db")) {
            } else if (qName.equals("data")) {
            } else if (qName.equals("meta")) {
                parseMeta(attributes);
            } else if (qName.equals("item")) {
                parseItem(attributes);
            } else if (qName.equals("field")) {
                parseField(attributes);
            }

        }

        private void parseField(Attributes attributes) {
            currentField = new Field();
            currentField.setName(attributes.getValue("name"));
            currentField.setContent(attributes.getValue("content"));
        }

        private void parseItem(Attributes attributes) {
            currentItem = new Item();
            currentItem.setId(attributes.getValue("id"));
            String tags = attributes.getValue("tags");
            if (tags != null && !tags.trim().isEmpty()) {
                String[] split = tags.split("\\s+");
                currentItem.addTags(Arrays.asList(split));
            }
        }

        private void parseMeta(Attributes attributes) {
            String name = attributes.getValue("name");
            currentMetaName = name;
            if (name == null) { return; }

            String content = attributes.getValue("content");
            switch (name) {
                case "version":
                    db.setVersion(content);
                    break;
                case "description":
                    db.setDescription(content);
                    break;
                default:
                    System.out.println("Unknown meta: " + name);
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName)
        throws SAXException {
            //调用DefaultHandler类的endElement方法
            super.endElement(uri, localName, qName);
            if (qName.equals("item")) {
                db.addItem(currentItem);
                currentItem = null;
            } else if (qName.equals("field")) {
                if (currentValue.length() > 0) {
                    currentField.setContent(currentValue.toString().trim());
                }
                currentItem.addFiled(currentField);
                currentField = null;
            } else if (qName.equals("meta")) {
                if (currentValue.length() > 0 && !isEmpty(currentMetaName)) {
                    switch (currentMetaName) {
                        case "version":
                            db.setVersion(currentValue.toString().trim());
                            break;
                        case "description":
                            db.setDescription(currentValue.toString().trim());
                            break;
                        default:
                            System.out.println(
                                    "Unknown meta: " + currentMetaName);
                    }
                }
                currentMetaName = null;
            } else if (qName.equals("data")) {
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
        throws SAXException {
            super.characters(ch, start, length);
            currentValue.append(new String(ch, start, length));
        }
    }

    public static void main(String[] args) {
        String     testPath = "src/test/resources/sample.db.xml";
        SecurityDb db       = new SecurityDb();
        db.setPath(testPath);
        XmlUtils.parseDb(db);

        System.out.println(db.toXmlString());
    }
}

