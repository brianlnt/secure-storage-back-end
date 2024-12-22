package project.brianle.securestorage.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class CacheConfig {

    @Bean
    public CacheStore<String, Integer> cacheStore(){
        return new CacheStore<>(900, TimeUnit.SECONDS);
    }
}
