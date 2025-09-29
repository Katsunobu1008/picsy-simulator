package com.example.picsy_engine.util;

/**
 * 行列の補助関数をまとめたユーティリティ。
 * - copy: 二次元配列のディープコピー
 * - normalizeRowsInPlace: 各行を非負化し、行和=1に正規化
 */
public final class MatrixUtils {
    private MatrixUtils(){}

    /** 2D 配列のディープコピー */
    public static double[][] copy(double[][] src){
        int n = src.length; double[][] dst = new double[n][n];
        for (int i=0;i<n;i++) System.arraycopy(src[i], 0, dst[i], 0, n);
        return dst;
    }

    /** 各行を非負＆行和=1に正規化する（ゼロ行は例外） */
    public static void normalizeRowsInPlace(double[][] m){
        int n = m.length;
        for (int i=0;i<n;i++){
            double s=0;
            for (int j=0;j<n;j++){
                m[i][j] = Math.max(0.0, m[i][j]);
                s += m[i][j];
            }
            if (s<=0) throw new IllegalArgumentException("Row "+i+" sum is zero");
            for (int j=0;j<n;j++) m[i][j] /= s;
        }
    }
}
