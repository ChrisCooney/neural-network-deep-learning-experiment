package org.cooney.neural;

public class NeuralNetworkTrainingData {
    private final double[][] validInputs;
    private final double[][] validOutputs;

    public NeuralNetworkTrainingData(double[][] validInputs, double[][] validOutputs) throws InvalidTrainingDataException {
        if (validInputs.length != validOutputs.length) {
            throw new InvalidTrainingDataException();
        }

        this.validInputs = validInputs;
        this.validOutputs = validOutputs;
    }

    public double[] getInputAtIndex(int index) {
        return validInputs[index];
    }

    public double[] getOutputAtIndex(int index) {
        return validOutputs[index];
    }

    public int getDataSize() {
        return validInputs.length;
    }

    public double[][] getValidInputs() {
        return validInputs;
    }

    public double[][] getValidOutputs() {
        return validOutputs;
    }
}

