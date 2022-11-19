package org.cooney.matrix.utils;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.matrix.Matrix;

public final class MatrixArithmeticUtils {
    public static Matrix performDotProduct(Matrix m1, Matrix m2) throws InvalidMatrixShapeException {
        if (!isMatricesDefined(m1, m2)) {
            throw new InvalidMatrixShapeException(String.format("Columns of m1 must equal rows of m2 for dot product. m1.columns = %d & m2.rows = %d", m1.getColumns(), m2.getRows()));
        }

        Matrix outputMatrix = new Matrix(m1.getRows(), m2.getColumns());

        for(int x = 0; x < outputMatrix.getRows(); x++) {
            for(int y = 0; y < outputMatrix.getColumns(); y++) {
                double total = 0;
                for(int k = 0; k < m1.getColumns(); k++) {
                    total += m1.getData()[x][k] * m2.getData()[k][y];
                }
                outputMatrix.getData()[x][y] = total;
            }
        }

        return outputMatrix;
    }

    public static Matrix performTransposition(Matrix m) {
        Matrix output = new Matrix(m.getRows(), m.getColumns());

        for(int x = 0; x < output.getRows(); x++) {
            for(int y = 0; y < output.getColumns(); y++) {
                output.getData()[x][y] = m.getData()[y][x];
            }
        }

        return output;
    }

    private static boolean isMatricesDefined(Matrix m1, Matrix m2) {
        return m1.getColumns() == m2.getRows();
    }

}
