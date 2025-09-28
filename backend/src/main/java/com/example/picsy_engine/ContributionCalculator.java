// このファイルが com.example.picsyengine という住所に属していることを示す宣言です。
package com.example.picsy_engine;

// 「これから、外部のライブラリであるEJMLから、SimpleMatrixという便利な道具を使います」
// ということをJavaに教えるためのインポート宣言です。
import org.ejml.simple.SimpleMatrix;

/**
 * PICSYの貢献度ベクトルを計算するためのクラス（設計図）です。
 * このクラスは仮想中央銀行法に基づいて計算を行います。
 */
public class ContributionCalculator {

    // 反復計算の最大回数を定義します。「定数」と呼ばれ、後から変更しやすいように名前をつけています。
    private static final int MAX_ITERATIONS = 100;

    /**
     * 評価行列（入力）を受け取り、貢献度ベクトル（出力）を計算して返すメソッド（機能）です。
     * これが、この「貢献度計算機」が持つメインの機能となります。
     * @param evaluationMatrix コミュニティのメンバー間の評価を示す N x N の行列。
     * @return 計算された N x 1 の貢献度ベクトル（各メンバーの貢献度スコアのリスト）。
     */
    public SimpleMatrix calculate(SimpleMatrix evaluationMatrix) {
        // --- ステップ1: 仮想中央銀行法に基づき、計算用の行列 E' を作成 ---
        SimpleMatrix E_prime = transformForVirtualCentralBank(evaluationMatrix);

        // --- ステップ2: べき乗法による貢献度ベクトルの計算 ---
        // 行列のサイズ（コミュニティのメンバー数N）を取得します。
        // ※ .numRows() は古い命令なので、推奨されている .getNumRows() を使います。
        int n = E_prime.getNumRows();

        // 貢献度ベクトル c の初期状態を作成します。
        // 計算の出発点として、「全員の貢献度は等しく1.0である」という仮説を置きます。
        SimpleMatrix c = new SimpleMatrix(n, 1);
        c.fill(1.0);

        // べき乗法（Power Iteration）による反復計算を行います。
        for (int i = 0; i < MAX_ITERATIONS; i++) {
            // 計算式 c(t+1) = E'^T * c(t) を実行します。
            SimpleMatrix newC = E_prime.transpose().mult(c);

            // 計算を安定させるため、ベクトルの長さを1に保つ正規化を行います。
            double norm = newC.normF();
            if (norm == 0) {
                return newC;
            }
            c = newC.divide(norm);
        }

        // --- ステップ3: PICSYの定義に従い、貢献度の合計がNになるように最終調整 ---
        double sumOfElements = c.elementSum(); // ベクトルの全要素の合計を計算
        if (sumOfElements == 0) {
            return c; // 合計が0ならそのまま返す
        }
        double scalingFactor = n / sumOfElements; // 合計がNになるための拡大/縮小率を計算

        // 最終的な貢献度ベクトルを返す
        return c.scale(scalingFactor);
    }

    /**
     * 評価行列Eを、仮想中央銀行法で用いる計算用の行列E'に変換するヘルパーメソッドです。
     * このような補助的な機能を別のメソッドに切り出すことで、メインの`calculate`メソッドが
     * 見通しやすくなり、コードの品質が向上します。
     * @param E 元の評価行列
     * @return 変換後の行列 E'
     */
    private SimpleMatrix transformForVirtualCentralBank(SimpleMatrix E) {
        int n = E.getNumRows();
        if (n <= 1) {
            return E; // メンバーが1人以下の場合は変換不要
        }

        // B: Eの対角成分（自己評価＝予算）だけを抽出した対角行列
        SimpleMatrix B = new SimpleMatrix(n, n);
        for (int i = 0; i < n; i++) {
            B.set(i, i, E.get(i, i));
        }

        // D: 対角成分が0で、それ以外がすべて1の分配行列
        SimpleMatrix D = SimpleMatrix.ones(n, n).minus(SimpleMatrix.identity(n));

        // PICSYの論文(4.62)式: E' = E - B + (B*D)/(N-1) を計算
        SimpleMatrix BD = B.mult(D);
        SimpleMatrix term3 = BD.divide(n - 1.0);

        return E.minus(B).plus(term3);
    }
}
