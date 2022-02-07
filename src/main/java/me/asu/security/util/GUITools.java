package me.asu.security.util;

import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.swing.JTextArea;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.FontUIResource;
import javax.swing.plaf.metal.DefaultMetalTheme;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;

public class GUITools {

    public static        Clipboard             clipboard = Toolkit.getDefaultToolkit()
                                                            .getSystemClipboard();
    private static final GraphicsDevice        device    = GraphicsEnvironment
            .getLocalGraphicsEnvironment().getScreenDevices()[0];
    private static final Map<Frame, Rectangle> winStates = new HashMap<>();

    public static void center(Window win) {
        // 获得窗口宽
        int windowWidth = win.getWidth();
        // 获得窗口高
        int windowHeight = win.getHeight();
        // 定义工具包
        Toolkit kit = Toolkit.getDefaultToolkit();
        // 获取屏幕的尺寸
        Dimension screenSize = kit.getScreenSize();
        // 获取屏幕的宽
        int screenWidth = screenSize.width;
        // 获取屏幕的高
        int screenHeight = screenSize.height;
        // 设置窗口居中显示
        win.setLocation(screenWidth / 2 - windowWidth / 2, screenHeight / 2 - windowHeight / 2);
    }

    /**
     * attach Ctrl+a, Ctrl+x, Ctrl+v, Ctrl+z, Ctrl+r
     *
     * @param editors JTextComponent[]
     */
    public static void attachKeyListener(JTextComponent... editors) {
        //撤销管理类
        UndoManager um = new UndoManager();
        attachKeyListener(um, editors);
    }

    public static void attachKeyListener(UndoManager um, JTextComponent... editors) {
        for (JTextComponent editor : editors) {
            editor.addKeyListener(GUITools.getKeyAdapter(editor, um));
            //编辑撤销的监听
            editor.getDocument().addUndoableEditListener(e -> um.addEdit(e.getEdit()));
        }
    }

    public static void attachKeyListener(UndoManager um, java.util.List<JTextComponent> editors) {
        attachKeyListener(um, editors.toArray(new JTextComponent[0]));
    }

    public static void attachKeyListener(java.util.List<JTextComponent> editors) {
        //撤销管理类
        UndoManager um = new UndoManager();
        attachKeyListener(um, editors.toArray(new JTextComponent[0]));
    }


    /**
     * attach Ctrl+a, Ctrl+x, Ctrl+v, Ctrl+z, Ctrl+r
     *
     * @param component JTextComponent
     * @param um        UndoManager
     * @return KeyAdapter
     */
    public static KeyAdapter getKeyAdapter(final JTextComponent component, UndoManager um) {

        return new KeyAdapter() {
            /**
             * Invoked when a key has been pressed.
             *
             * @param e
             */
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.isControlDown() && (e.getKeyCode() == KeyEvent.VK_A/*全选*/
                        || e.getKeyCode() == KeyEvent.VK_C/* 复制 */
                        || e.getKeyCode() == KeyEvent.VK_X/*剪切*/ || e.getKeyCode() == KeyEvent.VK_V
                        || e.getKeyCode() == KeyEvent.VK_Z || e.getKeyCode() == KeyEvent.VK_R)) {
                    //                      System.out.println(e.getKeyCode());
                } else {
                    return;
                }
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_A:
                        component.setSelectionStart(0);
                        component.setSelectionEnd(component.getText().length());
                        break;
                    case KeyEvent.VK_C:
                        String selectedText = component.getSelectedText();

                        if (selectedText == null || selectedText.isEmpty()) {
                            selectedText = component.getText();
                        }
                        //设置字符串
                        //构建String数据类型
                        StringSelection selection = new StringSelection(selectedText);
                        //添加文本到系统剪切板
                        clipboard.setContents(selection, null);
                        break;
                    case KeyEvent.VK_V:
                        //从系统剪切板中获取数据
                        Transferable content = clipboard.getContents(null);
                        //判断是否为文本类型
                        if (content.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                            String text = null;//从数据中获取文本值
                            try {
                                text = (String) content.getTransferData(DataFlavor.stringFlavor);
                            } catch (UnsupportedFlavorException e1) {
                                e1.printStackTrace();
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }
                            if (text == null) {
                                return;
                            }
                            if (component instanceof JTextArea) {
                                ((JTextArea) component)
                                        .replaceRange(text, component.getSelectionStart(),
                                                component.getSelectionEnd());
                            } else if (component instanceof JTextComponent) {
                                component.replaceSelection(text);
                            }
                            component.paintImmediately(component.getBounds());
                        }
                        break;
                    case KeyEvent.VK_X:
                        String selectedText2 = component.getSelectedText();
                        if (selectedText2 == null || selectedText2.isEmpty()) {
                            selectedText2 = component.getText();
                            component.setText("");
                        } else {
                            if (component instanceof JTextArea) {
                                ((JTextArea) component)
                                        .replaceRange("", component.getSelectionStart(),
                                                component.getSelectionEnd());
                            } else if (component instanceof JTextComponent) {
                                component.replaceSelection("");
                            }
                        }
                        //设置字符串
                        //构建String数据类型
                        StringSelection selection2 = new StringSelection(selectedText2);
                        //添加文本到系统剪切板
                        clipboard.setContents(selection2, null);
                        break;
                    case KeyEvent.VK_R:
                        um.redo();
                        break;
                    case KeyEvent.VK_Z:
                        um.undo();
                    default:
                        break;
                }
            }
        };
    }

    public static void initLookAndFeel() {
        String[] lafArray = new String[]{
                "com.alee.laf.WebLookAndFeel",

                "ch.randelshofer.quaqua.mountainlion.Quaqua16MountainLionLookAndFeel",
                "ch.randelshofer.quaqua.snowleopard.Quaqua16SnowLeopardLookAndFeel",
                "ch.randelshofer.quaqua.lion.Quaqua16LionLookAndFeel",
                "ch.randelshofer.quaqua.leopard.Quaqua16LeopardLookAndFeel",

                "ch.randelshofer.quaqua.leopard.Quaqua15LeopardLookAndFeel",
                "ch.randelshofer.quaqua.leopard.Quaqua15LeopardLookAndFeel",
                "ch.randelshofer.quaqua.leopard.Quaqua15LeopardCrossPlatformLookAndFeel",
                "ch.randelshofer.quaqua.leopard.Quaqua15LeopardCrossPlatformLookAndFeel",

                "ch.randelshofer.quaqua.tiger.Quaqua15TigerLookAndFeel",
                "ch.randelshofer.quaqua.tiger.Quaqua15TigerLookAndFeel",
                "ch.randelshofer.quaqua.tiger.Quaqua15TigerCrossPlatformLookAndFeel",

                "ch.randelshofer.quaqua.jaguar.Quaqua15JaguarLookAndFeel",
                "ch.randelshofer.quaqua.jaguar.Quaqua15JaguarLookAndFeel",
                "ch.randelshofer.quaqua.panther.Quaqua15PantherLookAndFeel",
                "ch.randelshofer.quaqua.panther.Quaqua15PantherLookAndFeel",

                "com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
                "com.sun.java.swing.plaf.windows.WindowsClassicLookAndFeel",
                "javax.swing.plaf.nimbus.NimbusLookAndFeel",
                "com.sun.java.swing.plaf.gtk.GTKLookAndFeel",
                UIManager.getSystemLookAndFeelClassName(),
                UIManager.getCrossPlatformLookAndFeelClassName(),
                "com.sun.java.swing.plaf.motif.MotifLookAndFeel"

        };

        //        // You should work with UI (including installing L&F) inside Event Dispatch Thread (EDT)
        //        SwingUtilities.invokeLater (new Runnable () {
        //            public void run ()
        //            {
        //                // Install WebLaF as application L&F
        //                WebLookAndFeel.install ();
        //
        //                // Create you Swing application here
        //                // JFrame frame = ...
        //            }
        //        } );
        for (String lookAndFeel : lafArray) {
            try {
                UIManager.setLookAndFeel(lookAndFeel);

                // If L&F = "Metal", set the theme
                if ("Metal".equals(lookAndFeel)) {
                    MetalLookAndFeel.setCurrentTheme(new DefaultMetalTheme());
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
                }
                System.out.println("using " + lookAndFeel);
                break;
            } catch (ClassNotFoundException e) {
                System.err.printf("Couldn't find class for specified look and feel:%s%n",
                        lookAndFeel);
            } catch (UnsupportedLookAndFeelException e) {
                System.err.printf("Can't use the specified look and feel (%s) on this platform.%n",
                        lookAndFeel);
            } catch (Throwable e) {
                System.err.printf("Couldn't get specified look and feel (%s), for some reason.%n",
                        lookAndFeel);
                e.printStackTrace();
            }
        }
        Font font = new Font("PMingLiU", Font.PLAIN, 16);
        initGlobalFont(font);
    }

    //统一界面字体
    public static void initGlobalFont(Font font) {
        FontUIResource fontRes = new FontUIResource(font);
        for (Enumeration<Object> keys = UIManager.getDefaults().keys(); keys.hasMoreElements(); ) {
            Object key   = keys.nextElement();
            Object value = UIManager.get(key);
            if (value instanceof FontUIResource) {
                UIManager.put(key, fontRes);
            }
        }

    }

    public static void setUIFont(Font font, String... names) {

        for (String item : names) {
            UIManager.put(item + ".font", font);
        }
    }

    public static void setUIFont() {
        Font f = new Font("Simsun", Font.PLAIN, 18);
        String[] names = {"Label", "CheckBox", "PopupMenu", "MenuItem", "CheckBoxMenuItem",
                "JRadioButtonMenuItem", "ComboBox", "Button", "Tree", "ScrollPane", "TabbedPane",
                "EditorPane", "TitledBorder", "Menu", "TextArea", "OptionPane", "MenuBar",
                "ToolBar", "ToggleButton", "ToolTip", "ProgressBar", "TableHeader", "Panel", "List",
                "ColorChooser", "PasswordField", "TextField", "Table", "Label", "Viewport",
                "RadioButtonMenuItem", "RadioButton", "DesktopPane", "InternalFrame"};
        for (String item : names) {
            UIManager.put(item + ".font", f);
        }
    }


    public static void fullscreen(Frame win, boolean flag) {
        if (flag) {
            win.setVisible(false);
            Rectangle bounds = win.getBounds();
            winStates.put(win, bounds);

            win.dispose();
            win.setUndecorated(true);
            Dimension screenSize = win.getToolkit().getScreenSize();
            win.setBounds(0, 0, screenSize.width, screenSize.height);

            //            device.setFullScreenWindow(win);
            win.setAlwaysOnTop(true);
            win.setVisible(true);
        } else {
            win.setVisible(false);
            win.dispose();
            win.setUndecorated(false);
            win.setBounds(winStates.get(win));
            //            device.setFullScreenWindow(null);
            win.setAlwaysOnTop(false);
            win.setVisible(true);
        }
    }
}
