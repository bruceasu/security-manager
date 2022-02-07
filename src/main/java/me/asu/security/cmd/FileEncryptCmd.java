package me.asu.security.cmd;

import static me.asu.security.ErrorCode.*;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;
import me.asu.security.util.AutoClipboard;
import me.asu.security.util.GetOpt;
import me.asu.security.util.PBEUtils;

public class FileEncryptCmd implements Command {

    String              name        = "encrypt";
    String              optString   = "hi:o:p:c";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-i", "輸入文件名，如果爲空，則最後的文本爲輸入。");
        description.put("-o", "輸入文件名，優先於-c參數。如果爲空且沒有-c參數時，則輸出到終端");
        description.put("-c", "輸出到剪切板。同時出先-o，忽略此參數。");
        description.put("-p", "加密密碼。");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "加密文本。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;

        String  pass         = null;
        String  input         = null;
        String  output         = null;
        boolean isToClipboard = false;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'h':
                    opt.printUsage(name, description);
                    System.exit(OK);
                    break;
                case 'i':
                    input = opt.getOptionArg();
                    break;
                case 'o':
                    output =  opt .getOptionArg();
                    break;
                case 'c':
                    isToClipboard = true;
                    break;
                case 'p':
                    pass = opt.getOptionArg();
                    break;
            }
        }
        String[] cmdArgs = opt.getCmdArgs();
        if (isEmpty(pass)) {
            try {
                pass = readPassword();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(UNKNOWN_ERROR);
            }
        } else {

        }

        byte[] source = null;
        if (isEmpty(input)) {
            if (cmdArgs == null || cmdArgs.length == 0) {
                opt.printUsage(name, description);
                System.exit(PARAM_REQUIRED_ERROR);
            } else {
                source = cmdArgs[0].getBytes(StandardCharsets.UTF_8);
            }
        } else {
            Path path = Paths.get(input);
            if (Files.isRegularFile(path)) {
                try {
                    source = Files.readAllBytes(path);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("無法讀取文件。");
                    System.exit(IO_ERROR);
                }
            } else {
                System.err.println("文件沒有找到："+ input);
                System.exit(FILE_NOT_FOUND_ERROR);
            }
        }

        byte[] encrypt = new byte[0];
        try {
            encrypt = PBEUtils.encrypt(source, pass);
        } catch (Exception e) {
            System.err.println("加密錯誤。");
            e.printStackTrace();
            System.exit(ENCRYPT_ERROR);
        }

        Encoder mimeEncoder = Base64.getMimeEncoder();
        if (isEmpty(output)) {
            String s = mimeEncoder.encodeToString(encrypt);
            if(isToClipboard) {
                AutoClipboard.setSysClipboardText(s);
                System.out.println("加密內容保存在剪切板。");
            } else {
                // output to console
                System.out.println(s);
            }
        } else {
            writeToFile(output, encrypt, mimeEncoder);
            System.out.println("加密內容保存在： " + output);
        }
        return OK;
    }

    private void writeToFile(String output, byte[] encrypt, Encoder mimeEncoder) {
        try {
            Path path   = Paths.get(output);
            Path parent = path.getParent();
            if (!Files.isDirectory(path)) {
                Files.createDirectories(parent);
            }
            byte[] data = mimeEncoder.encode(encrypt);
            Files.write(path, data);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(IO_ERROR);
        }
    }

}
