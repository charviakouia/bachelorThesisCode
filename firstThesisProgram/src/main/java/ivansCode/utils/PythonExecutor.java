package ivansCode.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Files;
import java.sql.Array;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

public final class PythonExecutor {

    public static String executeScript(String... params) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(initializeArray(params));
        Process process = processBuilder.start();

        List<String> result = new BufferedReader(new InputStreamReader(process.getInputStream())).lines().toList();

        if (result.isEmpty()){
            throw new IllegalArgumentException("CodeBERT received too many tokens");
        } else {
            return result.get(0);
        }

    }

    private static String[] initializeArray(String... pythonParams){

        URL url = ApplicationProperties.class.getClassLoader().getResource("example.py");
        assert url != null;

        String[] paramArr = new String[pythonParams.length + 2];
        System.arraycopy(pythonParams, 0, paramArr, 2, pythonParams.length);
        paramArr[0] = "python3";
        paramArr[1] = url.getPath();

        return paramArr;

    }

}
