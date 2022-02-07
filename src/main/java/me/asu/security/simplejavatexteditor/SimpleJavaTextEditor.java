/**
 * @name        Simple Java NotePad
 * @package     ph.notepad
 * @file        UI.java
 * @author      SORIA Pierre-Henry
 * @email       pierrehs@hotmail.com
 * @link        http://github.com/pH-7
 * @copyright   Copyright Pierre-Henry SORIA, All Rights Reserved.
 * @license     Apache (http://www.apache.org/licenses/LICENSE-2.0)
 * @create      2012-05-04
 * @update      2016-21-03
 *
 *
 * @modifiedby  Achintha Gunasekara
 * @modweb      http://www.achinthagunasekara.com
 * @modemail    contact@achinthagunasekara.com
 */

package me.asu.security.simplejavatexteditor;

import static me.asu.security.util.StringUtils.isEmpty;

import java.nio.charset.Charset;
import javax.swing.JTextPane;
import me.asu.security.util.GUITools;
import me.asu.security.util.GetOpt;

public class SimpleJavaTextEditor extends JTextPane {

    private static final long serialVersionUID = 1L;
    public final static String AUTHOR_EMAIL = "hi@ph7.me";
    public final static String NAME = "PHNotePad";
    public final static String EDITOR_EMAIL = "bruceasu@bruceasu.com";
    public final static double VERSION = 3.1;


    /**
     * @param args
     */
    public static void main(String[] args) {
        String optString = "e:";
        GetOpt opt       = new GetOpt(args, optString);
        int    c;
        String encoding  = null;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'e':
                    encoding = opt.getOptionArg();
                    break;
            }
        }

        String[] cmdArgs = opt.getCmdArgs();
        GUITools.initLookAndFeel();
        UI ui = new UI();
        if (cmdArgs != null && cmdArgs.length > 0) {
            String file = cmdArgs[0];
            if (isEmpty(encoding)) {
                ui.openFile(file, null);
            } else {
                ui.openFile(file, Charset.forName(encoding));
            }
        }
        ui.setVisible(true);
        do {
            try {
                Thread.sleep(2000L);
            } catch (InterruptedException e) {
                break;
            }
        } while(!ui.isClose());
    }


}
