package ivansCode.utils;

import java.util.regex.Pattern;

public final class StringUtils {

    private StringUtils(){}

    public static String normalizeWhitespace(String original){
        return original.replaceAll("\\s+", " ").trim();
    }

    public static boolean containsOnly(String target, char symbol, int atLeast){

        String result = target.replaceAll(Pattern.quote(String.valueOf(symbol)), "");
        return result.isEmpty() && target.length() >= atLeast;

    }

}
