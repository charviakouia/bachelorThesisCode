package ivansCode.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public final class ThreadService {

    private ThreadService(){}

    private static ExecutorService es;

    public static void startup(){
        es = Executors.newFixedThreadPool(5);
    }

    public static void shutdown(){
        es.shutdown();
    }

    public static Future<?> submit(Runnable runnable){
        return es.submit(runnable);
    }

}
