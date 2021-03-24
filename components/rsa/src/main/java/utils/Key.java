package utils;

import java.math.BigInteger;

public class Key {

    private final BigInteger n;
    private final BigInteger e;

    public Key(BigInteger n, BigInteger e) {
        this.n = n;
        this.e = e;
    }

    public BigInteger getN() {
        return n;
    }

    public BigInteger getE() {
        return e;
    }
}
