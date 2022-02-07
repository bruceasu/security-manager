package me.asu.security.cmd;

import static me.asu.security.ErrorCode.ADD_ENTRY_ERROR;
import static me.asu.security.ErrorCode.OK;
import static me.asu.security.ErrorCode.PARAM_REQUIRED_ERROR;
import static me.asu.security.ErrorCode.UNKNOWN_ERROR;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import me.asu.security.db.Field;
import me.asu.security.db.Item;
import me.asu.security.db.SecurityDb;
import me.asu.security.util.GetOpt;

public class DeleteEntityCmd implements Command {

    String              name        = "delete";
    String              optString   = "hd:p:i:w";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-d", "數據庫路徑。");
        description.put("-p", "數據庫密碼。");
        description.put("-i", "刪除項目ID。");
        description.put("-w", "項目ID支持通配符?和*。");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "刪除項目。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;
        String defaultPath = Paths
                .get(System.getProperty("user.home"), ".security-db", "s.db")
                .toString();
        String dbPath = defaultPath;
        String  dbPass    = null;
        String entry  = null;
        boolean isWildcard = false;
        while ((c = opt.getNextOption()) != -1) {
            switch (c) {
                case 'h':
                    opt.printUsage(name, description);
                    System.exit(0);
                    break;
                case 'd':
                    dbPath = opt.getOptionArg();
                    break;
                case 'p':
                    dbPass = opt.getOptionArg();
                    break;
                case 'i':
                    entry = opt.getOptionArg();
                    break;
                case 'w':
                    isWildcard = true;
                    break;
            }
        }
        String[] cmdArgs = opt.getCmdArgs();
        if (isEmpty(entry)) {
            opt.printUsage(name, description);
            System.exit(PARAM_REQUIRED_ERROR);
        }
        // 1. open the db
        SecurityDb db = new SecurityDb();
        db.setPath(dbPath);
        if (isEmpty(dbPass)) {
            try {
                String dbPassword = readPassword();
                db.setDbPassword(dbPassword);
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(UNKNOWN_ERROR);
            }
        } else {
            db.setDbPassword(dbPass);
        }
        db.load();
        if(isWildcard) {
            // search the items
            Set<Item> items = db.filterItems(entry, null);
            for (Item item : items) {
                db.removeItemById(item.getId());
            }
        } else {
            db.removeItemById(entry);
        }

        try {
            db.store();
        } catch (Exception e) {
            e.printStackTrace();
            return ADD_ENTRY_ERROR;
        }
        return OK;
    }
}
