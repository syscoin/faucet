package org.powfaucet.faucet.util;

public class Claim {

    public final String reward;
    public final String targetAddress;
    public final String txid;

    public Claim(String reward, String targetAddress, String txid) {

        this.reward = reward;
        this.targetAddress = targetAddress;
        this.txid = txid;
    }

}
