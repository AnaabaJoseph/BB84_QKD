package org.example.bb84;

import java.util.Random;

public class Bob {
    private Random random = new Random();
    private double efficiencyZ; // detector efficiency for Z basis
    private double efficiencyX; // detector efficiency for X basis

    public Bob(double efficiencyZ, double efficiencyX) {
        this.efficiencyZ = efficiencyZ;
        this.efficiencyX = efficiencyX;
    }

    public MeasurementResult measureQubit(Qubit qubit) {
        Basis measurementBasis = random.nextBoolean() ? Basis.Z : Basis.X;
        int measuredBit;
        double efficiency = (measurementBasis == Basis.Z) ? efficiencyZ : efficiencyX;

        // Check if measurement succeeds based on efficiency
        if (random.nextDouble() < efficiency) {
            if (measurementBasis == qubit.getBasis()) {
                measuredBit = qubit.getBit();
            } else {
                measuredBit = random.nextInt(2);
            }
        } else {
            // Measurement fails, random bit
            measuredBit = random.nextInt(2);
        }
        return new MeasurementResult(measurementBasis, measuredBit, efficiency);
    }

    public static class MeasurementResult {
        private Basis basis;
        private int bit;
        private double efficiency; // added for tracking

        public MeasurementResult(Basis basis, int bit, double efficiency) {
            this.basis = basis;
            this.bit = bit;
            this.efficiency = efficiency;
        }

        public Basis getBasis() {
            return basis;
        }

        public int getBit() {
            return bit;
        }

        public double getEfficiency() {
            return efficiency;
        }
    }
}
