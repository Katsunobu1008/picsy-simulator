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
    // まずは100回で十分な精度が得られる。
    private static final int MAX_ITERATIONS = 100;

    /**
     * 評価行列から貢献度ベクトルを計算するメソッド。
     * これは、この「貢献度計算機」のメイン機能。
     * @param evaluationMatrix N x N の評価行列
     * @return N x 1 の貢献度ベクトル（列ベクトル）
     */
    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        // 行列のサイズ（メンバーの数N）を取得する
        int n = evaluationMatrix.numRows();

        // 貢献度ベクトルの初期値を設定する。
        // 最初は「全員の貢献度は等しい」という仮定からスタートする。
        // 全員の貢献度の初期値を1.0とする N x 1 のベクトルを作成。
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0);

        // べき乗法（Power Iteration）による反復計算
        // このループがPICSYの計算の心臓部。
        // 水槽のアナロジーで言えば、水が安定状態になるまで何度も循環させるプロセス。
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // PICSYの基本式: c(t+1) = E^T * c(t)
            // E^T (転置行列) と現在の貢献度ベクトルcを掛けて、新しいcを求める。
            // これが「他者からの評価の流入」を計算している部分。
            SimpleMatrix newC = evaluationMatrix.transpose().mult(c);

            // 正規化（Normalization）
            // 計算の過程でベクトルの長さ（大きさ）が変わってしまうのを防ぐため、
            // ベクトルの長さを常に1に保つ操作。これにより計算が安定する。
            double norm = newC.normF(); // ベクトルの長さを計算
            if (norm == 0) {
                return newC; // 長さが0ならそのまま返す
            }
            c = newC.divide(norm); // ベクトルの各要素をその長さで割り、長さを1にする
        }

        // 最終的な貢献度ベクトルを返す
        return c;
    }
}