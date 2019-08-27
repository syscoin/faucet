package org.powfaucet.faucet;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.powfaucet.faucet.util.Settings;

@Configuration
public class FaucetConfiguration {

    @Bean
    public Settings settings(
        @Value("${org.powfaucet.faucet.hash.function}") String hashFunction,
        @Value("${org.powfaucet.faucet.coin.shortcut}") String shortcut,
        @Value("${org.powfaucet.faucet.coin.name}") String coinName,
        @Value("${org.powfaucet.faucet.hash.hashesPerCoin}") long hashesPerCoin,
        @Value("${org.powfaucet.faucet.urlOther}") String urlOther,
        @Value("${org.powfaucet.faucet.urlOtherName}") String urlOtherName,
        @Value("${org.powfaucet.faucet.minReward}") long minReward,
        @Value("${org.powfaucet.faucet.maxReward}") long maxReward,
        @Value("${org.powfaucet.faucet.hash.hashSize}") int hashSize,
        @Value("${org.powfaucet.faucet.clientWorkTime}") long clientWorkTime,
        @Value("${org.powfaucet.faucet.cache.maxSize}") long cacheMaxSize,
        @Value("${org.powfaucet.faucet.maxSessionTime}") long maxSessionTime
    ) {
        return new Settings(
            shortcut, 
            coinName, 
            hashFunction, 
            hashesPerCoin, 
            minReward, 
            maxReward, 
            hashSize, 
            clientWorkTime, 
            maxSessionTime, 
            cacheMaxSize, 
            urlOther, 
            urlOtherName
        );
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }
}
