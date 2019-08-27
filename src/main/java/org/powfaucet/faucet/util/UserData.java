package org.powfaucet.faucet.util;

import java.time.Instant;

public class UserData {

    public final Instant created;
    public final String seed;
    public final String ip;
    private String targetAddress;
    private String bestHash;

    public UserData(Instant created, String seed, String ip) {
        this.created = created;
        this.seed = seed;
        this.ip = ip;
        this.targetAddress = "";
        this.bestHash = "";
    }

    @Override
    public synchronized String toString() {
        return "UserData{" +
                "created=" + created +
                ", seed='" + seed + '\'' +
                ", ip='" + ip + '\'' +
                ", targetAddress='" + targetAddress + '\'' +
                ", bestHash='" + bestHash + '\'' +
                '}';
    }

    public synchronized String getTargetAddress() {
        return targetAddress;
    }

    public synchronized void setTargetAddress(String targetAddress) {
        this.targetAddress = targetAddress;
    }

    public synchronized String getBestHash() {
        return bestHash;
    }

    public synchronized void setBestHash(String bestHash) {
        this.bestHash = bestHash;
    }
}
