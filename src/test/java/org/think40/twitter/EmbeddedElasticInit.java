package org.think40.twitter;

import org.springframework.boot.test.context.TestComponent;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import pl.allegro.tech.embeddedelasticsearch.EmbeddedElastic;
import pl.allegro.tech.embeddedelasticsearch.PopularProperties;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Configuration
public class EmbeddedElasticInit implements
        ApplicationContextInitializer<ConfigurableApplicationContext> {


    private static EmbeddedElastic embeddedElastic;

    public EmbeddedElastic getEmbeddedElastic(){
        return embeddedElastic;
    }

    @Override
    public void initialize(ConfigurableApplicationContext applicationContext) {
        try {
        embeddedElastic = EmbeddedElastic.builder()
                .withElasticVersion("6.6.1")
                .withSetting(PopularProperties.HTTP_PORT, 9200)
                .withStartTimeout(60, TimeUnit.SECONDS)
                .build();

            embeddedElastic.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
