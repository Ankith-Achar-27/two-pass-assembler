import java.util.HashMap;
import java.util.Map;

public class OpcodeTable {

    private static final Map<String, String> OPTAB = new HashMap<>();

    static {
        OPTAB.put("LDA", "00");
        OPTAB.put("ADD", "18");
        OPTAB.put("STA", "0C");
        OPTAB.put("RSUB", "4C");
    }

    public static boolean contains(String mnemonic) {
        return OPTAB.containsKey(mnemonic);
    }

    public static String getOpcode(String mnemonic) {
        return OPTAB.get(mnemonic);
    }
}