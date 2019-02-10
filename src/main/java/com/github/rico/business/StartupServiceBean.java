package com.github.rico.business;

import com.github.rico.model.entity.Fund;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

import static javax.transaction.Transactional.TxType.SUPPORTS;

@Singleton
@Startup
@Transactional(SUPPORTS)
public class StartupServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServiceBean.class);

    @Inject
    private CaixaGestServiceBean caixaGestServiceBean;

    private ExecutorService executor;

    private ExecutorService pool;

    @PostConstruct
    public void onCreate() throws InterruptedException, ExecutionException, TimeoutException {
        LocalDateTime startTime = LocalDateTime.now();
        LOGGER.info("Starting up Caixagest.");
        //check for updates on caixagest
        executor = Executors.newSingleThreadExecutor();
        // execute the task
        final Future<List<Fund>> future = executor.submit(() -> caixaGestServiceBean.checkFunds());
        final List<Fund> existing = future.get(5, TimeUnit.SECONDS);

        LOGGER.info("Proceeding with {} funds.", existing.size());

        pool = Executors.newFixedThreadPool(existing.size());
        existing.forEach(fund -> pool.execute(() -> caixaGestServiceBean.updateRates(fund)));

        LOGGER.info("Took me {} to complete.", startTime.until(LocalDateTime.now(), ChronoUnit.SECONDS));
    }

    @PreDestroy
    public void onDestroy() {
        LOGGER.info("Shutting down Caixagest.");

        Optional.ofNullable(executor).ifPresent(this::shutdownAndAwaitTermination);
        Optional.ofNullable(pool).ifPresent(this::shutdownAndAwaitTermination);
    }

    private void shutdownAndAwaitTermination(ExecutorService pool) {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(5, TimeUnit.SECONDS)) {
                pool.shutdownNow(); // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(2, TimeUnit.SECONDS))
                    LOGGER.error("Pool did not terminate");
            }
        } catch (InterruptedException ie) {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
