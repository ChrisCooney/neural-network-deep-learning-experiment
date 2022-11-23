package org.cooney.neural;

import org.junit.Assert;
import org.junit.Test;

public class NeuralNetworkTrainingDataTest {
    @Test
    public void testTrainingDataValidationInputLength() {
        double[][] inputLength1 = new double[][]{{0, 0}};
        double[] scores = new double[]{2};
        double[][] newGridItems = new double[1][2];
        int[] actionsTaken = new int[1];
        Assert.assertThrows("", InvalidTrainingDataException.class, () -> new NeuralNetworkTrainingData(inputLength1, scores, newGridItems, actionsTaken));
    }

    @Test
    public void testLengthGet() throws InvalidTrainingDataException {
        double[][] input = new double[][]{{0}};
        double[] scores = new double[]{1};
        double[][] newGridItems = new double[1][8];
        int[] actionsTaken = new int[1];

        NeuralNetworkTrainingData trainingData = new NeuralNetworkTrainingData(input, scores, newGridItems, actionsTaken);

        Assert.assertEquals(1, trainingData.getDataSize(), 0);
    }

    @Test
    public void testGetInputAndOutputAtIndex() throws InvalidTrainingDataException {
        double[][] input = new double[][]{{0}};
        double[][] output = new double[][]{{1}};
        double[] scores = new double[]{1};
        double[][] newGridItems = new double[1][8];
        int[] actionsTaken = new int[]{1};

        NeuralNetworkTrainingData trainingData = new NeuralNetworkTrainingData(input, scores, newGridItems, actionsTaken);

        Assert.assertArrayEquals(new double[]{0}, trainingData.getInputAtIndex(0), 0);
        Assert.assertEquals(1, trainingData.getActionAtIndex(0), 0);
    }
}