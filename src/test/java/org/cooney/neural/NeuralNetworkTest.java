package org.cooney.neural;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.matrix.Matrix;
import org.junit.Assert;
import org.junit.Test;

public class NeuralNetworkTest {
    @Test
    public void testInitialisationOfMatrices() {
        int inputValuesCount = 5;
        int hiddenValuesCount = 4;
        int outputValuesCount = 3;

        NeuralNetwork nn = new NeuralNetwork(inputValuesCount, hiddenValuesCount, outputValuesCount, 0.01);

        Matrix hiddenToOutputLayerWeights = nn.getHiddenOutputLayerWeights();
        Matrix inputToHiddenLayerWeights = nn.getInputHiddenLayerWeights();

        // Ensure that the weights are set up to be dot product compatible.
        Assert.assertEquals(outputValuesCount, hiddenToOutputLayerWeights.getRows(), 0);
        Assert.assertEquals(hiddenValuesCount, hiddenToOutputLayerWeights.getColumns(), 0);
        Assert.assertEquals(inputValuesCount, inputToHiddenLayerWeights.getRows(), 0);
        Assert.assertEquals(hiddenValuesCount, inputToHiddenLayerWeights.getColumns(), 0);
    }
}