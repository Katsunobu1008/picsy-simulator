// このテストファイルも、コードと同じ住所に属していることを示します。
package com.example.picsy_engine;

// テストに必要な道具（ライブラリ）を使えるようにするためのインポート宣言です。
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test; // 正しい「定規」をインポートします

/**
 * ContributionCalculatorクラスの品質を検査するためのテストクラスです。
 */
public class ContributionCalculatorTest {

    // 「@Test」という印は、これがテスト項目であることを示します。
    @Test
    void testVirtualCentralBankCalculation() {
        // --- 準備 (Given) ---
        // テスト対象の「貢献度計算機」のインスタンスを作ります。
        ContributionCalculator calculator = new ContributionCalculator();

        // テスト用の評価行列を用意します。
        // Aさん -> (A:0.1, B:0.6, C:0.3)
        // Bさん -> (A:0.2, B:0.1, C:0.7)
        // Cさん -> (A:0.5, B:0.4, C:0.1)
        SimpleMatrix matrix = new SimpleMatrix(new double[][]{
            {0.1, 0.6, 0.3},
            {0.2, 0.1, 0.7},
            {0.5, 0.4, 0.1}
        });

        // --- 実行 (When) ---
        // 実際に計算機を動かして、貢献度ベクトルを計算させます。
        SimpleMatrix result = calculator.calculate(matrix);

        // --- 検証 (Then) ---
        // 実行結果が、私たちが期待する「正しい答え」と一致するかを検証します。

        // 仮想中央銀行法 + 合計=N の正規化ルールに基づいた、期待される貢献度の理論値です。
        // 計算方法が変わったため、期待値も新しくなっています。
        double expectedA = 0.861; // ±0.01 許容なら 0.861〜0.862 程度でも可
        double expectedB = 1.050;
        double expectedC = 1.089;

        // 実際の計算結果から、各メンバーの貢献度スコアを取り出します。
        double actualA = result.get(0, 0);
        double actualB = result.get(1, 0);
        double actualC = result.get(2, 0);

        // 結果の合計値が、メンバー数N（今回は3）になることも検証します。
        double totalContribution = result.elementSum();
        assertThat(totalContribution).isCloseTo(3.0, within(0.01));

        // ★★★ エラー修正箇所 ★★★
        // AssertJの正しい使い方: isCloseToの第二引数には within(誤差) を使います。
        // これで「誤差0.01の範囲内でほぼ等しいか」を正しく検査できます。
        assertThat(actualA).isCloseTo(expectedA, within(0.01));
        assertThat(actualB).isCloseTo(expectedB, within(0.01));
        assertThat(actualC).isCloseTo(expectedC, within(0.01));
    }
}
