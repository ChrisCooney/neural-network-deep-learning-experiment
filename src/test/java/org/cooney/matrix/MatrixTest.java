package org.cooney.matrix;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

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
    public void testMatrixFromSeedArray() {
        double[] seedArray = new double[]{4,5,6,7};
        Matrix m = new Matrix(seedArray);

        Assert.assertEquals(seedArray[0], m.getData()[0][0], 0);
        Assert.assertEquals(seedArray[1], m.getData()[1][0], 0);
        Assert.assertEquals(seedArray[2], m.getData()[2][0], 0);
        Assert.assertEquals(seedArray[3], m.getData()[3][0], 0);

        Assert.assertEquals(m.getRows(), 4);
        Assert.assertEquals(m.getColumns(), 1);
    }

    @Test
    public void testMatrixScalarAddSample() {
        int rows = 10, columns = 10;
        double delta = 5;
        Matrix m = new Matrix(rows, columns);

        double[][] matrixData = m.getData();

        double sample = matrixData[3][4];
        double expected = sample + delta;

        double[][] output = m.add(delta).getData();

        Assert.assertEquals(expected, output[3][4], 0);
    }

    @Test
    public void testMatrixScalarAddAll() {
        int rows = 10, columns = 10;
        double delta = 5;
        Matrix m = new Matrix(rows, columns, -1, 1);

        double[][] output = m.add(delta).getData();

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                // Matrix Data is initialised to at MINIMUM -1 and at most 1, meaning if you add five, it will be somewhere between 4 and 6
                Assert.assertTrue(String.format("%f is out of range", output[x][y]), output[x][y] >= 4 && output[x][y] <= 6);
            }
        }
    }

    @Test
    public void testMatrixMatrixAddValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] + m2.getData()[3][4];

        Matrix outputMatrix = m.add(m2);
        Assert.assertEquals(expectedSample, outputMatrix.getData()[3][4], 0);
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

        Matrix outputMatrix = m.multiply(factor);

        Assert.assertEquals(expectedSample, outputMatrix.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixMultiplyValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] * m2.getData()[3][4];

        Matrix outputMatrix = m.multiply(m2);
        Assert.assertEquals(expectedSample, outputMatrix.getData()[3][4], 0);
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

        Matrix output = m.subtract(scalar);

        Assert.assertEquals(expectedSample, output.getData()[3][4], 0);
    }

    @Test
    public void testMatrixMatrixSubtractValidShape() throws InvalidMatrixShapeException {
        int rows = 10, columns = 10;
        Matrix m = new Matrix(rows, columns);
        Matrix m2 = new Matrix(rows, columns);

        double expectedSample = m.getData()[3][4] - m2.getData()[3][4];

        Matrix output = m.subtract(m2);
        Assert.assertEquals(expectedSample, output.getData()[3][4], 0);
    }

    @Test
    public void testSigmoid() {
        int rows = 5, columns = 5;
        Matrix m = new Matrix(rows, columns);

        double expectedSigmoidOutput = 1/(1 + Math.exp(-m.getData()[3][4]));

        Matrix output = m.sigmoid();

        Assert.assertEquals(expectedSigmoidOutput, output.getData()[3][4], 0);
    }

    @Test
    public void testDerivativeSigmoid() {
        int rows = 5, columns = 5;
        Matrix m = new Matrix(rows, columns);

        double expectedDerivativeSigmoidOutput = m.getData()[3][4] * (1 - m.getData()[3][4]);

        Matrix output = m.derivativeSigmoid();

        Assert.assertEquals(expectedDerivativeSigmoidOutput, output.getData()[3][4], 0);
    }

    @Test
    public void testMatrixTransposition() {
        Matrix m = new Matrix(8, 4, true);
        Matrix output = m.transpose();

        for(int x = 0; x < output.getRows(); x++) {
            for(int y = 0; y < output.getColumns(); y++) {
                Assert.assertEquals(m.getData()[y][x], output.getData()[x][y], 0);
            }
        }

        Assert.assertEquals(m.getColumns(), output.getRows(), 0);
        Assert.assertEquals(m.getRows(), output.getColumns(), 0);
    }

    @Test
    public void testPerformDotProductValidShape() throws InvalidMatrixShapeException {
        int rows = 2, columns = 2;

        Matrix m1 = new Matrix(rows, columns, true);
        Matrix m2 = new Matrix(rows, columns, true);

        double expectedSample = (m1.getData()[0][0] * m2.getData()[0][0]) + (m1.getData()[0][1] * m2.getData()[1][0]);

        Matrix output = m1.dotProduct(m2);

        Assert.assertEquals(expectedSample, output.getData()[0][0], 0);
    }

    @Test
    public void testPerformDotProductInvalidShape() {
        Matrix m1 = new Matrix(5, 19, true);
        Matrix m2 = new Matrix(7, 12, true);

        Assert.assertThrows(
                "Columns of m1 must equal rows of m2 for dot product. m1.columns = 3 & m2.rows = 7",
                InvalidMatrixShapeException.class,
                () -> m1.dotProduct(m2)
        );
    }

    @Test
    public void testToFlatArray() {
        Matrix m1 = new Matrix(2, 2, true);
        double[][] data = m1.getData();

        double[] expectedOutput = new double[]{data[0][0], data[0][1], data[1][0], data[1][1]};
        double[] output = m1.toFlatArray();

        Assert.assertArrayEquals(expectedOutput, output, 0);
    }
}
