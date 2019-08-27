package org.powfaucet.faucet.util;

import java.math.BigDecimal;
import java.math.BigInteger;

public class Settings {

    public final String shortcut;
    public final String coinName;
    public final String hashFunction;
    public final long hashesPerCoin;
    public final String urlOther;
    public final String urlOtherName;
    public final BigInteger minReward;
    public final BigInteger maxReward;
    public final int hashSize;
    public final long clientWorkTime;
    public final long maxSessionTime;
    public final long cacheMaxSize;
    public final BigDecimal maxRewardBD;
    private final BigInteger maxHash;

    public Settings(
        String shortcut,
        String coinName,
        String hashFunction,
        long hashesPerCoin,
        long minReward,
        long maxReward,
        int hashSize,
        long clientWorkTime,
        long maxSessionTime,
        long cacheMaxSize,
        String urlOther,
        String urlOtherName
    ) {
        this.shortcut = shortcut;
        this.coinName = coinName;
        this.hashFunction = hashFunction;
        this.hashesPerCoin = hashesPerCoin;
        this.urlOther = urlOther;
        this.urlOtherName = urlOtherName;
        this.minReward = BigInteger.valueOf(minReward);
        this.maxReward = BigInteger.valueOf(maxReward);
        this.maxRewardBD = BigDecimal.valueOf(maxReward);
        this.hashSize = hashSize;
        this.clientWorkTime = clientWorkTime;
        this.maxSessionTime = maxSessionTime;
        this.cacheMaxSize = cacheMaxSize;
        this.maxHash = new BigInteger("f".repeat(hashSize / 4), 16);
    }

    public BigInteger getMaxHash() {
        return maxHash;
    }
}
