package me.asu.security;

import static me.asu.security.ErrorCode.COM_NOT_SUPPORT_ERROR;
import static me.asu.security.ErrorCode.PARAM_ERROR;
import static me.asu.security.util.StringUtils.isEmpty;

import java.util.Map;
import java.util.TreeMap;
import me.asu.security.cmd.*;
import me.asu.security.util.GetOpt;
import me.asu.security.util.StringUtils;

public class Main {

    static Map<String, Command> commandMap = new TreeMap<>();

    static {
        Command[] list = new Command[]{
                new CreateDbCmd(),
                new ShowEntityCmd(),
                new AddEntityCmd(),
                new DeleteEntityCmd(),
                new RandomCmd(),
                new CpValToClipboardCmd(),
                new FileEncryptCmd(),
                new FileDecryptCmd(),
                new EditorCmd(),
        };
        for (Command cmd : list) {
            commandMap.put(cmd.name(), cmd);
        }
    }

    public static void main(String[] args) {
        // 使用 AES 算法的加密
        String cmd = null;
        if (args == null || args.length == 0 ) {
            showUsage(cmd);
            System.exit(PARAM_ERROR);
        } else if ("help".equalsIgnoreCase(args[0])) {
            if (args.length > 1) {
                cmd = args[1];
            }
            showUsage(cmd);
            System.exit(PARAM_ERROR);
        }
        Command command = commandMap.get(args[0]);
        if (command == null) {
            System.out.println("Not support this command: " + command);
            System.exit(COM_NOT_SUPPORT_ERROR);
        }
        String[] subArgs = new String[args.length - 1];
        if (subArgs.length > 0) {
            System.arraycopy(args, 1, subArgs, 0, subArgs.length);
        }

        int ret = command.execute(subArgs);
        System.exit(ret);
    }

    private static void showUsage(String cmd) {
        StringBuilder builder = new StringBuilder();
        if (isEmpty(cmd)) {
            builder.append("用法: <cmd> [option]\n");
            builder.append(String.format("%16s  %s%n", "help", "顯示本幫助。 help <cmd> 顯示命令的幫助。"));

            String padding = StringUtils.dup(' ', 18);
            commandMap.values().forEach(v -> {
                String n    = v.name();
                String d    = v.description();
                String desc = GetOpt.formatUsageDescription(d, padding, 76);
                builder.append(String.format("%16s  %s%n", n, desc));
            });

            System.err.println(builder.toString());
        } else {
            Command command = commandMap.get(cmd);
            if (command == null) {
                System.err.println("未知命令： " + cmd);
            } else {
                command.execute("-h");
            }
        }
    }

}

