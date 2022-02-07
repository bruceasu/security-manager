package me.asu.security.simplejavatexteditor;

import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;

public class PasswordPanel {

    public static String readPassword(String prompt) {
        JPanel         panel = new JPanel();
        JLabel         label = new JLabel(prompt);
        JPasswordField pass  = new JPasswordField(16);
        panel.add(label);
        panel.add(pass);
        String[] options = new String[]{"OK", "Cancel"};
        int option = JOptionPane.showOptionDialog(null,
                                                  panel,
                                                  "Password",
                                                  JOptionPane.NO_OPTION,
                                                  JOptionPane.PLAIN_MESSAGE,
                                                  null,
                                                  options,
                                                  options[0]);
        if (option == 0) { // pressing OK button
            char[] password = pass.getPassword();
            return new String(password);
        }
        return "";
    }
}
    