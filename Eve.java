package org.example.bb84;

import java.util.Random;

public class Eve {
    private Random random = new Random();

    public MeasurementResult interceptAndResend(Qubit qubit) {
        Basis measurementBasis = random.nextBoolean() ? Basis.Z : Basis.X;
        int measuredBit;
        if (measurementBasis == qubit.getBasis()) {
            measuredBit = qubit.getBit();
        } else {
            measuredBit = random.nextInt(2);
        }
        // Resend the measured qubit (which is now in Eve's basis)
        Qubit resentQubit = new Qubit(measurementBasis, measuredBit);
        return new MeasurementResult(measurementBasis, measuredBit, resentQubit);
    }

    public static class MeasurementResult {
        private Basis basis;
        private int bit;
        private Qubit resentQubit;

        public MeasurementResult(Basis basis, int bit, Qubit resentQubit) {
            this.basis = basis;
            this.bit = bit;
            this.resentQubit = resentQubit;
        }

        public Basis getBasis() {
            return basis;
        }

        public int getBit() {
            return bit;
        }

        public Qubit getResentQubit() {
            return resentQubit;
        }
    }
}
