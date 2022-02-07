package me.asu.security.cmd;

import static me.asu.security.ErrorCode.OK;
import static me.asu.security.ErrorCode.UNKNOWN_ERROR;
import static me.asu.security.util.StringUtils.isEmpty;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import me.asu.security.simplejavatexteditor.SimpleJavaTextEditor;
import me.asu.security.util.GetOpt;
import sun.java2d.pipe.SpanShapeRenderer.Simple;

public class EditorCmd implements Command {
    String              name        = "edit";
    String              optString   = "he:";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-e", "文件編碼");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "編輯器。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;
        String encoding = null;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'h':
                    opt.printUsage(name, description);
                    System.exit(0);
                    break;
                case 'e':
                    encoding = opt.getOptionArg();
                    break;
            }
        }

        String[] cmdArgs = opt.getCmdArgs();
        if (cmdArgs != null && cmdArgs.length > 0) {
            String file = cmdArgs[0];
            if (isEmpty(encoding)) {
                SimpleJavaTextEditor.main(new String[]{file});
            } else {
                SimpleJavaTextEditor.main(new String[]{"-e", encoding, file});
            }
        } else {
            SimpleJavaTextEditor.main(new String[0]);
        }

        return OK;
    }

}
