package com.market.analytics.domain;

public enum Temporality {

    D("D"),
    H("60");

    public final String per;

    Temporality(String per) {
        this.per = per;
    }

    public String getPer() {
        return per;
    }

}
