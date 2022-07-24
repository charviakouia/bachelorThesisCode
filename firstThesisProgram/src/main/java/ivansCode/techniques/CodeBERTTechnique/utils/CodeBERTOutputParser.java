package ivansCode.techniques.CodeBERTTechnique.utils;

import ivansCode.techniques.CodeBERTTechnique.components.CodeBERTOption;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CodeBERTOutputParser {

    private CodeBERTOutputParser(){}

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
