// このファイルがどのパッケージに属しているか
package com.example.picsyengine;

// テストで使う道具をインポートする
import org.ejml.simple.SimpleMatrix; // メインコードと同じ行列ライブラリ
import org.junit.jupiter.api.Test; // JUnit 5の「これはテストです」という宣言

import static org.assertj.core.api.Assertions.*; // より直感的に結果を検証するAssertJライブラリ

/**
 * ContributionCalculatorクラスの品質を検査するためのテストクラス。
 */
class ContributionCalculatorTest {

    // @Testアノテーション：JUnitに対して「このメソッドは独立したテストケースです」と教える印
    @Test
    void calculate_shouldReturnCorrectContributionVector_forSimple3x3Matrix() {
        // --- 1. Arrange (準備) ---
        // テストの前提条件や、テスト対象のオブジェクトを準備するフェーズ。

        // テストで使うための、結果が分かっている単純な評価行列を作成する。
        // A -> (B: 0.9, C: 0.1), B -> (A: 0.9, C: 0.1), C -> (A:0.1, B:0.1)
        // 自己評価は簡単のため0とする。
        SimpleMatrix evaluationMatrix = new SimpleMatrix(new double[][]{
                {0.0, 0.9, 0.1}, // Aさんの評価 (Aへ, Bへ, Cへ)
                {0.9, 0.0, 0.1}, // Bさんの評価
                {0.1, 0.1, 0.8}  // Cさんの評価
        });

        // テスト対象である「貢献度計算機」のインスタンスを作成する。
        ContributionCalculator calculator = new ContributionCalculator();


        // --- 2. Act (実行) ---
        // 実際にテストしたい機能を実行するフェーズ。

        // 準備した評価行列を渡して、貢献度計算メソッドを呼び出し、結果を取得する。
        SimpleMatrix actualVector = calculator.calculate(evaluationMatrix);


        // --- 3. Assert (検証) ---
        // 実行結果が、私たちが期待した通りの正しい値になっているかを検証するフェーズ。

        // この評価行列の場合、貢献度は最終的にこうなるはず、という期待値を定義する。
        // (この値は事前に手計算や別ツールで計算しておく)
        // 合計がN=3になるように正規化されているはず。
        double expectedA = 1.052;
        double expectedB = 1.052;
        double expectedC = 0.895;

        // 検証①: ベクトルのサイズが正しいか？ (メンバーは3人なので 3x1 のはず)
        assertThat(actualVector.numRows()).isEqualTo(3);
        assertThat(actualVector.numCols()).isEqualTo(1);

        // 検証②: 各メンバーの貢献度の値が期待値とほぼ等しいか？
        // 浮動小数点の計算は完全な一致が難しいため、「0.01の誤差の範囲で等しい」ことを検証する。
        assertThat(actualVector.get(0, 0)).isCloseTo(expectedA, offset(0.01)); // Aさんの貢献度
        assertThat(actualVector.get(1, 0)).isCloseTo(expectedB, offset(0.01)); // Bさんの貢献度
        assertThat(actualVector.get(2, 0)).isCloseTo(expectedC, offset(0.01)); // Cさんの貢献度

        // 検証③: 貢献度の合計がメンバー数(N=3)とほぼ等しいか？
        assertThat(actualVector.elementSum()).isCloseTo(3.0, offset(0.01));
    }
}
