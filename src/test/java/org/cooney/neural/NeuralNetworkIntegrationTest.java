package org.cooney.neural;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.junit.Test;

import java.util.Arrays;

public class NeuralNetworkIntegrationTest {
    @Test
    public void xorIntegrationTest() throws InvalidTrainingDataException, InvalidMatrixShapeException {
        NeuralNetwork xorNetwork = new NeuralNetwork(2, 10, 1, 0.01);

        double[][] validInputs = {
                {0,0},{1,0},{0,1},{1,1}
        };

        double[][] validOutputs = {
                {0},{1},{1},{0}
        };

        NeuralNetworkTrainingData neuralNetworkTrainingData = new NeuralNetworkTrainingData(validInputs, validOutputs);

        xorNetwork.fit(neuralNetworkTrainingData, 50000);

        for(double[] validInput: validInputs) {
            System.out.println(Arrays.toString(xorNetwork.predict(validInput)));
        }
    }
}
