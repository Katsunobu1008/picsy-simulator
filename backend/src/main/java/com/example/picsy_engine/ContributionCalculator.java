// このファイルがどのパッケージ（住所）に属しているかを示す宣言
package com.example.picsy_engine;

// 「EJMLライブラリからSimpleMatrixという道具を使います」というインポート宣言
import org.ejml.simple.SimpleMatrix;

/**
 * PICSYの貢献度ベクトルを計算するためのクラス。
 * 仮想中央銀行法に基づいて計算を行う。
 */
public class ContributionCalculator {

    private static final int MAX_ITERATIONS = 100;

    /**
     * 評価行列から貢献度ベクトルを計算するメソッド。
     * @param evaluationMatrix N x N の評価行列 (E)
     * @return N x 1 の貢献度ベクトル (c)
     */
    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        int n = evaluationMatrix.getNumRows();

        // ▼▼▼【仮想中央銀行法の核心部分】▼▼▼
        // 評価行列Eを、計算用の行列E'に変換する
        SimpleMatrix e_prime = createVirtualCentralBankMatrix(evaluationMatrix);
        // ▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲▲

        // 貢献度ベクトルの初期値を全員1.0で作成
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0);

        // べき乗法による反復計算 (計算にはE'を使用)
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // PICSYの基本式: c(t+1) = (E')^T * c(t)
            SimpleMatrix newC = e_prime.transpose().mult(c);

            // 計算安定化のための正規化
            double norm = newC.normF();
            if (norm == 0) return newC;
            c = newC.divide(norm);
        }

        // 最終的な貢献度ベクトルは、合計がNになるようにスケーリングする
        double sum = c.elementSum();
        if (sum == 0) return c;
        return c.scale(n / sum);
    }

    /**
     * 仮想中央銀行法の計算用行列E'を作成するプライベートメソッド。
     * E' = E - B + (B * D) / (N-1)
     * @param E 元の評価行列
     * @return E' 変換後の計算用行列
     */
    private SimpleMatrix createVirtualCentralBankMatrix(SimpleMatrix E) {
        int n = E.getNumRows();
        if (n <= 1) return E; // メンバーが1人以下の場合は変換不要

        // B: Eの対角成分のみを抽出した対角行列
        SimpleMatrix B = new SimpleMatrix(n, n);
        for (int i = 0; i < n; i++) {
            B.set(i, i, E.get(i, i));
        }

        // D: 対角成分が0で、それ以外が1の行列
        SimpleMatrix D = SimpleMatrix.ones(n, n);
        for (int i = 0; i < n; i++) {
            D.set(i, i, 0);
        }

        // E' = E - B + BD / (N-1) の計算
        SimpleMatrix E_minus_B = E.minus(B);
        SimpleMatrix BD = B.mult(D);
        SimpleMatrix BD_div_N_minus_1 = BD.divide(n - 1);

        return E_minus_B.plus(BD_div_N_minus_1);
    }
}
