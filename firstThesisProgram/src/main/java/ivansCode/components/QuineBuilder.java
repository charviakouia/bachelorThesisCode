package ivansCode.components;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class QuineBuilder {

    public static final String CLASS_NAME = "Main";

    private final Map<Integer, String> mutantIdToSourceCodeMap = new HashMap<>();
    private final String originalSourceCode;

    public QuineBuilder(String originalSourceCode){
        this.originalSourceCode = originalSourceCode;
    }

    public void add(Mutant mutant){
        mutantIdToSourceCodeMap.put(mutant.getId(), mutant.toString());
    }

    public String build(){
        StringBuilder builder = new StringBuilder();
        builder.append("class Main { public static void main(String[] args){ ")
                .append("switch (Integer.parseInt(args[0])){ ")
                .append(String.format("case -1: System.out.println(\"%s\"); break; ",
                        originalSourceCode.replace("\"", "\\\"")));
        for (Map.Entry<Integer, String> entry : mutantIdToSourceCodeMap.entrySet()){
            builder.append(String.format("case %d: System.out.println(\"%s\"); break; ",
                    entry.getKey(), entry.getValue().replace("\"", "\\\"")));
        }
        builder.append("default: throw new IllegalArgumentException(\"No such mutant\"); ")
                .append(" } } }");
        return builder.toString().replaceAll("[\\t\\n\\r]+"," ");
    }

}
