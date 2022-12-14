package org.cooney.neural;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.matrix.Matrix;

import java.util.Arrays;

public class NeuralNetwork {

    private Matrix inputHiddenLayerWeights;
    private Matrix hiddenOutputLayerWeights;
    private Matrix hiddenLayerBias;
    private Matrix outputLayerBias;

    private final double learningRate;

    /**
     * Auto-generate constructor that will build the Matrices in the correct way, using desired input values.
     * @param inputValuesCount The number of inputs to push into the neural network
     * @param hiddenValuesCount The number of hidden nodes in the neural network
     * @param outputValuesCount The number of outputs from the neural network.
     * @param learningRate The sensitivity of the Neural Network to errors in predictions when training.
     */
    public NeuralNetwork(int inputValuesCount, int hiddenValuesCount, int outputValuesCount, double learningRate) {
        inputHiddenLayerWeights = new Matrix(hiddenValuesCount, inputValuesCount, true);
        hiddenOutputLayerWeights = new Matrix(outputValuesCount, hiddenValuesCount, true);
        hiddenLayerBias = new Matrix(hiddenValuesCount, 1, true);
        outputLayerBias = new Matrix(outputValuesCount, 1, true);
        this.learningRate = learningRate;
    }

    public NeuralNetwork(Matrix inputHiddenLayerWeights, Matrix hiddenOutputLayerWeights, Matrix hiddenLayerBias, Matrix outputLayerBias, double learningRate) {
        this.inputHiddenLayerWeights = inputHiddenLayerWeights;
        this.hiddenOutputLayerWeights = hiddenOutputLayerWeights;
        this.hiddenLayerBias = hiddenLayerBias;
        this.outputLayerBias = outputLayerBias;
        this.learningRate = learningRate;
    }

    /**
     * This function uses the existing weights in the Neural Network to predict an outcome.
     * @param inputs The desired input parameters
     * @return The predicted output parameters
     * @throws InvalidMatrixShapeException If the inputs can't be matrix multiplied (dot product) with the input -> hidden layer weights
     */
    public double[] predict(double[] inputs) throws InvalidMatrixShapeException {
        Matrix inputsMatrix = new Matrix(inputs);
        Matrix finalOutput = predictMatrix(inputsMatrix);
        return finalOutput.toFlatArray();
    }

    private Matrix predictMatrix(Matrix inputsMatrix) throws InvalidMatrixShapeException {
        Matrix hiddenOutput = feedForwardInputToHidden(inputsMatrix);
        return feedForwardHiddenToOutput(hiddenOutput);
    }

    private Matrix feedForwardInputToHidden(Matrix inputsMatrix) throws InvalidMatrixShapeException {
        return inputHiddenLayerWeights
                .dotProduct(inputsMatrix)
                .add(hiddenLayerBias)
                .sigmoid();
    }

    private Matrix feedForwardHiddenToOutput(Matrix hiddenOutput) throws InvalidMatrixShapeException {
        return hiddenOutputLayerWeights
                .dotProduct(hiddenOutput)
                .add(outputLayerBias)
                .sigmoid();
    }


    private void qLearning(double[] oldState, double score, double[] newState, int action) throws InvalidMatrixShapeException {
        // Then we estimate the Q Value for the next action we're going to take
        double[] estimatedQValuesFromOldState = predict(oldState);
        double[] expectedQValueForNextAction = predict(newState);

        double discount = 0.95;

        estimatedQValuesFromOldState[action] = score + (discount * Arrays.stream(expectedQValueForNextAction).max().getAsDouble());

        train(oldState, estimatedQValuesFromOldState);
    }

    /**
     * Updates the weights in the network based on the error between the output and the target.
     *
     * @param input The input from the last feedforward run.
     * @param target The target to on which to train the network.
     *
     * @throws InvalidMatrixShapeException The input or target is not in the right format for dot product.
     */
    public void train(double[] input, double[] target) throws InvalidMatrixShapeException {
        Matrix inputsMatrix = new Matrix(input);

        Matrix hiddenOutput = feedForwardInputToHidden(inputsMatrix);
        Matrix output = feedForwardHiddenToOutput(hiddenOutput);

        Matrix targetMatrix = new Matrix(target);
        Matrix errorFromHiddenToOutputLayer = targetMatrix.subtract(output);

        Matrix gradient = output
                .derivativeSigmoid()
                .multiply(errorFromHiddenToOutputLayer)
                .multiply(learningRate);

        Matrix transposedHiddenValues = hiddenOutput.transpose();
        Matrix hiddenToOutputWeightChange = gradient.dotProduct(transposedHiddenValues);

        this.hiddenOutputLayerWeights = this.hiddenOutputLayerWeights.add(hiddenToOutputWeightChange);
        this.outputLayerBias = this.outputLayerBias.add(gradient);

        Matrix transposedHiddenOutputWeights = hiddenOutputLayerWeights.transpose();
        Matrix errorFromInputToHiddenLayer = transposedHiddenOutputWeights.dotProduct(errorFromHiddenToOutputLayer);

        Matrix hiddenGradient = hiddenOutput
                .derivativeSigmoid()
                .multiply(errorFromInputToHiddenLayer)
                .multiply(learningRate);

        Matrix transposedInput = inputsMatrix.transpose();
        Matrix inputToHiddenWeightChange = hiddenGradient.dotProduct(transposedInput);

        this.inputHiddenLayerWeights = this.inputHiddenLayerWeights.add(inputToHiddenWeightChange);
        this.hiddenLayerBias = this.hiddenLayerBias.add(hiddenGradient);
    }

    /**
     * This trains the neural network. You fit the network to the data before attempting predictions.
     * @param trainingData an object containing neural network training data for fitness to work on this network.
     * @param epochs The number of times to train the network, i.e. the number of iterations.
     */
    public void fit(NeuralNetworkTrainingData trainingData, int epochs) throws InvalidMatrixShapeException {
        for(int iterationCount = 0; iterationCount < epochs; iterationCount++) {
            int randomIndex = (int)(Math.random() * trainingData.getDataSize());

            double[] input = trainingData.getInputAtIndex(randomIndex);
            double[] newState = trainingData.getNewStateAtIndex(randomIndex);
            double score = trainingData.getRewardAtIndex(randomIndex);
            int action = trainingData.getActionAtIndex(randomIndex);

            this.qLearning(input, score, newState, action);
        }
    }

    public Matrix getInputHiddenLayerWeights() {
        return inputHiddenLayerWeights;
    }

    public Matrix getHiddenOutputLayerWeights() {
        return hiddenOutputLayerWeights;
    }

    public NeuralNetwork copy() {
        return new NeuralNetwork(this.inputHiddenLayerWeights.copy(), this.hiddenOutputLayerWeights.copy(), this.hiddenLayerBias.copy(), this.outputLayerBias.copy(), this.learningRate);
    }
}
