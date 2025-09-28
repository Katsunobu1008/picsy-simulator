// このファイルがどのパッケージ（住所）に属しているかを示す宣言
package com.example.picsyengine;

// 「EJMLライブラリからSimpleMatrixという道具を使います」というインポート宣言
import org.ejml.simple.SimpleMatrix;

/**
 * PICSYの貢献度ベクトルを計算するためのクラス。
 * このクラスは、評価行列を受け取り、貢献度ベクトルを計算する責務のみを持つ。
 */
public class ContributionCalculator {

    // 反復計算の回数。この回数が多いほど精度は上がるが、計算に時間がかかる。
    private static final int MAX_ITERATIONS = 100;

    /**
     * 評価行列から貢献度ベクトルを計算するメソッド。
     * @param evaluationMatrix N x N の評価行列
     * @return N x 1 の貢献度ベクトル（各要素の合計がNになるように正規化済み）
     */
    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        int n = evaluationMatrix.numRows();

        // 貢献度ベクトルの初期値を全員1.0で作成
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0);

        // べき乗法による反復計算
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // PICSYの基本式: c(t+1) = E^T * c(t)
            SimpleMatrix newC = evaluationMatrix.transpose().mult(c);

            // 計算安定化のための正規化（ベクトルの長さを1にする）
            double norm = newC.normF();
            if (norm == 0) {
                return newC; // ゼロベクトルの場合はそのまま返す
            }
            c = newC.divide(norm);
        }

        // ▼▼▼【プロとしての修正点】▼▼▼
        // 最終的な貢献度ベクトルは、合計がNになるようにスケーリングする
        // これがPICSYモデルの正式な定義
        double sum = c.elementSum(); // ベクトルの全要素の合計を計算
        if (sum == 0) {
            return c; // 合計が0ならそのまま返す（エラーケース）
        }
        return c.scale(n / sum); // (N / 合計値) をベクトル全体に掛ける
    }
}
