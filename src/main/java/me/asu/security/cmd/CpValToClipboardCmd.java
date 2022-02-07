package me.asu.security.cmd;

import static me.asu.security.ErrorCode.OK;
import static me.asu.security.ErrorCode.PARAM_REQUIRED_ERROR;
import static me.asu.security.ErrorCode.UNKNOWN_ERROR;
import static me.asu.security.util.StringUtils.dup;
import static me.asu.security.util.StringUtils.isBlank;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.nio.file.Paths;
import java.util.*;
import me.asu.security.db.Field;
import me.asu.security.db.Item;
import me.asu.security.db.SecurityDb;
import me.asu.security.util.AutoClipboard;
import me.asu.security.util.GetOpt;
import me.asu.security.util.PBEUtils;

public class CpValToClipboardCmd implements Command {

    String              name        = "clip";
    String              optString   = "hd:p:i:f:e";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-d", "數據庫路徑。");
        description.put("-p", "數據庫密碼。");
        description.put("-i", "通過項目ID查詢，支持通配符號：?，*。");
        description.put("-f", "顯示的項目欄位");
        description.put("-e", "加密欄位值");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "複製欄位值到剪切板。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;
        String defaultPath = Paths
                .get(System.getProperty("user.home"), ".security-db", "s.db")
                .toString();
        String  dbPath    = defaultPath;
        String  dbPass    = null;
        String  entry     = null;
        String  field     = null;
        boolean isEncrypt = false;
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
                case 'f':
                    field = opt.getOptionArg();
                    break;
                case 'e':
                    isEncrypt = true;
                    break;
            }
        }
        String[] cmdArgs = opt.getCmdArgs();
        if (isBlank(field)) {
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
        // 2. search the items
        Set<Item> items = db.filterItems(entry, null);
        if (items.isEmpty()) { return OK; }
        // 3. show the fields

        for (Item item : items) {
            List<Field> list = filterFields(item, field);
            // copy to clipboard
            if (!list.isEmpty()) {
                Field f = list.get(0);
                String content = f.getContent();
                if (isEncrypt) {
                    try {
                        content = PBEUtils.decryptString(content, db.getDbPassword());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                AutoClipboard.setSysClipboardText(content);
            }
        }
        return OK;
    }

    private List<Field> filterFields(Item item, String field) {
        Map<String, Field> fields = item.getFields();
        List<Field>        list   = new ArrayList<>();
        for (Field f : fields.values()) {
            if (f.getName().equals(field)) list.add(f);
        }
        return list;
    }

}
