package org.example.bb84;

import java.util.Random;

public class Channel {
    private Random random = new Random();
    private double noiseProbability; // depolarizing noise probability
    private double distance; // channel distance in km
    private static final double ALPHA = 0.02; // attenuation coefficient per km (typical for fiber)

    public Channel(double noiseProbability, double distance) {
        this.noiseProbability = noiseProbability;
        this.distance = distance;
    }

    public Qubit transmit(Qubit qubit) {
        // Compute distance-dependent photon loss probability
        double lossProbability = 1 - Math.exp(-ALPHA * distance);

        // First, check for photon loss
        if (random.nextDouble() < lossProbability) {
            return null; // Photon lost
        }

        // Apply depolarizing noise
        if (random.nextDouble() < noiseProbability) {
            // Depolarizing: qubit becomes random
            Basis newBasis = random.nextBoolean() ? Basis.Z : Basis.X;
            int newBit = random.nextInt(2);
            qubit.setBasis(newBasis);
            qubit.setBit(newBit);
        }
        // Else, qubit unchanged
        return qubit;
    }
}
