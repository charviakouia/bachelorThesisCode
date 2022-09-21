package ivansCode.components;

import com.github.javaparser.StaticJavaParser;

import java.util.HashMap;
import java.util.Map;

public class QuineBuilder {

    public static final String CLASS_NAME = "Main";

    private final Map<Integer, String> mutantIdToSourceCode = new HashMap<>();
    private final String originalSourceCode;

    public QuineBuilder(String originalSourceCode){
        this.originalSourceCode = StaticJavaParser.parse(originalSourceCode).toString();
    }

    public void add(Mutant mutant){
        mutantIdToSourceCode.put(mutant.getId(), mutant.toString());
    }

    public String build(){

        StringBuilder builder = new StringBuilder();

        builder.append("class Main { public static void main(String[] args){ ")
                .append("switch (Integer.parseInt(args[0])){ ")
                .append(
                        String.format("case -1: System.out.println(\"%s\"); break; ",
                                originalSourceCode
                                        .replace("\\", "\\\\")
                                        .replace("\"", "\\\"")
                                        .replace("\'", "\\\'")
                        )
                );

        for (Map.Entry<Integer, String> entry : mutantIdToSourceCode.entrySet()){
            builder.append(
                    String.format(
                            "case %d: System.out.println(\"%s\"); break; ",
                            entry.getKey(),
                            entry.getValue()
                                    .replace("\\", "\\\\")
                                    .replace("\"", "\\\"")
                                    .replace("\'", "\\\'")
                    )
            );
        }

        builder.append("default: throw new IllegalArgumentException(\"No such mutant\"); ")
                .append(" } } }");

        return builder.toString().replaceAll("[\\t\\n\\r]+"," ");

    }

}
