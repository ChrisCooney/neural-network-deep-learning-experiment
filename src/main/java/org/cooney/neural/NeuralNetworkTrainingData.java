package org.cooney.neural;

public class NeuralNetworkTrainingData {
    private final double[][] validInputs;
    private final double[] rewards;

    private final double[][] newState;

    private final int[] actionsTaken;

    public NeuralNetworkTrainingData(double[][] validInputs, double[] rewards, double[][] newState, int[] actionsTaken) throws InvalidTrainingDataException {
        this.actionsTaken = actionsTaken;

        if (!(validInputs.length == newState.length && rewards.length == newState.length && actionsTaken.length == newState.length)) {
            throw new InvalidTrainingDataException();
        }

        this.validInputs = validInputs;
        this.rewards = rewards;
        this.newState = newState;
    }

    public double[] getInputAtIndex(int index) {
        return validInputs[index];
    }

    public double getRewardAtIndex(int index) {
        return rewards[index];
    }

    public double[] getNewStateAtIndex(int index) {
        return newState[index];
    }

    public int getActionAtIndex(int index) {
        return actionsTaken[index];
    }

    public int getDataSize() {
        return validInputs.length;
    }
}

