package me.asu.security.cmd;

import static me.asu.security.ErrorCode.ADD_ENTRY_ERROR;
import static me.asu.security.ErrorCode.OK;
import static me.asu.security.ErrorCode.PARAM_REQUIRED_ERROR;
import static me.asu.security.ErrorCode.UNKNOWN_ERROR;
import static me.asu.security.util.StringUtils.isEmpty;
import static me.asu.security.util.StringUtils.readPassword;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import me.asu.security.db.Field;
import me.asu.security.db.Item;
import me.asu.security.db.SecurityDb;
import me.asu.security.util.GetOpt;
import me.asu.security.util.PBEUtils;

public class AddEntityCmd implements Command {

    String              name        = "add";
    String              optString   = "hd:p:i:t:f:e";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-d", "數據庫路徑。");
        description.put("-p", "數據庫密碼。");
        description.put("-i", "項目ID");
        description.put("-t", "項目標籤，刪除標籤以-（減號）結尾，使用逗號分割多個標籤。例如：a,b,c-,d-");
        description.put("-f",
                "項目欄位和值, 多個值使用分號分割，如果刪除欄位，設置值爲空。 ex. field1=value1;field2=value2;deleteField=");
        description.put("-e", "加密欄位值");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "增加項目。";
    }

    @Override
    public int execute(String[] args) {
        GetOpt opt = new GetOpt(args, optString);
        int    c;
        String defaultPath = Paths
                .get(System.getProperty("user.home"), ".security-db", "s.db")
                .toString();
        String  dbPath  = defaultPath;
        String  dbPass  = null;
        String  entry   = null;
        String  field   = null;
        String  tag     = null;
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
                case 't':
                    tag = opt.getOptionArg();
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
        // search the items
        Item item = db.getItemById(entry);
        if (item == null) {
            // create
            item = new Item();
            item.setId(entry);
            db.addItem(item);
        }

        if (!isEmpty(tag)) {
            String[]     split   = tag.split(",");
            List<String> addTags = new ArrayList<>();
            List<String> rmTags  = new ArrayList<>();
            for (String s : split) {
                if (s.endsWith("-")) {
                    rmTags.add(s.substring(0, s.length() - 1));
                } else {
                    addTags.add(s);
                }
            }
            item.addTags(addTags);
            item.removeTags(rmTags);
        }
        if (!isEmpty(field)) {
            String[] split = field.split(";");
            for (String s : split) {
                String[] kv = s.split("=", 2);
                Field    f  = new Field();
                f.setName(kv[0]);
                if (kv.length == 2) {
                    if (isEmpty(kv[1])) {
                        item.removeField(kv[0]);
                    } else {
                        if (isEncrypt) {
                            try {
                                f.setContent(PBEUtils.encryptString(kv[1], db.getDbPassword()));
                            } catch (Exception e) {
                                e.printStackTrace();
                                return ADD_ENTRY_ERROR;
                            }
                        } else {
                            f.setContent(kv[1]);
                        }
                        item.addFiled(f);
                    }
                } else {
                    item.addFiled(f);
                }
            }
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
