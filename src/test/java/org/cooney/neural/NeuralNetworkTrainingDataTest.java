package org.cooney.neural;

import org.junit.Assert;
import org.junit.Test;

public class NeuralNetworkTrainingDataTest {
    @Test
    public void testTrainingDataValidationInputLength() {
        double[][] inputLength1 = new double[][]{{0}};
        double[][] outputLength2 = new double[][]{{1}, {2}};
        Assert.assertThrows("", InvalidTrainingDataException.class, () -> new NeuralNetworkTrainingData(inputLength1, outputLength2));
    }

    @Test
    public void testLengthGet() throws InvalidTrainingDataException {
        double[][] input = new double[][]{{0}};
        double[][] output = new double[][]{{1}};

        NeuralNetworkTrainingData trainingData = new NeuralNetworkTrainingData(input, output);

        Assert.assertEquals(1, trainingData.getDataSize(), 0);
    }

    @Test
    public void testGetInputAndOutputAtIndex() throws InvalidTrainingDataException {
        double[][] input = new double[][]{{0}};
        double[][] output = new double[][]{{1}};

        NeuralNetworkTrainingData trainingData = new NeuralNetworkTrainingData(input, output);

        Assert.assertArrayEquals(new double[]{0}, trainingData.getInputAtIndex(0), 0);
        Assert.assertArrayEquals(new double[]{1}, trainingData.getOutputAtIndex(0), 0);
    }
}