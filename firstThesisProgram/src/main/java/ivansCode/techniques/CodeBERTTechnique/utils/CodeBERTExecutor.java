package ivansCode.techniques.CodeBERTTechnique.utils;

import ivansCode.utils.ApplicationProperties;
import ivansCode.utils.ThreadService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public final class CodeBERTExecutor {

    public static String executeScript(String... params) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder(initializeArray(params));
        Process process = processBuilder.start();

        AtomicReference<String> result = new AtomicReference<>();
        Future<?> future = ThreadService.submit(() -> {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                result.set(bf.readLine());
                process.waitFor();
            } catch (IOException e){
                throw new IllegalStateException("Couldn't complete CodeBERT execution", e);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });

        try {
            future.get(2, TimeUnit.MINUTES);
            if (result.get() == null){
                throw new IllegalArgumentException("CodeBERT received too many tokens");
            } else {
                return result.get();
            }
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Couldn't complete CodeBERT execution", e);
        } catch (ExecutionException e){
            throw new IllegalStateException("Couldn't complete CodeBERT execution", e);
        } catch (TimeoutException e){
            future.cancel(true);
            System.out.println("Timeout occurred while executing CodeBERT");
            return "";
        } finally {
            process.destroyForcibly();
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
