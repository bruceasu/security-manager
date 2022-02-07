package me.asu.security.util;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.Robot;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.KeyEvent;
import java.io.IOException;

public class AutoClipboard {

    private static final Clipboard clip = Toolkit.getDefaultToolkit().getSystemClipboard();

    /**
     * 从剪切板获得文字。
     */
    public static String getSysClipboardText() {
        String ret = "";
        // 获取剪切板中的内容
        Transferable clipTf = clip.getContents(null);

        if (clipTf != null) {
            // 检查内容是否是文本类型
            if (clipTf.isDataFlavorSupported(DataFlavor.stringFlavor)) {
                try {
                    ret = (String) clipTf.getTransferData(DataFlavor.stringFlavor);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        return ret;
    }

    public static void ctrlC() {
        try {
            Toolkit.getDefaultToolkit().setLockingKeyState(KeyEvent.VK_NUM_LOCK, false);
            Robot robot = new Robot();
            Thread.sleep(200L);
            robot.keyPress(KeyEvent.VK_HOME);
            robot.keyRelease(KeyEvent.VK_HOME);
            robot.keyPress(KeyEvent.VK_SHIFT);
            robot.keyPress(KeyEvent.VK_END);

            robot.keyRelease(KeyEvent.VK_END);
            robot.keyRelease(KeyEvent.VK_SHIFT);

            Thread.sleep(100);
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_C);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void ctrlV() {
        try {
            Robot robot = new Robot();
            robot.keyPress(KeyEvent.VK_CONTROL);
            robot.keyPress(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_V);
            robot.keyRelease(KeyEvent.VK_CONTROL);
        } catch (AWTException e) {
            e.printStackTrace();
        }
    }
    /**
     * 将字符串复制到剪切板。
     */
    public static void setSysClipboardText(String writeMe) {
        Transferable tText = new StringSelection(writeMe);
        clip.setContents(tText, null);
    }

    /**
     * 从剪切板获得图片。
     */
    public static Image getImageFromClipboard() throws Exception {
        Transferable cc = clip.getContents(null);
        if (cc == null) {
            return null;
        } else if (cc.isDataFlavorSupported(DataFlavor.imageFlavor)) {
            return (Image) cc.getTransferData(DataFlavor.imageFlavor);
        }
        return null;
    }

    /**
     * 复制图片到剪切板。
     */
    public static void setClipboardImage(final Image image) {
        Transferable trans = new Transferable() {
            @Override
            public DataFlavor[] getTransferDataFlavors() {
                return new DataFlavor[]{DataFlavor.imageFlavor};
            }

            @Override
            public boolean isDataFlavorSupported(DataFlavor flavor) {
                return DataFlavor.imageFlavor.equals(flavor);
            }

            @Override
            public Object getTransferData(DataFlavor flavor)
            throws UnsupportedFlavorException, IOException {
                if (isDataFlavorSupported(flavor)) {
                    return image;
                }
                throw new UnsupportedFlavorException(flavor);
            }

        };

        clip.setContents(trans, null);
    }
}
