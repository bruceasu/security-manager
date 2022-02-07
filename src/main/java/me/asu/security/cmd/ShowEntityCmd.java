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
import me.asu.security.util.GetOpt;

public class ShowEntityCmd implements Command {

    String              name        = "show";
    String              optString   = "hd:p:i:t:f:ao";
    Map<String, String> description = new TreeMap<>();

    {
        description.put("-h", "顯示本幫助。");
        description.put("-d", "數據庫路徑。");
        description.put("-p", "數據庫密碼。");
        description.put("-i", "通過項目ID查詢，支持通配符號：?，*。");
        description.put("-t", "通過項目標籤，使用逗號分割多個標籤。");
        description.put("-f", "顯示的項目欄位，使用逗號分割多個欄位。");
        description.put("-a", "顯示所有欄位。次時，-f表示不顯示的欄位。");
        description.put("-o", "僅顯示欄位名稱，不顯示欄位值。");
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public String description() {
        return "顯示項目。";
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
        String  tag       = null;
        boolean allFields = false;
        boolean onlyName  = false;
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
                case 'a':
                    allFields = true;
                    break;
                case 'f':
                    field = opt.getOptionArg();
                    break;
                case 'o':
                    onlyName = true;
                    break;

            }
        }
        String[] cmdArgs = opt.getCmdArgs();
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
        if (isBlank(entry) && isBlank(tag)) {
            System.out.println("Forbid to show all entries.");
            return PARAM_REQUIRED_ERROR;
        }
        Set<Item> items = db.filterItems(entry, tag);
        if (items.isEmpty()) { return OK; }
        // 3. show the fields
        Set<String> set = new HashSet<>();
        if (!isBlank(field)) {
            String[] split = field.split(",");
            set.addAll(Arrays.asList(split));
        }
        if (allFields) {
            // exclude
            if (onlyName) {
                for (Item item : items) {
                    printTags(item);
                    List<Field> list = filterFields(item, set, true);
                    printFieldsName(list);
                }
            } else {
                for (Item item : items) {
                    printTags(item);
                    List<Field> list = filterFields(item, set, true);
                    printFields(list);
                }
            }

        } else if (!isBlank(field)) {
            // include
            if (onlyName) {
                for (Item item : items) {
                    printTags(item);
                    List<Field> list = filterFields(item, set, false);
                    printFieldsName(list);
                }
            } else {
                for (Item item : items) {
                    printTags(item);
                    List<Field> list = filterFields(item, set, false);
                    printFields(list);
                }
            }
        } else {
            // only show id and tags of items
        }
        return OK;
    }

    private List<Field> filterFields(Item item, Set<String> set,
                                     boolean exclude) {
        Map<String, Field> fields = item.getFields();
        List<Field>        list   = new ArrayList<>();
        for (Field f : fields.values()) {
            boolean contains = set.contains(f.getName());
            if (exclude) {
                if (!contains) { list.add(f);}
            } else {
                if (contains) { list.add(f); }

            }
        }
        return list;
    }

    private void printFields(List<Field> list) {
        for (Field f : list) {
            System.out.printf("%20s: %s%n", f.getName(), f.getContent());
        }
        System.out.println("========================================");
    }

    private void printFieldsName(List<Field> list) {
        for (Field f : list) {
            System.out.printf("%20s: %n", f.getName());
        }
        System.out.println("========================================");
    }

    private void printTags(Item item) {
        String      id   = item.getId();
        Set<String> tags = item.getTags();
        System.out.printf(id);
        System.out.printf(dup(' ', 4));
        for (String s : tags) {
            System.out.printf(":%s", s);
        }
        System.out.println(":");
        System.out.println("----------------------------------------");
    }


}
