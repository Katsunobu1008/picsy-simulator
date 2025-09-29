package com.example.picsy_engine;

import org.ejml.simple.SimpleMatrix;

/**
 * VCB + 反復法で c を求め、sum(c)=N にスケール
 */
public class ContributionCalculator {

    private static final int MAX_ITERATIONS = 500;
    private static final double EPS = 1e-9;

    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        // 1) VCB 変換
        SimpleMatrix Eprime = transformForVirtualCentralBank(evaluationMatrix);

        int n = Eprime.numRows();
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0);

        SimpleMatrix Et = Eprime.transpose();

        for (int k = 0; k < MAX_ITERATIONS; k++) {
            SimpleMatrix next = Et.mult(c);

            // L1 正規化（和=1）
            double sum = 0.0;
            for (int i = 0; i < n; i++) sum += Math.abs(next.get(i, 0));
            if (sum == 0.0) break;
            for (int i = 0; i < n; i++) next.set(i, 0, next.get(i, 0) / sum);

            double diff = 0.0;
            for (int i = 0; i < n; i++) diff += Math.abs(next.get(i, 0) - c.get(i, 0));
            c = next;
            if (diff < EPS) break;
        }

        // sum(c)=N にスケール（PICSY慣習）
        double sum = 0.0;
        for (int i = 0; i < n; i++) sum += c.get(i, 0);
        double scale = (sum == 0.0) ? 1.0 : (n / sum);
        for (int i = 0; i < n; i++) c.set(i, 0, c.get(i, 0) * scale);

        return c;
    }

    // E' = E - B + (B*D)/(N-1),  D=ones - I
    private SimpleMatrix transformForVirtualCentralBank(SimpleMatrix E) {
        int n = E.numRows();
        if (n <= 1) return E.copy();

        SimpleMatrix B = new SimpleMatrix(n, n);
        for (int i = 0; i < n; i++) B.set(i, i, E.get(i, i));

        SimpleMatrix D = SimpleMatrix.ones(n, n).minus(SimpleMatrix.identity(n));

        SimpleMatrix BD = B.mult(D);
        SimpleMatrix term3 = BD.divide(n - 1.0);

        return E.minus(B).plus(term3);
    }
}
