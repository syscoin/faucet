package org.powfaucet.faucet.controller;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class ClaimForm {

    @NotNull(message = "No hash was provided")
    @Size(min = 16, max = 256, message = "Invalid hash length")
    private String bestHash;

    @NotNull(message = "No seed was provided")
    private String seed;

    @SuppressWarnings("unused")
    public String getBestHash() {
        return this.bestHash;
    }

    @SuppressWarnings("unused")
    public void setBestHash(String bestHash) {
        this.bestHash = bestHash;
    }

    @SuppressWarnings("unused")
    public String getSeed() {
        return seed;
    }

    @SuppressWarnings("unused")
    public void setSeed(String seed) {
        this.seed = seed;
    }

    @Override
    public String toString() {
        return "ClaimForm{" +
                "bestHash='" + bestHash + '\'' +
                ", seed='" + seed + '\'' +
                '}';
    }
}