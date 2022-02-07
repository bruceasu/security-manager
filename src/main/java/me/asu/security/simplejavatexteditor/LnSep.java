package me.asu.security.simplejavatexteditor;

public enum LnSep {
        dos("\r\n"),
        unix("\n");
        String ls;
        LnSep(String val) {
            ls = val;
        }

        public static LnSep getByVal(String val) {
            LnSep[] values = values();
            for (LnSep value : values) {
                if (value.ls.equals(val)) { return value; }
            }
            return null;
        }

        public String getVal() {
            return ls;
        }
        public String toString() {
            return name();
        }
    }