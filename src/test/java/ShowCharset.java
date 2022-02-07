import java.nio.charset.Charset;
import java.util.SortedMap;

public class ShowCharset {

    public static void main(String[] args) {
        SortedMap<String, Charset> m = Charset.availableCharsets();
        for (String s : m.keySet()) {
            System.out.println(s);
        }
    }
}
