package es.urjc.etsii.grafo.PDSP;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Parallel {

    public static ExecutorService executor;
    private static AtomicInteger counter = new AtomicInteger(1);

    public static synchronized void initialize(int nWorkers){
        if(executor != null) return;
        executor = Executors.newFixedThreadPool(nWorkers, r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("Mork-" + counter.getAndIncrement());
            return t;
        });
    }
}
