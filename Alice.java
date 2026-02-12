package org.example.bb84;

import java.util.Random;

public class Alice {
    private final Random random = new Random();

    public Qubit prepareQubit() {
        Basis basis = random.nextBoolean() ? Basis.Z : Basis.X;
        int bit = random.nextInt(2);
        return new Qubit(basis, bit);
    }
}
