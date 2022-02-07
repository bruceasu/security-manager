package me.asu.security.simplejavatexteditor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import javax.swing.ImageIcon;

public class Icons {
    public static final ImageIcon steIcon = new ImageIcon(loadResource("icons/ste.png"));

    //setup icons - Bold and Italic
    public static final ImageIcon boldIcon   = new ImageIcon(loadResource("icons/bold.png"));
    public static final ImageIcon italicIcon = new ImageIcon(loadResource("icons/italic.png"));

    // setup icons - File Menu
    public static final ImageIcon newIcon = new ImageIcon(loadResource("icons/new.png"));
    public static final ImageIcon openIcon = new ImageIcon(loadResource("icons/open.png"));
    public static final ImageIcon saveIcon = new ImageIcon(loadResource("icons/save.png"));
    public static final ImageIcon closeIcon = new ImageIcon(loadResource("icons/close.png"));

    // setup icons - Edit Menu
    public static final ImageIcon clearIcon = new ImageIcon(loadResource("icons/clear.png"));
    public static final ImageIcon cutIcon = new ImageIcon(loadResource("icons/cut.png"));
    public static final ImageIcon copyIcon = new ImageIcon(loadResource("icons/copy.png"));
    public static final ImageIcon pasteIcon = new ImageIcon(loadResource("icons/paste.png"));
    public static final ImageIcon selectAllIcon = new ImageIcon(loadResource("icons/selectall.png"));
    public static final ImageIcon wordwrapIcon = new ImageIcon(loadResource("icons/wordwrap.png"));

    // setup icons - Search Menu
    public static final ImageIcon searchIcon = new ImageIcon(loadResource("icons/search.png"));

    // setup icons - Help Menu
    public static final ImageIcon aboutMeIcon = new ImageIcon(loadResource("icons/about_me.png"));
    public static final ImageIcon aboutIcon = new ImageIcon(loadResource("icons/about.png"));



    private static byte[] loadResource(String path) {
        Path p = Paths.get(path);
        if (Files.isRegularFile(p)) {
            try {
                return Files.readAllBytes(p);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        InputStream in = UI.class.getClassLoader().getResourceAsStream(path);
        if (in != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            copy(in, out);
            byte[] bytes = out.toByteArray();
            safeClose(in);
            safeClose(out);
            return bytes;
        } else {
            return null;
        }
    }

    private static void copy(InputStream in, OutputStream pos) {
        byte buffer[] = new byte[4096];
        int  len      = 0;
        while (true) {
            try {
                len = in.read(buffer);
                if (len == - 1) break;
                pos.write(buffer, 0, len);
            } catch (IOException e) {
                e.printStackTrace();
                break;
            } catch (Exception e) {
                e.printStackTrace();
                break;
            }
        }
    }

    private static void safeClose(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                // ingored.
            }
        }
    }
}
