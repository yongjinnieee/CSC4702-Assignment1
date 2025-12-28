package com.csc4702;

// 3x3 matrix for 2D homogeneous transformations
public class Matrix3x3 {
    private double[][] data;

    public Matrix3x3(boolean isIdentity) {
        data = new double[3][3];
        if (isIdentity) {
            data[0][0] = 1;
            data[1][1] = 1;
            data[2][2] = 1;
        }
    }

    public void setValue(int row, int col, double value) {
        data[row][col] = value;
    }

    public double getValue(int row, int col) {
        return data[row][col];
    }

    public Matrix3x3 multiply(Matrix3x3 other) {
        Matrix3x3 result = new Matrix3x3(false);
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                for (int k = 0; k < 3; k++) {
                    result.data[i][j] += this.data[i][k] * other.data[k][j];
                }
            }
        }
        return result;
    }
}
