package org.cooney.matrix;

public class Matrix {
    private final double[][] data;
    private final int rows;
    private final int columns;

    public Matrix(int rows, int columns) {
        this.rows = rows;
        this.columns = columns;
        this.data = new double[rows][columns];

        randomlyInitialiseData(rows, columns, -1, 1);
    }

    public Matrix(int rows, int columns, int seedValueMinimum, int seedValueMaximum) {
        this.rows = rows;
        this.columns = columns;
        this.data = new double[rows][columns];

        randomlyInitialiseData(rows, columns, seedValueMinimum, seedValueMaximum);
    }

    public void add(double scalar) {
        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                this.data[x][y] += scalar;
            }
        }
    }

    public void add(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException("Invalid Shape for Matrix Add");
        }

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                this.data[x][y] += m.getData()[x][y];
            }
        }
    }

    public void multiply(double factor) {
        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                this.data[x][y] *= factor;
            }
        }
    }

    public void multiply(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException("Invalid Shape for Matrix Add");
        }

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                this.data[x][y] *= m.getData()[x][y];
            }
        }
    }

    public void subtract(double scalar) {
        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                this.data[x][y] -= scalar;
            }
        }
    }

    public void subtract(Matrix m) throws InvalidMatrixShapeException {
        if (m.getRows() != this.rows || m.getColumns() != this.columns){
            throw new InvalidMatrixShapeException("Invalid Shape for Matrix Add");
        }

        for(int x = 0; x < rows; x++) {
            for(int y = 0; y < columns; y++) {
                this.data[x][y] -= m.getData()[x][y];
            }
        }
    }

    public void sigmoid() {
        for(int x = 0; x < this.rows; x++) {
            for(int y = 0; y < this.columns; y++) {
                this.data[x][y] = 1/(1 +Math.exp(-this.data[x][y]));
            }
        }
    }

    public void performDerivativeSigmoid(Matrix m) {
        Matrix output = new Matrix(m.getRows(), m.getColumns());

        for(int x = 0; x < output.getRows(); x++) {
            for(int y = 0; y < output.getColumns(); y++) {
                output.getData()[x][y] = output.getData
            }
        }
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

    public Matrix clone() throws CloneNotSupportedException {
        Matrix m = (Matrix) super.clone();
        double[][] clonedData = this.data.clone();


    }
}
