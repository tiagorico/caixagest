package com.github.rico.business;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Singleton
@Startup
@TransactionAttribute(TransactionAttributeType.SUPPORTS)
public class StartupServiceBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(StartupServiceBean.class);

    @Inject
    private CaixaGestServiceBean caixaGestServiceBean;

    private ExecutorService executor;

    @PostConstruct
    public void onCreate() {
        LOGGER.info("Starting up Caixagest.");
        //check for updates on caixagest
        executor = Executors.newSingleThreadExecutor();
        // execute the timer task
        executor.execute(() -> caixaGestServiceBean.checkFunds());
    }

    @PreDestroy
    public void onDestroy() {
        LOGGER.info("Shutting down Caixagest.");
        Optional.ofNullable(executor).ifPresent(executorService -> {
            executorService.shutdown();
            try {
                if (!executorService.awaitTermination(800, TimeUnit.MILLISECONDS)) {
                    executorService.shutdownNow();
                }
            } catch (InterruptedException e) {
                executorService.shutdownNow();
            }
        });
    }
}
