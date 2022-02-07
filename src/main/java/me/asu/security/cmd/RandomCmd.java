package me.asu.security.cmd;

import static me.asu.security.ErrorCode.OK;
import static me.asu.security.ErrorCode.UNKNOWN_ERROR;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import me.asu.security.util.GetOpt;

public class RandomCmd implements Command {

    public static final byte[] DEFAULT_LETTERS     = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
            .getBytes();
    public static final byte[] DEFAULT_NUMBERS     = "1234567890".getBytes();
    public static final byte[] DEFAULT_PUNCTUATION = "~!@#$%^&*()_+-=[]{}".getBytes();

    private static final Random r = new Random();

    String              name        = "random";
    String              optString   = "hs:lnpa";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-s", "長度。");
        description.put("-l", "使用字母字符集");
        description.put("-n", "使用數字字符集");
        description.put("-p", "使用符號字符集");
        description.put("-a", "使用全部字符集，等同 -lnp。默認使用全部。");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "生成隨機密碼。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;

        String  dbPass         = null;
        int     size           = 16;
        boolean useLetter      = false;
        boolean useNumber      = false;
        boolean usePunctuation = false;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'h':
                    opt.printUsage(name, description);
                    System.exit(0);
                    break;
                case 's':
                    size = Integer.parseInt(opt.getOptionArg());
                    break;
                case 'l':
                    useLetter = true;
                    break;
                case 'n':
                    useNumber = true;
                    break;
                case 'p':
                    usePunctuation = true;
                    break;
                case 'a':
                    useLetter = true;
                    useNumber = true;
                    usePunctuation = true;
                    break;
            }
        }
        String[] cmdArgs = opt.getCmdArgs();
        if (!(useLetter || useNumber || usePunctuation)) {
            useLetter      = true;
            useNumber      = true;
            usePunctuation = true;
        }
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            if (useLetter) {
                out.write(DEFAULT_LETTERS);
            }
            if (useNumber) {
                out.write(DEFAULT_NUMBERS);
            }
            if (usePunctuation) {
                out.write(DEFAULT_PUNCTUATION);
            }
            byte[] bytes = out.toByteArray();
            out.reset();
            for (int i = 0; i < size; i++) {
                out.write(next(bytes));
            }
            String result = out.toString();
            System.out.println(result);
        } catch (IOException e) {
            e.printStackTrace();
            return UNKNOWN_ERROR;
        }
        return OK;
    }

    public byte next(byte[] src) {
        return src[Math.abs(r.nextInt(src.length))];
    }

}
