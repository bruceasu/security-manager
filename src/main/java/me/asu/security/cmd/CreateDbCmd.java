package me.asu.security.cmd;

import static me.asu.security.ErrorCode.*;
import static me.asu.security.db.SecurityDb.CURRENT_VERSION;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.io.Console;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.TreeMap;
import me.asu.security.db.SecurityDb;
import me.asu.security.util.GetOpt;

public class CreateDbCmd implements Command {
    public static  String DEFAULT_PATH = Paths
            .get(System.getProperty("user.home"), ".security-db", "s.db")
            .toString();
    String              name        = "create";
    String              optString   = "hd:i:l:n:p:w:";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-d", "數據庫路徑。");
        description.put("-i", "數據庫描述。");
        description.put("-w", "數據庫密碼。");
        description.put("-l", "隨機字符串字母集。默認： a-zA-Z");
        description.put("-n", "隨機字符串數字集。默認： 0-9");
        description.put("-p", "隨機字符串符號集。默認： ~!@#$%^&*()_+-=[]{}");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "創建一個新的數據庫。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt    = new GetOpt(args, optString);
        String dbPath = null;
        String dbDesc = null;
        String dbRL   = null;
        String dbRN   = null;
        String dbRP   = null;
        String dbPassword   = null;
        int    c;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'h':
                    opt.printUsage(name, this.description);
                    System.exit(0);
                    break;
                case 'd':
                    dbPath = opt.getOptionArg();
                    break;
                case 'i':
                    dbDesc = opt.getOptionArg();
                    break;
                case 'l':
                    dbRL = opt.getOptionArg();
                    break;
                case 'n':
                    dbRN = opt.getOptionArg();
                    break;
                case 'p':
                    dbRP = opt.getOptionArg();
                    break;
                case 'w':
                    dbPassword = opt.getOptionArg();
                    break;
            }
        }
        String[] cmdArgs = opt.getCmdArgs();
        if (isEmpty(dbPath)) {
            dbPath = DEFAULT_PATH;
        }

        if (Files.exists(Paths.get(dbPath))) {
            System.err.println(dbPath + " 已經存在。");
            System.exit(DB_EXISTS_ERROR);
        }
        if (isEmpty(dbPassword)) {
            System.err.println("數據庫必須有密碼");
            try {
                dbPassword = readPassword();
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(UNKNOWN_ERROR);
            }
        }
        // 1. fill the db
        SecurityDb db = new SecurityDb();
        db.setPath(dbPath);
        db.setDescription(dbDesc);
        db.setDbPassword(dbPassword);
        db.setVersion(CURRENT_VERSION);

        try {
            // 2. save
            db.store();
            System.out.println("生成數據庫："+db.getPath());
        } catch (Exception e) {
            e.printStackTrace();
            return CREATE_DB_ERROR;
        }
        return OK;
    }


}
