package org.cooney.world.items.agents;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.neural.NeuralNetwork;
import org.cooney.world.WorldEngine;
import org.cooney.world.items.Actor;
import org.cooney.world.items.Learner;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.map.GridItem;

import java.util.ArrayList;
import java.util.List;

public abstract class LivingEntity implements Actor, Learner, WorldItem {
    protected final double learningDegradationRate;
    protected final int meditationCadenceInTicks;
    protected final int explorationDegradeCadenceInTicks;
    protected final int maxMemorySize;
    protected double explorationRate = 0.95;
    protected int ticks = 0;
    protected Direction currentDirection = Direction.DOWN;
    protected Direction previousDirection = Direction.DOWN;
    protected boolean alive = true;
    protected final NeuralNetwork neuralNetwork;
    protected final List<LivingThingMemory> memory = new ArrayList<>();
    protected final WorldEngine outsideWorld;

    public LivingEntity(double learningDegradationRate, int meditationCadenceInTicks, int explorationDegradeCadenceInTicks, int maxMemorySize, NeuralNetwork neuralNetwork, WorldEngine outsideWorld, int ticks, double initialExplorationRate) {
        this.learningDegradationRate = learningDegradationRate;
        this.meditationCadenceInTicks = meditationCadenceInTicks;
        this.explorationDegradeCadenceInTicks = explorationDegradeCadenceInTicks;

        this.maxMemorySize = maxMemorySize;
        this.neuralNetwork = neuralNetwork;
        this.outsideWorld = outsideWorld;
        this.explorationRate = initialExplorationRate;
    }

    public void wakeUp() {
        while(alive) {
            List<GridItem> gridItemsICanSee = outsideWorld.getGridItemsInActorLineOfSight(this);
            act(gridItemsICanSee);
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                // it's okay if the thread is interrupted. Catch and swallow.
            }
        }

        outsideWorld.cleanUpCorpse(this);
    }

    protected void degradeExplorationRate() {
        if (this.explorationRate > 0.05) {
            this.explorationRate = this.explorationRate - 0.05;
        }
    }

    protected double[] gridItemsToNetworkInput(List<GridItem> gridItems) {
        return gridItems.stream()
                .map(GridItem::getWorldItem)
                .map(WorldItem::getWorldItemId)
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    protected void rememberThisDecision(double[] input, double[] inputStats, double moveScore, double[] newSurroundingItems, double[] newStats, Direction direction) {
        this.memory.add(new LivingThingMemory(input, inputStats, moveScore, newSurroundingItems, newStats, direction));

        if (this.memory.size() > maxMemorySize) {
            // Get rid of the oldest memory from the working set.
            this.memory.remove(0);
        }
    }

    protected List<GridItem> lookAround() {
        return outsideWorld.getGridItemsInActorLineOfSight(this);
    }

    protected void move(Direction direction) {
        int xDirection = direction.getXDirection();
        int yDirection = direction.getYDirection();

        outsideWorld.moveActor(this, xDirection, yDirection);
    }

    private boolean shouldSelectRandomDirection() {
        return Math.random() < this.explorationRate;
    }

    protected Direction decide(double[] networkInput) throws InvalidMatrixShapeException {
        if (shouldSelectRandomDirection()) {
            return Direction.randomDirection();
        }

        double[] possibleQValues = neuralNetwork.predict(networkInput);

        int indexOfMax = -1;
        double temp = -1;

        for(int x = 0; x < possibleQValues.length; x++) {
            if (possibleQValues[x] > temp) {
                indexOfMax = x;
                temp = possibleQValues[x];
            }
        }

        return Direction.getFromIndex(indexOfMax);
    }

    @Override
    public Direction getDirectionIamFacing() {
        if (currentDirection == Direction.STAY_STILL) {
            return previousDirection;
        } else {
            return currentDirection;
        }
    }

    public boolean isAlive() {
        return alive;
    }

    public NeuralNetwork getNeuralNetwork() {
        return this.neuralNetwork.copy();
    }

    public int getTicks() {
        return ticks;
    }

    public void act(List<GridItem> gridItems) {
        double[] input = gridItemsToNetworkInput(gridItems);

        ticks ++;

        try {
            if (alive) {
                if (ticks % meditationCadenceInTicks == 0) {
                    learn();
                } else {
                    makeAMove(input);
                    if (this.isDead()) {
                        System.out.println("I AM DEAD!");
                        alive = false;
                    }
                }

                if (ticks % explorationDegradeCadenceInTicks == 0) {
                    degradeExplorationRate();
                }
            }
        } catch (InvalidMatrixShapeException | InvalidTrainingDataException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public double getWorldItemId() {
        return alive? WorldItemIds.LIVING_THING_ID : WorldItemIds.CORPSE;
    }

    protected abstract void makeAMove(double[] input) throws InvalidMatrixShapeException;
    protected abstract boolean isDead();
}
