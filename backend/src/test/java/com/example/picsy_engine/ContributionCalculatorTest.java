package com.example.picsy_engine;

import org.ejml.simple.SimpleMatrix;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * ContributionCalculatorクラスの品質を検査するためのテストクラス。
 */
class ContributionCalculatorTest {

    @Test
    void calculate_shouldReturnCorrectContributionVector_forAsymmetricMatrix() {
        // --- 1. Arrange (準備) ---
        // 貢献度に差がつく、より現実的な非対称な評価行列を準備する。
        SimpleMatrix evaluationMatrix = new SimpleMatrix(new double[][]{
                {0.1, 0.7, 0.2}, // Aさん -> (A:0.1, B:0.7, C:0.2)
                {0.6, 0.1, 0.3}, // Bさん -> (A:0.6, B:0.1, C:0.3)
                {0.2, 0.2, 0.6}  // Cさん -> (A:0.2, B:0.2, C:0.6)
        });

        ContributionCalculator calculator = new ContributionCalculator();

        // --- 2. Act (実行) ---
        SimpleMatrix actualVector = calculator.calculate(evaluationMatrix);

        // --- 3. Assert (検証) ---
        // 上記の行列から計算される、正しい貢献度の期待値を定義する。
        // 合計がN=3になるように正規化されているはず。
        double expectedA = 1.043;
        double expectedB = 1.068;
        double expectedC = 0.889;

        // 検証①: ベクトルのサイズが正しいか？
        assertThat(actualVector.numRows()).isEqualTo(3);
        assertThat(actualVector.numCols()).isEqualTo(1);

        // 検証②: 各メンバーの貢献度の値が、新しい期待値とほぼ等しいか？
        assertThat(actualVector.get(0, 0)).isCloseTo(expectedA, offset(0.01)); // Aさんの貢献度
        assertThat(actualVector.get(1, 0)).isCloseTo(expectedB, offset(0.01)); // Bさんの貢献度
        assertThat(actualVector.get(2, 0)).isCloseTo(expectedC, offset(0.01)); // Cさんの貢献度

        // 検証③: 貢献度の合計がメンバー数(N=3)とほぼ等しいか？
        assertThat(actualVector.elementSum()).isCloseTo(3.0, offset(0.01));
    }
}
