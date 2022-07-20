package ivansCode.utils;

public class StringUtils {

    public static String replaceFirst(String wholeStr, String prefix, String replacement){
        int index = wholeStr.indexOf(prefix);
        if (index == 0){
            return replacement + wholeStr.substring(prefix.length());
        } else {
            throw new IllegalArgumentException(String.format("'%s' not a prefix of '%s'", prefix, wholeStr));
        }
    }

    public static String normalizeWhitespace(String original){
        return original.replaceAll("\\s+", " ").trim();
    }

}
