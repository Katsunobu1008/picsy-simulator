package com.example.picsy_engine;

import org.ejml.simple.SimpleMatrix;

/**
 * PICSY の貢献度ベクトル c を計算する中核クラス。
 *
 * 1) 仮想中央銀行法(VCB)で自己ループ(予算)を仮想的に解体した行列 E' を作る:
 *    E' = E - B + (B*D)/(N-1)
 *      - E: 評価行列 (行和=1)
 *      - B: 対角に E_ii を持つ対角行列 (各人の予算)
 *      - D: 対角0、非対角1の行列
 * 2) E'^T を繰り返し掛ける反復法(べき乗法)で定常分布を推定し、
 *    sum(c)=N となるようにスケールする（PICSY慣習）。
 *
 * 注意:
 * - EJML の SimpleMatrix の行数は numRows() で取得します（getNumRows()は存在しません）。
 * - 収束判定は L1 ノルム差 < EPS。
 */
public class ContributionCalculator {

    private static final int MAX_ITERATIONS = 500; // 反復上限
    private static final double EPS = 1e-9;        // 収束しきい値

    /** 与えられた評価行列 E (行和=1) から貢献度ベクトル c を返す */
    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        // 1) VCB 変換で E' を作成
        SimpleMatrix Eprime = transformForVirtualCentralBank(evaluationMatrix);

        int n = Eprime.getNumRows();
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0); // 初期ベクトルは全要素1（中立）

        SimpleMatrix Et = Eprime.transpose();

        // 2) べき乗法： c ← normalize( E'^T * c )
        for (int k = 0; k < MAX_ITERATIONS; k++) {
            SimpleMatrix next = Et.mult(c);

            // L1 正規化（ベクトル和=1にする）
            double sum = 0.0;
            for (int i = 0; i < n; i++) sum += Math.abs(next.get(i, 0));
            if (sum == 0.0) break;
            for (int i = 0; i < n; i++) next.set(i, 0, next.get(i, 0) / sum);

            // 収束判定（L1差）
            double diff = 0.0;
            for (int i = 0; i < n; i++) diff += Math.abs(next.get(i, 0) - c.get(i, 0));
            c = next;
            if (diff < EPS) break;
        }

        // 3) sum(c) = N にスケール（PICSY慣習）
        double sum = 0.0;
        for (int i = 0; i < n; i++) sum += c.get(i, 0);
        double scale = (sum == 0.0) ? 1.0 : (n / sum);
        for (int i = 0; i < n; i++) c.set(i, 0, c.get(i, 0) * scale);

        return c;
    }

    /** VCB 変換: E' = E - B + (B*D)/(N-1) */
    private SimpleMatrix transformForVirtualCentralBank(SimpleMatrix E) {
        int n = E.getNumRows();
        if (n <= 1) return E.copy();

        // B: diag(E_ii)
        SimpleMatrix B = new SimpleMatrix(n, n);
        for (int i = 0; i < n; i++) B.set(i, i, E.get(i, i));

        // D: 対角0・非対角1
        SimpleMatrix D = SimpleMatrix.ones(n, n).minus(SimpleMatrix.identity(n));

        // term3 = (B*D)/(N-1)
        SimpleMatrix BD = B.mult(D);
        SimpleMatrix term3 = BD.divide(n - 1.0);

        return E.minus(B).plus(term3);
    }
}
