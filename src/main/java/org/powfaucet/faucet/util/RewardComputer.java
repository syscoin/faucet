package org.powfaucet.faucet.util;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.BigInteger;

@Service
public class RewardComputer {

    public static final BigInteger COIN = new BigInteger("100000000");
    public static final BigDecimal COIN_BD = new BigDecimal("100000000");

    public BigInteger compute(BigInteger Ri, BigInteger hashesPerCoin, BigInteger maxHash) {
        return COIN.multiply(maxHash).divide(Ri).divide(hashesPerCoin);
    }
}
