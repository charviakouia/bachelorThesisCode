package ivansCode.utils;

import ivansCode.components.CodeBERTOption;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CodeBERTOutputParser {

    private CodeBERTOutputParser(){}

    public static void main(String[] args){
        String str = "[{'score': 0.7236989140510559, 'token': 8, 'token_str': ' and', 'sequence': " +
                "'if (x is not None) and(x>1)'}, {'score': 0.10633815824985504, 'token': 359, 'token_str': ' &', " +
                "'sequence': 'if (x is not None) &(x>1)'}, {'score': 0.021604152396321297, 'token': 463, " +
                "'token_str': 'and', 'sequence': 'if (x is not None)and(x>1)'}, {'score': 0.021227488294243813, " +
                "'token': 4248, 'token_str': ' AND', 'sequence': 'if (x is not None) AND(x>1)'}, " +
                "{'score': 0.016991255804896355, 'token': 114, 'token_str': ' if', 'sequence': " +
                "'if (x is not None) if(x>1)'}]";
        List<CodeBERTOption> list = parseCodeBERTStringOutput(str);
        System.out.println(list.get(0));
    }

    public static List<CodeBERTOption> parseCodeBERTStringOutput(String str){
        List<CodeBERTOption> list = new LinkedList<>();
        Pattern pattern = Pattern.compile(
                "\\{'score': (\\d+\\.\\d*), 'token': (\\d+), 'token_str': '([^']*)', 'sequence': '([^']*)'}");
        Matcher matcher = pattern.matcher(str);
        while (matcher.find()){
            double score = Double.parseDouble(str.substring(matcher.start(1), matcher.end(1)));
            int token = Integer.parseInt(str.substring(matcher.start(2), matcher.end(2)));
            String tokenString = str.substring(matcher.start(3), matcher.end(3));
            String sequence = str.substring(matcher.start(4), matcher.end(4));
            list.add(new CodeBERTOption(score, token, tokenString, sequence));
        }
        return list;
    }

}
