package org.cooney.matrix;

import java.util.ArrayList;
import java.util.List;

public class Matrix {
    private double[][] data;
    private final int rows;
    private final int columns;

    protected Matrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.data = new double[rows][columns];
    }

    public Matrix(int rows, int columns, boolean autoSeed) {
        this(rows, columns);

        if (autoSeed) {
            randomlyInitialiseData(rows, columns, -1, 1);
        }
    }

    public Matrix(int rows, int columns, int seedValueMinimum, int seedValueMaximum) {
        this(rows, columns);
        randomlyInitialiseData(rows, columns, seedValueMinimum, seedValueMaximum);
    }

    public Matrix(double[] seedArray) {
        this(seedArray.length, 1);

        for(int x = 0; x < seedArray.length; x++) {
            this.data[x][0] = seedArray[x];
        }
    }

    public Matrix(int rows, int columns, double[][] data) {
        this(rows, columns, false);

        this.data = new double[data.length][data[0].length];

        for(int x = 0; x < this.data.length; x++) {
            for(int y = 0; y < this.data[x].length; y++) {
                this.data[x][y] = data[x][y];
            }
        }
    }

    public Matrix add(double scalar) {
        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] + scalar;
            }
        }

        return outputMatrix;
    }

    public Matrix add(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException("Invalid Shape for Matrix Add");
        }

        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] + m.getData()[x][y];
            }
        }

        return outputMatrix;
    }

    public Matrix multiply(double factor) {
        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] * factor;
            }
        }

        return outputMatrix;
    }

    public Matrix multiply(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException(String.format("Invalid Shape for Matrix Multiply - This = [%d, %d], m = [%d, %d]", this.rows, this.columns, m.getRows(), m.getColumns()));
        }

        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] * m.getData()[x][y];
            }
        }

        return outputMatrix;
    }

    public Matrix subtract(double scalar) {
        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] - scalar;
            }
        }

        return outputMatrix;
    }

    public Matrix subtract(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException("Invalid Shape for Matrix Add");
        }

        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] - m.getData()[x][y];
            }
        }

        return outputMatrix;
    }

    public Matrix sigmoid() {
        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                outputMatrix.getData()[x][y] = 1/(1 +Math.exp(-this.data[x][y]));
            }
        }

        return outputMatrix;
    }

    public Matrix derivativeSigmoid() {
        Matrix outputMatrix = new Matrix(this.rows, this.columns);

        for(int x = 0; x < outputMatrix.getRows(); x++) {
            for(int y = 0; y < outputMatrix.getColumns(); y++) {
                outputMatrix.getData()[x][y] = this.data[x][y] * (1 - this.data[x][y]);
            }
        }

        return outputMatrix;
    }

    public Matrix transpose() {
        Matrix output = new Matrix(this.columns, this.rows);

        for(int x = 0; x < this.getRows(); x++) {
            for(int y = 0; y < this.getColumns(); y++) {
                output.getData()[y][x] = this.data[x][y];
            }
        }

        return output;
    }

    public Matrix dotProduct(Matrix m2) throws InvalidMatrixShapeException {
        if (!isMatricesDefined(this, m2)) {
            throw new InvalidMatrixShapeException(String.format("Columns of m1 must equal rows of m2 for dot product. m1.columns = %d & m2.rows = %d", this.columns, m2.getRows()));
        }

        Matrix outputMatrix = new Matrix(this.rows, m2.getColumns(), true);

        for(int x = 0; x < outputMatrix.getRows(); x++) {
            for(int y = 0; y < outputMatrix.getColumns(); y++) {
                double total = 0;
                for(int k = 0; k < this.columns; k++) {
                    total += this.data[x][k] * m2.getData()[k][y];
                }
                outputMatrix.getData()[x][y] = total;
            }
        }

        return outputMatrix;
    }

    private static boolean isMatricesDefined(Matrix m1, Matrix m2) {
        return m1.getColumns() == m2.getRows();
    }

    private void randomlyInitialiseData(int rows, int cols, int seedMin, int seedMax) {
        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < cols; y++) {
                this.data[x][y] = Math.random()*(seedMax - seedMin) + seedMin;
            }
        }
    }

    public double[][] getData() {
        return data;
    }

    public int getRows() {
        return rows;
    }

    public int getColumns() {
        return columns;
    }

    public double[] toFlatArray() {
        double[] outputArray = new double[rows * columns];

        int pointer = 0;
        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                outputArray[pointer++] = this.data[x][y];
            }
        }

        return outputArray;
    }

    public Matrix copy() {
        return new Matrix(this.rows, this.columns, this.data);
    }
}
