package com.example.picsyengine;

// テストに必要な道具をインポートする
import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * ContributionCalculatorクラスが正しく動作するかを検証するためのテストクラス。
 */
public class ContributionCalculatorTest {

    // @Test は、これがテスト用のメソッド（検査項目）であることを示す印
    @Test
    void testSimpleCalculation() {
        // --- 準備 (Given) ---
        // テスト対象の「貢献度計算機」インスタンスを作成する
        ContributionCalculator calculator = new ContributionCalculator();

        // テストに使う、3人の小さなコミュニティの評価行列を作成する
        // Aさんは (A:0.1, B:0.5, C:0.4) と評価
        // Bさんは (A:0.2, B:0.1, C:0.7) と評価
        // Cさんは (A:0.6, B:0.3, C:0.1) と評価
        SimpleMatrix matrix = new SimpleMatrix(new double[][]{
            {0.1, 0.5, 0.4},
            {0.2, 0.1, 0.7},
            {0.6, 0.3, 0.1}
        });

        // --- 実行 (When) ---
        // 実際に計算機を動かして、貢献度ベクトルを計算させる
        SimpleMatrix result = calculator.calculate(matrix);

        // --- 検証 (Then) ---
        // 計算結果が、期待される値とほぼ一致するかをチェックする
        // べき乗法の結果は近似値なので、厳密な一致ではなく「非常に近い」ことを確認する

        // 期待される貢献度の比率（手計算や別のツールで求めた理論値）
        // この行列の場合、おおよそ (A:0.35, B:0.34, C:0.31) の比率になる
        double expectedA = 0.551;
        double expectedB = 0.540;
        double expectedC = 0.635;

        // 実際の計算結果から値を取り出す
        double actualA = result.get(0, 0);
        double actualB = result.get(1, 0);
        double actualC = result.get(2, 0);

        // AssertJというライブラリを使い、期待値と実際値が「非常に近い」ことを表明(assert)する
        // 0.01の誤差は許容する
        assertThat(actualA).isCloseTo(expectedA, assertThat(0.01));
        assertThat(actualB).isCloseTo(expectedB, assertThat(0.01));
        assertThat(actualC).isCloseTo(expectedC, assertThat(0.01));
    }
}