package org.cooney.matrix;

import org.junit.Assert;
import org.junit.Test;

public class MatrixTest {

    @Test
    public void testMatrixInit() {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns, -1, 1);

        double[][] matrixData = m.getData();

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                Assert.assertTrue(String.format("%f is out of range", matrixData[x][y]), matrixData[x][y] >= -1 && matrixData[x][y] <= 1);
            }
        }
    }

    @Test
    public void testMatrixScalarAddSample() {
        int rows = 10, columns = 10;
        double delta = 5;
        Matrix m = new Matrix(rows, columns);

        double[][] matrixData = m.getData();

        double sample = matrixData[3][4];
        double expected = sample + delta;

        m.add(delta);

        Assert.assertEquals(expected, matrixData[3][4], 0);
    }

    @Test
    public void testMatrixScalarAddAll() {
        int rows = 10, columns = 10;
        double delta = 5;
        Matrix m = new Matrix(rows, columns, -1, 1);

        double[][] matrixData = m.getData();

        m.add(delta);

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                // Matrix Data is initialised to at MINIMUM -1 and at most 1, meaning if you add five, it will be somewhere between 4 and 6
                Assert.assertTrue(String.format("%f is out of range", matrixData[x][y]), matrixData[x][y] >= 4 && matrixData[x][y] <= 6);
            }
        }
    }

    @Test
    public void testMatrixMatrixAddValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] + m2.getData()[3][4];

        m.add(m2);
        Assert.assertEquals(expectedSample, m.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixAddNotValidShape() {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(3, 7);

        Assert.assertThrows("Invalid Shape for Matrix Add", InvalidMatrixShapeException.class, () -> m.add(m2));
    }

    @Test
    public void testMatrixScalarMultiplySample() {
        int rows = 10, columns = 10;
        double factor = 5;
        Matrix m = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] * factor;

        m.multiply(factor);

        Assert.assertEquals(expectedSample, m.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixMultiplyValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] * m2.getData()[3][4];

        m.multiply(m2);
        Assert.assertEquals(expectedSample, m.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixMultiplyNotValidShape() {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(3, 7);

        Assert.assertThrows("Invalid Shape for Matrix Add", InvalidMatrixShapeException.class, () -> m.multiply(m2));
    }

    @Test
    public void testMatrixScalarSubtractSample() {
        int rows = 10, columns = 10;
        double scalar = 5;
        Matrix m = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] - scalar;

        m.subtract(scalar);

        Assert.assertEquals(expectedSample, m.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixSubtractValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] - m2.getData()[3][4];

        m.subtract(m2);
        Assert.assertEquals(expectedSample, m.getData()[3][4], 0);
    }

    @Test
    public void testSigmoid() {
        int rows = 5, columns = 5;
        Matrix m = new Matrix(rows, columns);

        double expectedSigmoidOutput = 1/(1 + Math.exp(-m.getData()[3][4]));

        m.sigmoid();

        Assert.assertEquals(expectedSigmoidOutput, m.getData()[3][4], 0);
    }
}
