/**
 * @name Simple Java NotePad
 * @package ph.notepad
 * @file UI.java
 * @author Pierre-Henry Soria
 * @email pierrehenrysoria@gmail.com
 * @link http://github.com/pH-7
 * @copyright Copyright Pierre-Henry SORIA, All Rights Reserved.
 * @license Apache (http://www.apache.org/licenses/LICENSE-2.0)
 * @create 2012-04-05
 * @update 2017-02-18
 * @modifiedby Achintha Gunasekara
 * @modemail contact@achinthagunasekara.com
 * @modifiedby Marcus Redgrave-Close
 * @modemail marcusrc1@hotmail.co.uk
 * @Modifiedby SidaDan
 * @modemail Fschultz@sinf.de
 * Added Tooltip Combobox Font type and Font size
 * Overwrite method processWindowEvent to detect window closing event.
 * Added safety query to save the file before exit
 * or the user select "newfile"
 * Only available if the user has pressed a key
 * Added safety query if user pressed the clearButton
 * @Modifiedby SidaDan
 * @modemail Fschultz@sinf.de
 * Removed unuse objects like container,  Border emptyBorder
 * Removed unsused imports
 * @Modifiedby Giorgos Pasentsis
 * @modemail gpasents@gmail.com
 */
package me.asu.security.simplejavatexteditor;

import static java.awt.FlowLayout.LEFT;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static me.asu.security.simplejavatexteditor.Icons.*;
import static me.asu.security.simplejavatexteditor.PasswordPanel.readPassword;
import static me.asu.security.util.StringUtils.isEmpty;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.dnd.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Base64.Decoder;
import java.util.List;
import java.util.Base64.Encoder;
import javax.swing.*;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultStyledDocument;
import me.asu.security.util.GzipUtils;
import me.asu.security.util.PBEUtils;

public class UI extends JFrame implements ActionListener {

    private final  String[] dragDropExtensionFilter = {".txt", ".java", ".c",
            ".h", ".cpp", ".dat", ".log", ".xml", ".md", ".org", ".html",
            ".htm"};
    private static long     serialVersionUID        = 1L;


    JTextArea          textArea;
    JMenuBar           menuBar;
    JComboBox<String>  fontType;
    JComboBox<Integer> fontSize;
    JMenu              menuFile, menuEdit, menuFind, menuAbout, menuTool, menuDocument, menuLineSeparator, menuReloadCharset, menuCharset;
    JMenuItem newFile, openFile, saveFile, close, cut, copy, paste, clearFile, selectAll, quickFind, aboutMe, aboutSoftware, wordWrap, encryptFile, decryptFile;
    JToolBar mainToolbar;
    JButton  newButton, openButton, saveButton, clearButton, quickButton, aboutMeButton, aboutButton, closeButton;
    JLabel statusText = new JLabel(" ");
    Action selectAllAction;

    private SupportedKeywords kw                  = new SupportedKeywords();

    private HighlightText     languageHighlighter = new HighlightText(Color.RED);

    AutoComplete autocomplete;
    private          boolean hasListener   = false;
    private          boolean edit          = false;
    private volatile boolean isClose       = false;
    private          String  fileEncoding  = Charset.defaultCharset().name();
    private          String  lineSeparator = System.getProperty("line.separator",
                                                                "\n");
    private          File    curDir;
    private          String  currentFile;

    public UI() {
        try {
            super.setIconImage(steIcon.getImage());
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        // Set the initial size of the window
        setSize(800, 500);

        // Set the title of the window
        setTitle("Untitled | " + SimpleJavaTextEditor.NAME);

        // Set the default close operation (exit when it gets closed)
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // center the frame on the monitor
        setLocationRelativeTo(null);
        getContentPane().setLayout(new BorderLayout()); // the BorderLayout bit makes it fill it automatically

        createMenu();
        createToolBar();
        createEditor();

        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane);
        getContentPane().add(panel);

        JPanel statusBar = new JPanel(new FlowLayout(LEFT));
        statusBar.add(statusText);

        this.add(statusBar, BorderLayout.SOUTH);
        updateStatusText();
    }

    private void createEditor() {
        // Set a default font for the TextArea
        textArea = new JTextArea("", 0, 0);
        textArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        textArea.setTabSize(2);
        textArea.setFont(new Font("Century Gothic", Font.PLAIN, 12));
        textArea.setTabSize(2);

        /* SETTING BY DEFAULT WORD WRAP ENABLED OR TRUE */
        textArea.setLineWrap(true);
        DropTarget dropTarget = new DropTarget(textArea, dropTargetListener);
        textArea.setDocument(new DefaultStyledDocument());
        // Set an higlighter to the JTextArea
        textArea.addKeyListener(new KeyAdapter() {


            @Override
            public void keyReleased(KeyEvent e) {
                updateStatusText();
            }

            @Override
            public void keyPressed(KeyEvent ke) {
                int code = ke.getKeyCode();
                // todo: 一些組合移動鍵也不會修改文件內容。
                //  SHIFT_MASK CTRL_MASK META_MASK ALT_MASK
                int modifiers = ke.getModifiers();

                switch (code) {
                    case KeyEvent.VK_UP:
                    case KeyEvent.VK_DOWN:
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                    case KeyEvent.VK_KP_UP:
                    case KeyEvent.VK_KP_DOWN:
                    case KeyEvent.VK_KP_LEFT:
                    case KeyEvent.VK_KP_RIGHT:
                        return;
                }
                edit = true;
//                languageHighlighter.highLight(textArea, kw.getCppKeywords());
//                languageHighlighter.highLight(textArea, kw.getJavaKeywords());
            }
        });

        textArea.setWrapStyleWord(true);
    }

    private void updateStatusText() {
        SwingUtilities.invokeLater(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                int           length  = textArea.getText().length();
                int           pos     = textArea.getCaretPosition();
                int           row     = textArea.getLineOfOffset(pos) + 1;
                // 另一种計算行的方法
//                Rectangle rec = textArea.modelToView(pos);
//                int row = rec.y / rec.height + 1;
                int col = pos - textArea.getLineStartOffset(row - 1) + 1;

                builder.append(row).append(":").append(col).append(" ");
                if (length > 0) {
                    builder.append(100 * pos / length).append("%");
                }
                builder.append(" ")
                       .append(textArea.getLineWrap() ? "Wrap" : "");
                builder.append(" ").append(LnSep.getByVal(lineSeparator));
                builder.append(" ").append(getFileEncoding());
                if (edit) builder.append(" * ");
                statusText.setText(builder.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

    }

    private void createToolBar() {
        mainToolbar = new JToolBar();
        this.add(mainToolbar, BorderLayout.NORTH);
        // used to create space between button groups
        newButton = new JButton(newIcon);
        newButton.setToolTipText("New");
        newButton.addActionListener(this);
        mainToolbar.add(newButton);
        mainToolbar.addSeparator();

        openButton = new JButton(openIcon);
        openButton.setToolTipText("Open");
        openButton.addActionListener(this);
        mainToolbar.add(openButton);
        mainToolbar.addSeparator();

        saveButton = new JButton(saveIcon);
        saveButton.setToolTipText("Save");
        saveButton.addActionListener(this);
        mainToolbar.add(saveButton);
        mainToolbar.addSeparator();

        clearButton = new JButton(clearIcon);
        clearButton.setToolTipText("Clear All");
        clearButton.addActionListener(this);
        mainToolbar.add(clearButton);
        mainToolbar.addSeparator();

        quickButton = new JButton(searchIcon);
        quickButton.setToolTipText("Quick Search");
        quickButton.addActionListener(this);
        mainToolbar.add(quickButton);
        mainToolbar.addSeparator();

        aboutMeButton = new JButton(aboutMeIcon);
        aboutMeButton.setToolTipText("About Me");
        aboutMeButton.addActionListener(this);
        mainToolbar.add(aboutMeButton);
        mainToolbar.addSeparator();

        aboutButton = new JButton(aboutIcon);
        aboutButton.setToolTipText("About NotePad PH");
        aboutButton.addActionListener(this);
        mainToolbar.add(aboutButton);
        mainToolbar.addSeparator();

        closeButton = new JButton(closeIcon);
        closeButton.setToolTipText("Quit");
        closeButton.addActionListener(this);
        mainToolbar.add(closeButton);
        mainToolbar.addSeparator();

        /**
         * **************** FONT SETTINGS SECTION **********************
         */
        //FONT FAMILY SETTINGS SECTION START
        fontType = new JComboBox<String>();

        //GETTING ALL AVAILABLE FONT FOMILY NAMES
        String[] fonts = GraphicsEnvironment.getLocalGraphicsEnvironment()
                                            .getAvailableFontFamilyNames();

        for (String font : fonts) {
            //Adding font family names to font[] array
            fontType.addItem(font);
        }
        //Setting maximize size of the fontType ComboBox
        fontType.setMaximumSize(new Dimension(170, 30));
        fontType.setToolTipText("Font Type");
        mainToolbar.add(fontType);
        mainToolbar.addSeparator();

        //Adding Action Listener on fontType JComboBox
        fontType.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                //Getting the selected fontType value from ComboBox
                String p = fontType.getSelectedItem().toString();
                //Getting size of the current font or text
                int s = textArea.getFont().getSize();
                textArea.setFont(new Font(p, Font.PLAIN, s));
            }
        });

        //FONT FAMILY SETTINGS SECTION END
        //FONT SIZE SETTINGS START
        fontSize = new JComboBox<Integer>();

        for (int i = 8; i <= 100; i++) {
            fontSize.addItem(i);
        }
        fontSize.setMaximumSize(new Dimension(70, 30));
        fontSize.setToolTipText("Font Size");
        mainToolbar.add(fontSize);

        fontSize.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                String sizeValue  = fontSize.getSelectedItem().toString();
                int    sizeOfFont = Integer.parseInt(sizeValue);
                String fontFamily = textArea.getFont().getFamily();

                Font font1 = new Font(fontFamily, Font.PLAIN, sizeOfFont);
                textArea.setFont(font1);
            }
        });
        //FONT SIZE SETTINGS SECTION END
    }

    private void createMenu() {
        // Set the Menus
        menuFile     = new JMenu("File");
        menuEdit     = new JMenu("Edit");
        menuFind     = new JMenu("Search");
        menuAbout    = new JMenu("About");
        menuDocument = new JMenu("Document");
        menuTool     = new JMenu("Tool");
        //Font Settings menu

        // Set the Items Menu
        newFile       = new JMenuItem("New", newIcon);
        openFile      = new JMenuItem("Open", openIcon);
        saveFile      = new JMenuItem("Save", saveIcon);
        close         = new JMenuItem("Quit", closeIcon);
        clearFile     = new JMenuItem("Clear", clearIcon);
        quickFind     = new JMenuItem("Quick", searchIcon);
        aboutMe       = new JMenuItem("About Me", aboutMeIcon);
        aboutSoftware = new JMenuItem("About Software", aboutIcon);

        menuBar = new JMenuBar();
        menuBar.add(menuFile);
        menuBar.add(menuEdit);
        menuBar.add(menuFind);
        menuBar.add(menuDocument);
        menuBar.add(menuTool);
        menuBar.add(menuAbout);

        this.setJMenuBar(menuBar);

        // Set Actions:
        selectAllAction = new SelectAllAction("Select All",
                                              clearIcon,
                                              "Select all text",
                                              new Integer(KeyEvent.VK_A),
                                              textArea);

        this.setJMenuBar(menuBar);

        // New File
        newFile.addActionListener(this);  // Adding an action listener (so we know when it's been clicked).
        newFile.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK)); // Set a keyboard shortcut
        menuFile.add(newFile); // Adding the file menu

        // Open File
        openFile.addActionListener(this);
        openFile.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK));
        menuFile.add(openFile);

        // Save File
        saveFile.addActionListener(this);
        saveFile.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK));
        menuFile.add(saveFile);

        // Close File
        /*
         * Along with our "CTRL+F4" shortcut to close the window, we also have
         * the default closer, as stated at the beginning of this tutorial. this
         * means that we actually have TWO shortcuts to close:
         * 1) the default close operation (example, Alt+F4 on Windows)
         * 2) CTRL+F4, which we are
         * about to define now: (this one will appear in the label).
         */
        close.setAccelerator(
                KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK));
        close.addActionListener(this);
        menuFile.add(close);

        // Select All Text
        selectAll = new JMenuItem(selectAllAction);
        selectAll.setText("Select All");
        selectAll.setIcon(selectAllIcon);
        selectAll.setToolTipText("Select All");
        selectAll.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_A,
                                                        InputEvent.CTRL_MASK));
        menuEdit.add(selectAll);

        // Clear File (Code)
        clearFile.addActionListener(this);
        clearFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_K,
                                                        InputEvent.CTRL_MASK));
        menuEdit.add(clearFile);

        // Cut Text
        cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setIcon(cutIcon);
        cut.setToolTipText("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
                                                  InputEvent.CTRL_MASK));
        menuEdit.add(cut);

        // Copy Text
        copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setIcon(copyIcon);
        copy.setToolTipText("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
                                                   InputEvent.CTRL_MASK));
        menuEdit.add(copy);

        // Paste Text
        paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setIcon(pasteIcon);
        paste.setToolTipText("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
                                                    InputEvent.CTRL_MASK));
        menuEdit.add(paste);

        // Find Word
        quickFind.addActionListener(this);
        quickFind.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,
                                                        InputEvent.CTRL_MASK));
        menuFind.add(quickFind);

        // WordWrap
        wordWrap = new JMenuItem();
        wordWrap.setText("Word Wrap");
        wordWrap.setIcon(wordwrapIcon);
        wordWrap.setToolTipText("Word Wrap");

        //Short cut key or key stroke
        wordWrap.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W,
                                                       InputEvent.CTRL_MASK));
        menuDocument.add(wordWrap);

        /* CODE FOR WORD WRAP OPERATION
         * BY DEFAULT WORD WRAPPING IS ENABLED.
         */
        wordWrap.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent ev) {
                // If wrapping is false then after clicking on menuitem the word wrapping will be enabled
                /* Setting word wrapping to true */
                // else  if wrapping is true then after clicking on menuitem the word wrapping will be disabled
                /* Setting word wrapping to false */
                textArea.setLineWrap(!textArea.getLineWrap());
                updateStatusText();
            }
        });

        menuLineSeparator = new JMenu("Line Separator");
        JMenuItem lineDosWin = new JMenuItem("DOS/Win");
        JMenuItem lineUnix   = new JMenuItem("Unix/Linux");
        menuLineSeparator.add(lineDosWin);
        menuLineSeparator.add(lineUnix);
        lineDosWin.addActionListener(ev -> {
            lineSeparator = LnSep.dos.getVal();
            updateStatusText();
        });
        lineUnix.addActionListener(ev -> {
            lineSeparator = LnSep.unix.getVal();
            updateStatusText();
        });
        menuDocument.add(menuLineSeparator);

        menuReloadCharset = new JMenu("Reload with Charset");
        {
            List<JMenuItem> items = createCharsetItemList();
            for (JMenuItem item : items) {
                item.addActionListener(this::reloadFileActionPerformed);
                menuReloadCharset.add(item);
            }
        }

        menuDocument.add(menuReloadCharset);
        menuCharset = new JMenu("Set file charset");
        {
            List<JMenuItem> items = createCharsetItemList();
            for (JMenuItem item : items) {
                item.addActionListener(this::setFileEncodingActionPerformed);
                menuCharset.add(item);
            }
        }
        menuDocument.add(menuCharset);

        encryptFile = new JMenuItem("Encrypt File");
        encryptFile.addActionListener(this);
        encryptFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,
                                                          InputEvent.CTRL_DOWN_MASK
                                                                  | InputEvent.SHIFT_DOWN_MASK));
        decryptFile = new JMenuItem("Decrypt File");
        decryptFile.addActionListener(this);
        decryptFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D,
                                                          InputEvent.CTRL_DOWN_MASK
                                                                  | InputEvent.SHIFT_DOWN_MASK));
        menuTool.add(encryptFile);
        menuTool.add(decryptFile);
        // About Me
        aboutMe.addActionListener(this);
        aboutMe.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F1, 0));
        menuAbout.add(aboutMe);

        // About Software
        aboutSoftware.addActionListener(this);
        aboutSoftware.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
        menuAbout.add(aboutSoftware);
    }

    private void clear() {
        textArea.setText("");
    }

    private List<JMenuItem> createCharsetItemList() {
        JMenuItem csGB18030     = new JMenuItem("天朝:GB18030");
        JMenuItem csGB2312      = new JMenuItem("天朝·新加坡:GB2312");
        JMenuItem csGBK         = new JMenuItem("天朝:GBK");
        JMenuItem csISO_2022_CN = new JMenuItem("天朝:ISO-2022-CN");

        JMenuItem csBig5       = new JMenuItem("華人城邦:Big5");
        JMenuItem csBig5_HKSCS = new JMenuItem("華人城邦:Big5-HKSCS");

        JMenuItem csUtf8    = new JMenuItem("萬國碼:UTF-8");
        JMenuItem csUtf16   = new JMenuItem("萬國碼:UTF-16");
        JMenuItem csUtf16be = new JMenuItem("萬國碼:UTF-16BE");
        JMenuItem csUtf16le = new JMenuItem("萬國碼:UTF-16LE");

        JMenuItem csISO_8859_1 = new JMenuItem("西欧·美國:ISO-8859-1");
        JMenuItem csWin1252    = new JMenuItem("西欧·美國:Windows-1252");
        JMenuItem csUS_ASCII   = new JMenuItem("美國:US-ASCII");

        JMenuItem csWindows_31j   = new JMenuItem("倭奴·日本:windows-31j");
        JMenuItem csShift_JIS     = new JMenuItem("倭奴·日本:Shift_JIS");
        JMenuItem csISO_2022_JP   = new JMenuItem("倭奴·日本:ISO-2022-JP");
        JMenuItem csISO_2022_JP_2 = new JMenuItem("倭奴·日本:ISO-2022-JP-2");
        JMenuItem csEUC_JP        = new JMenuItem("倭奴·日本:EUC-JP");

        JMenuItem csEUC_KR      = new JMenuItem("辰馬並·三韓:EUC-KR");
        JMenuItem csISO_2022_KR = new JMenuItem("辰馬並·三韓:ISO-2022-KR");

        JMenuItem csKOI8_R  = new JMenuItem("斯拉夫·俄语:KOI8-R");
        JMenuItem csWin1251 = new JMenuItem("斯拉夫·俄语:Windows-1251");

        JMenuItem csISO_8859_2  = new JMenuItem("匈奴·中欧:ISO-8859-2");
        JMenuItem csISO_8859_6  = new JMenuItem("大食·阿拉伯:ISO-8859-6");
        JMenuItem csISO_8859_7  = new JMenuItem("希腊:ISO-8859-7");
        JMenuItem csISO_8859_8  = new JMenuItem("希伯来:ISO-8859-8");
        JMenuItem csISO_8859_9  = new JMenuItem("突厥·土耳其:ISO-8859-9");
        JMenuItem csISO_8859_11 = new JMenuItem("暹罗·泰:ISO-8859-11");

        return Arrays.asList(
                csGB18030, csGB2312, csGBK, csISO_2022_CN, csBig5, csBig5_HKSCS,
                csUtf8, csUtf16, csUtf16be, csUtf16le,
                csISO_8859_1, csWin1252, csUS_ASCII,
                csWindows_31j, csShift_JIS, csISO_2022_JP, csISO_2022_JP_2, csEUC_JP,
                csEUC_KR, csISO_2022_KR, csKOI8_R, csWin1251, csISO_8859_2,
                csISO_8859_6, csISO_8859_7, csISO_8859_8, csISO_8859_9, csISO_8859_11
        );
    }

    @Override
    protected void processWindowEvent(WindowEvent e) {
        if (e.getID() == WindowEvent.WINDOW_CLOSING) {
            if (edit) {
                Object[] options = {"Save and exit", "No Save and exit",
                        "Return"};
                int n = JOptionPane.showOptionDialog(this,
                                                     "Do you want to save the file ?",
                                                     "Question",
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     options,
                                                     options[1]);
                if (n == 0) {// save and exit
                    saveFile();
                    this.dispose();// dispose all resources and close the application
                } else if (n == 1) {// no save and exit
                    this.dispose();// dispose all resources and close the application
                }
            } else {
                System.exit(99);
            }
        }
    }

    // Make the TextArea available to the autocomplete handler
    protected JTextArea getEditor() {
        return textArea;
    }

    // Enable autocomplete option
    public void enableAutoComplete(File file) {
        if (hasListener) {
            textArea.getDocument().removeDocumentListener(autocomplete);
            hasListener = false;
        }

        ArrayList<String> arrayList;
        String[]          list = kw.getSupportedLanguages();

        for (int i = 0; i < list.length; i++) {
            if (file.getName().endsWith(list[i])) {
                switch (i) {
                    case 0: {
                        String[] jk = kw.getJavaKeywords();
                        arrayList    = kw.setKeywords(jk);
                        autocomplete = new AutoComplete(this, arrayList);
                        textArea.getDocument()
                                .addDocumentListener(autocomplete);
                        hasListener = true;
                    }
                    break;
                    case 1: {
                        String[] ck = kw.getCppKeywords();
                        arrayList    = kw.setKeywords(ck);
                        autocomplete = new AutoComplete(this, arrayList);
                        textArea.getDocument()
                                .addDocumentListener(autocomplete);
                        hasListener = true;
                    }
                    break;
                }
            }
        }
    }

    public void setFileEncodingActionPerformed(ActionEvent e) {
        JMenuItem item    = (JMenuItem) e.getSource();
        String    text    = item.getText();
        String[]  split   = text.split(":");

        String newFileEncoding;
        if (split.length > 1) {
            newFileEncoding = split[1];
        } else {
            newFileEncoding = text;
        }
        this.fileEncoding = newFileEncoding;
        updateStatusText();
    }

    public void reloadFileActionPerformed(ActionEvent e) {
        JMenuItem item    = (JMenuItem) e.getSource();
        String    text    = item.getText();
        String[]  split   = text.split(":");
        String    content = textArea.getText();

        String newFileEncoding;
        if (split.length > 1) {
            newFileEncoding = split[1];
        } else {
            newFileEncoding = text;
        }
        try {
            byte[] bytes;
            if (!isEmpty(this.currentFile)) {
                bytes = Files.readAllBytes(Paths.get(currentFile));
            } else if (isEmpty(this.fileEncoding)) {
                bytes = content.getBytes(Charset.defaultCharset());
            } else {
                 bytes = content.getBytes(fileEncoding);

            }
            String s     = new String(bytes, newFileEncoding);
            SwingUtilities.invokeLater(() -> {
                textArea.setText(s);
                fileEncoding = newFileEncoding;
                edit=true;
                updateStatusText();
            });
        } catch (Exception ex) {
            System.err.println(ex.getMessage());
        }
    }


    public void actionPerformed(ActionEvent e) {
        // If the source of the event was our "close" option
        if (e.getSource() == close || e.getSource() == closeButton) {
            closeEditor();
        } // If the source was the "new" file option
        else if (e.getSource() == newFile || e.getSource() == newButton) {
            newFile();
        } // If the source was the "open" option
        else if (e.getSource() == openFile || e.getSource() == openButton) {
            openFile();
        } // If the source of the event was the "save" option
        else if (e.getSource() == saveFile || e.getSource() == saveButton) {
            saveFile();
        }// If the source of the event was the "Bold" button
//        else if (e.getSource() == boldButton) {
//            if (textArea.getFont().getStyle() == Font.BOLD) {
//                textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN));
//            } else {
//                textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
//            }
//        }// If the source of the event was the "Italic" button
//        else if (e.getSource() == italicButton) {
//            if (textArea.getFont().getStyle() == Font.ITALIC) {
//                textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN));
//            } else {
//                textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
//            }
//        }
        // Clear File (Code)
        if (e.getSource() == clearFile || e.getSource() == clearButton) {
            clearText();
        }
        // Find
        if (e.getSource() == quickFind || e.getSource() == quickButton) {
            new Find(textArea);
        } // About Me
        else if (e.getSource() == aboutMe || e.getSource() == aboutMeButton) {
            new About(this).me();
        } // About Software
        else if (e.getSource() == aboutSoftware
                || e.getSource() == aboutButton) {
            new About(this).software();
        } else if (e.getSource() == encryptFile) {
            String text = textArea.getText();
            if (isEmpty(text)) { return; }
            String pass = readPassword("請輸入密碼:");
            if (isEmpty(pass)) {
                JOptionPane.showMessageDialog(this,
                                              "沒有密碼，取消加密。",
                                              "錯誤",
                                              ERROR_MESSAGE);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                byte[] encrypt = new byte[0];
                try {

                    encrypt = PBEUtils.encrypt(text.getBytes(StandardCharsets.UTF_8),
                                               pass);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                                                  "加密錯誤",
                                                  "錯誤",
                                                  ERROR_MESSAGE);
                    return;
                }

                Encoder mimeEncoder = Base64.getMimeEncoder();
                byte[]  z           = GzipUtils.zip(encrypt);
                String  s           = mimeEncoder.encodeToString(z);
                textArea.setText(s);
                textArea.setCaretPosition(s.length());
                updateStatusText();
            });

        } else if (e.getSource() == decryptFile) {
            String text = textArea.getText();
            if (isEmpty(text)) { return; }
            String pass = readPassword("請輸入密碼:");
            if (isEmpty(pass)) {
                JOptionPane.showMessageDialog(this,
                                              "沒有密碼，取消解密密。",
                                              "錯誤",
                                              ERROR_MESSAGE);
                return;
            }
            SwingUtilities.invokeLater(() -> {
                Decoder mimeDecoder = Base64.getMimeDecoder();
                byte[] bytes = mimeDecoder.decode(text.getBytes(StandardCharsets.UTF_8));
                byte[] z       = GzipUtils.unzip(bytes);
                byte[] decrypt = new byte[0];
                try {
                    decrypt = PBEUtils.decrypt(z, pass);
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                                                  "解密錯誤",
                                                  "錯誤",
                                                  ERROR_MESSAGE);
                    return;
                }
                textArea.setText(new String(decrypt, StandardCharsets.UTF_8));
                updateStatusText();
            });
        }
    }

    private void closeEditor() {
        if (edit) {
            Object[] options = {"Save and exit", "No Save and exit", "Return"};
            int n = JOptionPane.showOptionDialog(this,
                                                 "Do you want to save the file ?",
                                                 "Question",
                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 options,
                                                 options[2]);
            if (n == 0) {// save and exit
                saveFile();
                this.dispose();// dispose all resources and close the application
            } else if (n == 1) {// no save and exit
                this.dispose();// dispose all resources and close the application
            }
        } else {
            this.dispose();// dispose all resources and close the application
        }
        isClose = true;
    }

    private void clearText() {
        Object[] options = {"Yes", "No"};
        int n = JOptionPane.showOptionDialog(this,
                                             "Are you sure to clear the text Area ?",
                                             "Question",
                                             JOptionPane.YES_NO_CANCEL_OPTION,
                                             JOptionPane.QUESTION_MESSAGE,
                                             null,
                                             options,
                                             options[1]);
        if (n == 0) {// clear
            clear();
        }
    }

    private void newFile() {
        if (edit) {
            Object[] options = {"Save", "No Save", "Return"};
            int n = JOptionPane.showOptionDialog(this,
                                                 "Do you want to save the file at first ?",
                                                 "Question",
                                                 JOptionPane.YES_NO_CANCEL_OPTION,
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 options,
                                                 options[2]);
            if (n == 0) {// save
                saveFile();
                edit = false;
            } else if (n == 1) {
                edit = false;
                clear();
            }
        } else {
            clear();
        }
    }

    public void openFile(File openFile, Charset charset) {
        if (!openFile.exists()) {
            try {
                openFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        currentFile = openFile.getAbsolutePath();
        if (charset != null) {
            // todo: detect file encoding
            fileEncoding = charset.name();
        } else {
            fileEncoding = Charset.defaultCharset().name();
        }
        try {
            clear(); // clear the TextArea before applying the file contents
            setTitle(openFile.getName() + " | " + SimpleJavaTextEditor.NAME);
            InputStream       in     = Files.newInputStream(openFile.toPath());
            Charset           cs     = Charset.forName(fileEncoding);
            InputStreamReader reader = new InputStreamReader(in, cs);
            Scanner scan = new Scanner(reader);
            while (scan.hasNext()) {
                textArea.append(scan.nextLine() + "\n");
            }
            curDir = openFile.getParentFile();
            enableAutoComplete(openFile);
        } catch (Exception ex) { // catch any exceptions, and...
            // ...write to the debug console
            System.err.println(ex.getMessage());
        }
    }

    public void openFile(String file, Charset charset) {
        File openFile = new File(file);
        openFile(openFile, charset);
    }

    private void openFile() {
        JFileChooser open = new JFileChooser(); // open up a file chooser (a dialog for the user to  browse files to open)
        if (!(textArea.getText().equals(""))) {
            saveFile();
        }
        // if true does normal operation
        int option = open.showOpenDialog(this); // get the option that the user selected (approve or cancel)

        /*
         * NOTE: because we are OPENing a file, we call showOpenDialog~ if
         * the user clicked OK, we have "APPROVE_OPTION" so we want to open
         * the file
         */
        if (option == JFileChooser.APPROVE_OPTION) {
            File openFile = open.getSelectedFile();
            openFile(openFile, null);
        }
    }


    private void saveFile() {
        if (currentFile != null) {
            try {
                BufferedWriter out;
                Path           path = Paths.get(currentFile);
                if (fileEncoding == null) {
                    out = Files.newBufferedWriter(path,
                                                  Charset.defaultCharset());
                } else {
                    out = Files.newBufferedWriter(path,
                                                  Charset.forName(fileEncoding));
                }
                String   text  = textArea.getText();
                String[] split = text.split("\\n");
                for (String s : split) {
                    out.write(s);
                    out.write(lineSeparator);
                }
                out.close();

                enableAutoComplete(path.toFile());
                edit = false;
            } catch (Exception ex) { // again, catch any exceptions and...
                // ...write to the debug console
                System.err.println(ex.getMessage());
            }

        } else {
            // Open a file chooser
            JFileChooser fileChoose;
            if (curDir == null) {
                fileChoose = new JFileChooser();
            } else {
                fileChoose = new JFileChooser(curDir);
            }
            // Open the file, only this time we call
            int option = fileChoose.showSaveDialog(this);

            /*
             * ShowSaveDialog instead of showOpenDialog if the user clicked OK
             * (and not cancel)
             */
            if (option == JFileChooser.APPROVE_OPTION) {
                try {
                    File file = fileChoose.getSelectedFile();
                    curDir = file.getParentFile();
                    setTitle(
                            file.getName() + " | " + SimpleJavaTextEditor.NAME);
                    BufferedWriter out;
                    if (fileEncoding == null) {
                        out = Files.newBufferedWriter(file.toPath(),
                                                      Charset.defaultCharset());
                    } else {
                        out = Files.newBufferedWriter(file.toPath(),
                                                      Charset.forName(
                                                              fileEncoding));
                    }
                    String   text  = textArea.getText();
                    String[] split = text.split("\\n");
                    for (String s : split) {
                        out.write(text);
                        out.write(lineSeparator);
                    }

                    out.close();

                    enableAutoComplete(file);
                    edit = false;
                } catch (Exception ex) { // again, catch any exceptions and...
                    // ...write to the debug console
                    System.err.println(ex.getMessage());
                }
            }
        }
    }

    public void setEncoding(String charset) {
        if (!isEmpty(charset)) { this.fileEncoding = charset; }
    }

    public String getFileEncoding() {
        return this.fileEncoding;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    class SelectAllAction extends AbstractAction {

        /**
         * Used for Select All function
         */
        private static final long serialVersionUID = 1L;

        public SelectAllAction(String text,
                               ImageIcon icon,
                               String desc,
                               Integer mnemonic,
                               final JTextArea textArea) {
            super(text, icon);
            putValue(SHORT_DESCRIPTION, desc);
            putValue(MNEMONIC_KEY, mnemonic);
        }

        public void actionPerformed(ActionEvent e) {
            textArea.selectAll();
        }
    }

    DropTargetListener dropTargetListener = new DropTargetListener() {

        @Override
        public void dragEnter(DropTargetDragEvent e) {
        }

        @Override
        public void dragExit(DropTargetEvent e) {
        }

        @Override
        public void dragOver(DropTargetDragEvent e) {
        }

        @Override
        public void drop(DropTargetDropEvent e) {
            if (edit) {
                Object[] options = {"Save", "No Save", "Return"};
                int n = JOptionPane.showOptionDialog(UI.this,
                                                     "Do you want to save the file at first ?",
                                                     "Question",
                                                     JOptionPane.YES_NO_CANCEL_OPTION,
                                                     JOptionPane.QUESTION_MESSAGE,
                                                     null,
                                                     options,
                                                     options[2]);
                if (n == 0) {// save
                    UI.this.saveFile();
                    edit = false;
                } else if (n == 1) {
                    edit = false;
                    clear();
                } else if (n == 2) {
                    e.rejectDrop();
                    return;
                }
            }
            try {
                Transferable tr      = e.getTransferable();
                DataFlavor[] flavors = tr.getTransferDataFlavors();
                for (DataFlavor flavor : flavors) {
                    if (flavor.isFlavorJavaFileListType()) {
                        e.acceptDrop(e.getDropAction());

                        try {
                            String fileName = tr.getTransferData(flavor)
                                                .toString()
                                                .replace("[", "")
                                                .replace("]", "");

                            // Allowed file filter extentions for drag and drop
                            boolean extensionAllowed = false;
                            for (String s : dragDropExtensionFilter) {
                                if (fileName.endsWith(s)) {
                                    extensionAllowed = true;
                                    break;
                                }
                            }
                            if (!extensionAllowed) {
                                JOptionPane.showMessageDialog(UI.this,
                                                              "This file is not allowed for drag & drop",
                                                              "Error",
                                                              ERROR_MESSAGE);

                            } else {
                                FileInputStream fis = new FileInputStream(new File(
                                        fileName));
                                byte[] ba = new byte[fis.available()];
                                fis.read(ba);
                                textArea.setText(new String(ba));
                                fis.close();
                            }
                        } catch (Exception ex) {
                            ex.printStackTrace();
                        }
                        e.dropComplete(true);
                        return;
                    }
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
            e.rejectDrop();
        }

        @Override
        public void dropActionChanged(DropTargetDragEvent e) {
        }
    };

}
