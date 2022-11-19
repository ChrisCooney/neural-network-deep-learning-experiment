package org.cooney.matrix.utils;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.matrix.Matrix;
import org.junit.Assert;
import org.junit.Test;

public class MatrixArithmeticUtilsTest {

    @Test
    public void testPerformDotProductValidShape() throws InvalidMatrixShapeException {
        int rows = 2, columns = 2;

        Matrix m1 = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = (m1.getData()[0][0] * m2.getData()[0][0]) + (m1.getData()[0][1] * m2.getData()[1][0]);

        Matrix output = MatrixArithmeticUtils.performDotProduct(m1, m2);

        Assert.assertEquals(expectedSample, output.getData()[0][0], 0);
    }

    @Test
    public void testPerformDotProductInvalidShape() {
        Matrix m1 = new Matrix(5, 19);
        Matrix m2 = new Matrix(7, 12);

        Assert.assertThrows(
                "Columns of m1 must equal rows of m2 for dot product. m1.columns = 3 & m2.rows = 7",
                InvalidMatrixShapeException.class,
                () -> MatrixArithmeticUtils.performDotProduct(m1, m2)
        );
    }

    @Test
    public void testMatrixTransposition() {
        Matrix m = new Matrix(4, 4);
        Matrix output = MatrixArithmeticUtils.performTransposition(m);

        for(int x = 0; x < 4; x++) {
            for(int y = 0; y < 4; y++) {
                Assert.assertEquals(m.getData()[x][y], output.getData()[y][x], 0);
            }
        }
    }
}