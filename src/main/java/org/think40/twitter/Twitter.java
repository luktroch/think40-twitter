package org.think40.twitter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.think40.twitter.repository.TweetService;

import java.util.concurrent.TimeUnit;

@SpringBootApplication()
public class Twitter implements ApplicationListener<ApplicationReadyEvent> {

    private static ConfigurableApplicationContext ctx;
    @Autowired
    private TweetService service;

    private Logger logger = LoggerFactory.getLogger(Twitter.class);


    public static void main(String[] args) {
       ctx = SpringApplication.run(Twitter.class, args);
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        waitForCluster();
        service.initializeIndex();
        logger.info("Service is ready");
    }

    private void waitForCluster() {
        for (int i = 0; i < 5; i++) {
            logger.info("Waiting for elastic...");
            if (service.ping()) return;
            try {
                TimeUnit.SECONDS.sleep(10);
            } catch (InterruptedException e) {
                logger.warn("",e);
            }

        }
        logger.error("Unable to ping elasticsearch");
        ctx.close();
    }
}
