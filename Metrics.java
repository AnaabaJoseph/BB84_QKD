package org.example.bb84;

public class Metrics {

    /**
     * Computes the Quantum Bit Error Rate (QBER) from sifted bits.
     * @param aliceBits Alice's sifted bits
     * @param bobBits Bob's sifted bits
     * @return QBER (0.0 to 1.0)
     */
    public static double computeQBER(int[] aliceBits, int[] bobBits) {
        if (aliceBits.length != bobBits.length || aliceBits.length == 0) {
            return 0.0;
        }
        int errors = 0;
        for (int i = 0; i < aliceBits.length; i++) {
            if (aliceBits[i] != bobBits[i]) {
                errors++;
            }
        }
        return (double) errors / aliceBits.length;
    }

    /**
     * Computes the binary entropy h(p) = -p*log2(p) - (1-p)*log2(1-p)
     * @param p probability (0.0 to 1.0)
     * @return entropy in bits
     */
    private static double binaryEntropy(double p) {
        if (p == 0.0 || p == 1.0) {
            return 0.0;
        }
        return -p * Math.log(p) / Math.log(2) - (1 - p) * Math.log(1 - p) / Math.log(2);
    }

    /**
     * Computes the asymptotic secure key rate using Devetak bound.
     * r = 1 - 2 * h(QBER)
     * @param qber Quantum Bit Error Rate
     * @return secure key rate (bits per sifted bit)
     */
    public static double computeSecureKeyRate(double qber) {
        if (qber >= 0.5) {
            return 0.0; // No secure key if QBER >= 50%
        }
        return 1.0 - 2.0 * binaryEntropy(qber);
    }
}
