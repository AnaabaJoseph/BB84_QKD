package org.example.bb84;

import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

public class BB84Simulation {

    static class Trial {
        Basis aliceBasis;
        int aliceBit;
        Basis bobBasis;
        int bobBit;
        boolean lost;
    }

    public static void main(String[] args) {
        // Run parameter sweeps and generate CSV data
        sweepNoiseProbability();
        sweepDistance();

        System.out.println("\n=== Normal Simulation ===");
        runSimulation(20, 0.01, 0.05);

        System.out.println("\n=== Simulation with Eve ===");
        runSimulationWithEve(10);
        // To visualize, run: java Visualizer
        System.out.println("CSV files generated. Run 'java Visualizer' to view plots (Swing-based).");
    }

    private static void runSimulationWithEve(int numPhotons) {
        Alice alice = new Alice();
        Eve eve = new Eve();
        Bob bob = new Bob(0.9, 0.8);

        System.out.println("Photon | Alice Basis | Alice Bit | Eve Basis | Eve Bit | Bob Basis | Bob Bit");
        System.out.println("-------|-------------|-----------|-----------|---------|-----------|---------");

        for (int i = 0; i < numPhotons; i++) {
            Qubit aliceQubit = alice.prepareQubit();
            Basis aliceBasis = aliceQubit.getBasis();
            int aliceBit = aliceQubit.getBit();

            Eve.MeasurementResult eveResult = eve.interceptAndResend(aliceQubit);
            Basis eveBasis = eveResult.getBasis();
            int eveBit = eveResult.getBit();
            Qubit eveResentQubit = eveResult.getResentQubit();

            Bob.MeasurementResult bobResult = bob.measureQubit(eveResentQubit);
            Basis bobBasis = bobResult.getBasis();
            int bobBit = bobResult.getBit();

            System.out.printf("%6d | %11s | %9d | %9s | %7d | %9s | %7d%n",
                    i+1, aliceBasis, aliceBit, eveBasis, eveBit, bobBasis, bobBit);
        }
    }

    private static void runSimulation(int numPhotons, double noiseP, double lossP) {
        Alice alice = new Alice();
        Bob bob = new Bob(0.9, 0.8);
        Channel channel = new Channel(noiseP, lossP);

        List<Trial> trials = new ArrayList<>();

        for (int i = 0; i < numPhotons; i++) {
            Qubit qubit = alice.prepareQubit();
            Trial trial = new Trial();
            trial.aliceBasis = qubit.getBasis();
            trial.aliceBit = qubit.getBit();

            Qubit transmitted = channel.transmit(qubit);
            if (transmitted == null) {
                trial.lost = true;
            } else {
                trial.lost = false;
                Bob.MeasurementResult result = bob.measureQubit(transmitted);
                trial.bobBasis = result.getBasis();
                trial.bobBit = result.getBit();
            }
            trials.add(trial);
        }

        // Sifting
        List<Integer> siftedAliceBits = new ArrayList<>();
        List<Integer> siftedBobBits = new ArrayList<>();
        for (Trial t : trials) {
            if (!t.lost && t.aliceBasis == t.bobBasis) {
                siftedAliceBits.add(t.aliceBit);
                siftedBobBits.add(t.bobBit);
            }
        }

        // Compute QBER
        int errors = 0;
        for (int i = 0; i < siftedAliceBits.size(); i++) {
            if (!siftedAliceBits.get(i).equals(siftedBobBits.get(i))) {
                errors++;
            }
        }
        double qber = siftedAliceBits.isEmpty() ? 0.0 : (double) errors / siftedAliceBits.size();
        System.out.println("QBER: " + qber + ", Sifted Bits: " + siftedAliceBits.size() + ", Errors: " + errors);
    }

    private static void sweepNoiseProbability() {
        int numPhotons = 1000000; // Large scale
        double distance = 10.0; // Fixed distance
        double[] noisePs = {0.0, 0.01, 0.02, 0.05, 0.1, 0.15, 0.2};

        try (FileWriter writer = new FileWriter("qber_vs_noise.csv")) {
            writer.write("Noise Probability,QBER,Secure Key Rate\n");
            for (double noiseP : noisePs) {
                SimulationResult result = runSimulation(numPhotons, noiseP, distance, false);
                double keyRate = Metrics.computeSecureKeyRate(result.qber);
                writer.write(String.format("%.3f,%.6f,%.6f\n", noiseP, result.qber, keyRate));
                System.out.printf("Noise: %.3f, QBER: %.6f, Key Rate: %.6f\n", noiseP, result.qber, keyRate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void sweepDistance() {
        int numPhotons = 1000000;
        double noiseP = 0.01; // Fixed noise
        double[] distances = {1.0, 5.0, 10.0, 20.0, 50.0, 100.0};

        try (FileWriter writer = new FileWriter("qber_vs_distance.csv")) {
            writer.write("Distance (km),QBER,Secure Key Rate\n");
            for (double dist : distances) {
                SimulationResult result = runSimulation(numPhotons, noiseP, dist, false);
                double keyRate = Metrics.computeSecureKeyRate(result.qber);
                writer.write(String.format("%.1f,%.6f,%.6f\n", dist, result.qber, keyRate));
                System.out.printf("Distance: %.1f km, QBER: %.6f, Key Rate: %.6f\n", dist, result.qber, keyRate);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static SimulationResult runSimulation(int numPhotons, double noiseP, double distance, boolean withEve) {
        Alice alice = new Alice();
        Bob bob = new Bob(0.9, 0.8); // Z efficiency 0.9, X 0.8
        Channel channel = new Channel(noiseP, distance);
        Eve eve = withEve ? new Eve() : null;

        List<Trial> trials = new ArrayList<>();
        int[] aliceSifted = new int[numPhotons];
        int[] bobSifted = new int[numPhotons];
        int siftedCount = 0;

        for (int i = 0; i < numPhotons; i++) {
            Qubit qubit = alice.prepareQubit();
            Trial trial = new Trial();
            trial.aliceBasis = qubit.getBasis();
            trial.aliceBit = qubit.getBit();

            Qubit transmitted = channel.transmit(qubit);
            if (transmitted == null) {
                trial.lost = true;
            } else {
                trial.lost = false;
                if (withEve) {
                    Eve.MeasurementResult eveResult = eve.interceptAndResend(transmitted);
                    transmitted = eveResult.getResentQubit();
                }
                Bob.MeasurementResult result = bob.measureQubit(transmitted);
                trial.bobBasis = result.getBasis();
                trial.bobBit = result.getBit();
            }
            trials.add(trial);

            // Sifting
            if (!trial.lost && trial.aliceBasis == trial.bobBasis) {
                aliceSifted[siftedCount] = trial.aliceBit;
                bobSifted[siftedCount] = trial.bobBit;
                siftedCount++;
            }

        }

        // Trim arrays to actual sifted count
        int[] aliceSiftedTrimmed = Arrays.copyOf(aliceSifted, siftedCount);
        int[] bobSiftedTrimmed = Arrays.copyOf(bobSifted, siftedCount);

        double qber = Metrics.computeQBER(aliceSiftedTrimmed, bobSiftedTrimmed);
        return new SimulationResult(qber, siftedCount);
    }

    static class SimulationResult {
        double qber;
        int siftedBits;

        SimulationResult(double qber, int siftedBits) {
            this.qber = qber;
            this.siftedBits = siftedBits;
        }
    }
}
