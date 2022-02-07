package me.asu.security.cmd;

import static me.asu.security.ErrorCode.*;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Map;
import java.util.TreeMap;
import me.asu.security.util.AutoClipboard;
import me.asu.security.util.GetOpt;
import me.asu.security.util.PBEUtils;

public class FileDecryptCmd implements Command {

    String              name        = "decrypt";
    String              optString   = "hi:o:p:ce:";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-i", "輸入文件名，不爲空。");
        description.put("-o", "輸入文件名，與-c至少有1個。同時出現時，-o優先。");
        description.put("-c", "輸出到剪切板，與-o至少有1個。同時出現時，忽略此參數");
        description.put("-e", "內容字符集，默認爲UTF-8");
        description.put("-p", "解密密碼");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "解密文本。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;

        String  pass          = null;
        String  input         = null;
        String  output        = null;
        String  encoding      = "UTF-8";
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
                    output = opt.getOptionArg();
                    break;
                case 'c':
                    isToClipboard = true;
                    break;
                case 'p':
                    pass = opt.getOptionArg();
                    break;
                case 'e':
                    encoding = opt.getOptionArg();
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
            opt.printUsage(name, description);
            System.exit(PARAM_REQUIRED_ERROR);
        } else {
            Path path = Paths.get(input);
            if (Files.isRegularFile(path)) {
                try {
                    source = Files.readAllBytes(path);
                    Decoder mimeDecoder = Base64.getMimeDecoder();
                    source = mimeDecoder.decode(source);
                } catch (IOException e) {
                    e.printStackTrace();
                    System.err.println("無法讀取文件。");
                    System.exit(IO_ERROR);
                }
            } else {
                System.err.println("文件沒有找到：" + input);
                System.exit(FILE_NOT_FOUND_ERROR);
            }
        }

        byte[] bytes = new byte[0];
        try {
            bytes = PBEUtils.decrypt(source, pass);
        } catch (Exception e) {
            System.err.println("加密錯誤。");
            e.printStackTrace();
            System.exit(ENCRYPT_ERROR);
        }

        if (!isEmpty(output)) {
            writeToFile(output, bytes);
            System.out.println("加密內容保存在： " + output);
        } else if (isToClipboard) {
            try {
                AutoClipboard.setSysClipboardText(new String(bytes, encoding));
                System.out.println("解密內容保存在剪切板。");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                AutoClipboard.setSysClipboardText(e.getMessage());
            }

        } else {
            opt.printUsage(name, description);
            System.exit(PARAM_REQUIRED_ERROR);
        }
        return OK;
    }

    private void writeToFile(String output,
                             byte[] data) {
        try {
            Path path   = Paths.get(output);
            Path parent = path.getParent();
            if (!Files.isDirectory(path)) {
                Files.createDirectories(parent);
            }
            Files.write(path, data);
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(IO_ERROR);
        }
    }

}
