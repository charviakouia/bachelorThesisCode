package ivansCode.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class CodeLoader {

    public static String getOriginalSourceCode(Path experimentPath) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder("java", "Main", String.valueOf(-1));
        processBuilder.directory(experimentPath.toFile());

        return executeProcess(processBuilder.start());

    }

    public static String getSourceCodeForMutant(Path experimentPath, int mutantId) throws IOException {

        ProcessBuilder processBuilder = new ProcessBuilder("java", "Main", String.valueOf(mutantId));
        processBuilder.directory(experimentPath.toFile());

        return executeProcess(processBuilder.start());

    }

    private static String executeProcess(Process process){

        AtomicReference<String> result = new AtomicReference<>();
        Future<?> future = ThreadService.submit(() -> {
            try (BufferedReader bf = new BufferedReader(new InputStreamReader(process.getInputStream()))){
                result.set(bf.readLine());
                process.waitFor();
            } catch (IOException e){
                throw new IllegalStateException("File containing the code couldn't be read", e);
            } catch (InterruptedException e){
                Thread.currentThread().interrupt();
            }
        });

        try {
            future.get(2, TimeUnit.MINUTES);
            return result.get();
        } catch (InterruptedException e){
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Process was interrupted", e);
        } catch (ExecutionException e){
            throw new IllegalStateException("Process unexpectedly aborted", e);
        } catch (TimeoutException e){
            future.cancel(true);
            throw new IllegalStateException("Timeout occurred while retrieving code", e);
        } finally {
            process.destroyForcibly();
        }

    }

}
