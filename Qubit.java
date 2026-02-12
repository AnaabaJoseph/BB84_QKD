package org.example.bb84;

public class Qubit {
    private Basis basis;
    private int bit; // 0 or 1

    public Qubit(Basis basis, int bit) {
        this.basis = basis;
        this.bit = bit;
    }

    public Basis getBasis() {
        return basis;
    }

    public int getBit() {
        return bit;
    }

    public void setBit(int bit) {
        this.bit = bit;
    }

    public void setBasis(Basis basis) {
        this.basis = basis;
    }
}

