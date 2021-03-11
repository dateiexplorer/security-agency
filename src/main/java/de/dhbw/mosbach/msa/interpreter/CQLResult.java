package de.dhbw.mosbach.msa.interpreter;

public class CQLResult {

    public enum Type {
        OK, ERROR, MESSAGE
    }

    private final Type type;
    private final String output;

    public CQLResult(CQLResult.Type type, String output) {
        this.type = type;
        this.output = output;
    }

    public Type getType() {
        return type;
    }

    public String getOutput() {
        return output;
    }
}
