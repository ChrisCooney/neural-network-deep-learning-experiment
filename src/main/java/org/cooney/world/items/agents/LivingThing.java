package org.cooney.world.items.agents;

import com.googlecode.lanterna.TextColor;
import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.neural.NeuralNetwork;
import org.cooney.neural.NeuralNetworkTrainingData;
import org.cooney.world.WorldEngine;
import org.cooney.world.items.*;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class LivingThing implements Actor, WorldItem, Learner, Breeder {

    private static final int MEDITATION_CADENCE_IN_TICKS = 100;
    private static final int EXPLORATION_DEGRADE_CADENCE_IN_TICKS = 100;
    private static final int MAX_MEMORY_SIZE = 1000;
    private final NeuralNetwork neuralNetwork;
    private final List<LivingThingMemory> memory;
    private final WorldEngine outsideWorld;
    private double hunger;
    private double thirst;
    private double isolation;
    private double explorationRate = 0.95;
    private int ticks;

    private Direction currentDirection = Direction.DOWN;

    private Direction previousDirection = Direction.DOWN;

    private int totalScore;

    private boolean alive = true;

    private double energy;

    public LivingThing(WorldEngine outsideWorld) {
        neuralNetwork = new NeuralNetwork(7, 500, 5, 0.1);
        this.memory = new ArrayList<>();

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;
        this.ticks = 0;
        this.energy = 500;

        this.outsideWorld = outsideWorld;
    }

    public LivingThing(WorldEngine outsideWorld, double explorationRate, int ticks) {
        this.neuralNetwork = new NeuralNetwork(7, 500, 5, 0.1);;
        this.memory = new ArrayList<>();

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;

        this.ticks = ticks;
        this.energy = 500;

        this.outsideWorld = outsideWorld;

        this.explorationRate = explorationRate;
    }

    public LivingThing(WorldEngine outsideWorld, NeuralNetwork neuralNetwork, double explorationRate, int ticks) {
        this.neuralNetwork = neuralNetwork;
        this.memory = new ArrayList<>();

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;

        this.ticks = ticks;
        this.energy = 500;

        this.outsideWorld = outsideWorld;

        this.explorationRate = explorationRate;
    }

    public void wakeUp() {
        while(alive) {
            List<GridItem> gridItemsICanSee = outsideWorld.getGridItemsInActorLineOfSight(this);
            act(gridItemsICanSee);
            try {
                if (ticks < 100000) {
                    //Thread.sleep(0);
                } else {
                    Thread.sleep(50);
                }

            } catch (InterruptedException e) {
                // it's okay if the thread is interrupted. Catch and swallow.
            }
        }

        outsideWorld.cleanUpCorpse(this);
    }

    @Override
    public void act(List<GridItem> gridItems) {
        double[] input = gridItemsToNetworkInput(gridItems);

        ticks ++;

        try {
            if (alive) {
                if (ticks % MEDITATION_CADENCE_IN_TICKS == 0) {
                    learn();
                } else {
                    makeAMove(input);
                    if (hunger > 1000 || thirst > 1000) {
                        System.out.println("I AM DEAD!");
                        alive = false;
                    }
                }

                if (ticks % EXPLORATION_DEGRADE_CADENCE_IN_TICKS == 0) {
                    degradeExplorationRate();
                }
            }
        } catch (InvalidMatrixShapeException | InvalidTrainingDataException e) {
            throw new RuntimeException(e);
        }
    }

    private void degradeExplorationRate() {
        if (this.explorationRate > 0.05) {
            this.explorationRate = this.explorationRate - 0.05;
        }
    }

    private double[] gridItemsToNetworkInput(List<GridItem> gridItems) {
        return gridItems.stream()
                .map(GridItem::getWorldItem)
                .map(WorldItem::getWorldItemId)
                .mapToDouble(Double::doubleValue)
                .toArray();
    }

    private void makeAMove(double[] input) throws InvalidMatrixShapeException {
        Direction direction = decide(input);
        move(direction);

        // If it went from moving to staying still, keep the previous direction set to what it was already.
        // If they've gone from moving in one direction to another, track the previous direction.
        this.previousDirection = this.currentDirection == Direction.STAY_STILL ? this.previousDirection : this.currentDirection;
        this.currentDirection = direction;
        List<GridItem> surroundingGridItems = lookAround();
        List<GridItem> consumableGridItems = outsideWorld.getGridItemsActorCanInteractWith(this);
        double[] newStats = consumeResources(consumableGridItems);
        double moveScore = scoreTheMoveIMade(newStats);
        rememberThisDecision(input, moveScore, gridItemsToNetworkInput(surroundingGridItems), newStats, direction);
        updateMyStats(newStats);

        if (direction != Direction.STAY_STILL) {
            energy = energy - 1;
        } else {
            energy += 100;
        }
    }

    private void rememberThisDecision(double[] input, double moveScore, double[] newSurroundingItems, double[] newStats, Direction direction) {

        double[] statsArr = new double[]{this.hunger, this.thirst, this.isolation, this.energy};

        if (moveScore > 0) {
           // System.out.println("I made a move to " + direction.toString() + " and it scored " + moveScore + " and my stats were " + Arrays.toString(statsArr) + " and after the move they were " + Arrays.toString(newStats));
        }

        this.memory.add(new LivingThingMemory(input, statsArr, moveScore, newSurroundingItems, newStats, direction));

        if (this.memory.size() > MAX_MEMORY_SIZE) {
            // Get rid of the oldest memory from the working set.
            this.memory.remove(0);
        }
    }

    private void updateMyStats(double[] newStats) {
        this.hunger = newStats[0];
        this.thirst = newStats[1];
        this.isolation = newStats[2];
        this.energy = newStats[3];
    }

    private double scoreTheMoveIMade(double[] newStats) {
        double score = 0;

        boolean hungerIsPrimaryConcern = hunger > thirst && hunger > isolation && hunger > 100;
        boolean thirstIsPrimaryConcern = thirst > hunger && thirst > isolation && thirst > 100;
        boolean isolationIsPrimaryConcern = isolation > hunger && isolation > thirst && isolation > 100;

        final int primaryConcernScoreIncrease = 10;
        final int secondaryConcernScoreIncrease = 0;

        // If they deal with their primary concern, they get 10 points.
        if (hungerIsPrimaryConcern) {
            // Hunger is the primary motivator. Score based on that.
            if (newStats[0] < hunger) {
                // We reduced hunger and that was the goal! Yay!
                score += primaryConcernScoreIncrease;
            }
        } else if (hunger > 200 && newStats[0] < hunger) {
            // Hunger is a secondary concern, so give some points.
            score += secondaryConcernScoreIncrease;
        }

        if (thirstIsPrimaryConcern) {
            // Thirst is the most important...
            if (newStats[1] < thirst) {
                score += primaryConcernScoreIncrease;
            }
        } else if (thirst > 200 && newStats[1] < thirst) {
            score += secondaryConcernScoreIncrease;
        }

        if (isolationIsPrimaryConcern) {
            // Isolation is the most important...
            if (newStats[2] < isolation) {
                score += primaryConcernScoreIncrease;
            }
        } else if (isolation > 200 && newStats[2] < isolation) {
            score += secondaryConcernScoreIncrease;
        }

        totalScore += score;

        return score;
    }

    private double[] consumeResources(List<GridItem> gridItems) {
        // Look to see if there is food nearby.
        Map<Double, List<WorldItem>> mappedById = gridItems.stream()
                .map(GridItem::getWorldItem).collect(Collectors.groupingBy(WorldItem::getWorldItemId));

        double[] newStats = new double[4];

        newStats[3] = this.energy;

        if (!mappedById.containsKey(WorldItemIds.FOOD_ID)) {
            newStats[0] = this.hunger + 1;
        } else if (this.hunger > 100) {
            mappedById.get(WorldItemIds.FOOD_ID).stream()
                    .map(worldItem -> (Food)worldItem)
                    .max((f1, f2) -> (int) (f1.getResourceCount() - f2.getResourceCount()))
                    .ifPresent((bestFoodSource) -> {
                        if (bestFoodSource.getResourceCount() > 0) {
                            bestFoodSource.consume(this);
                            newStats[0] = this.hunger - 20;
                            newStats[3] = this.energy + 20;
                        }
                    });
        }

        if (!mappedById.containsKey(WorldItemIds.WATER_ID)) {
            newStats[1] = this.thirst + 1;
        } else if (this.thirst > 100) {
            mappedById.get(WorldItemIds.WATER_ID).stream()
                    .map(worldItem -> (Water) worldItem)
                    .max((w1, w2) -> (int) (w1.getResourceCount() - w2.getResourceCount()))
                    .ifPresent((bestWaterSource) -> {
                        if (bestWaterSource.getResourceCount() > 0) {
                            bestWaterSource.consume(this);
                            newStats[1] = this.thirst - 20;
                        }
                    });
        }

        if (!mappedById.containsKey(WorldItemIds.LIVING_THING_ID)) {
            newStats[2] = this.isolation + 1;
        } else {
            newStats[2] = this.isolation < 100 ? 0 : this.isolation - 100;
        }

        return newStats;
    }

    private List<GridItem> lookAround() {
        return outsideWorld.getGridItemsInActorLineOfSight(this);
    }

    private void move(Direction direction) {
        int xDirection = direction.getXDirection();
        int yDirection = direction.getYDirection();

        outsideWorld.moveActor(this, xDirection, yDirection);
    }

    private Direction decide(double[] networkInput) throws InvalidMatrixShapeException {

        if (energy == 0) {
            return Direction.STAY_STILL;
        }

        if (shouldSelectRandomDirection()) {
            return Direction.randomDirection();
        }

        double[] statsArray = new double[]{this.hunger, this.thirst, this.isolation, this.energy};

        double[] possibleQValues = neuralNetwork.predict(buildNeuralNetworkInputArray(networkInput, statsArray));

        int indexOfMax = -1;
        double temp = -1;

        for(int x = 0; x < possibleQValues.length; x++) {
            if (possibleQValues[x] > temp) {
                indexOfMax = x;
                temp = possibleQValues[x];
            }
        }

        //Direction d = Direction.getFromIndex(indexOfMax);

        //System.out.printf("I am deciding to go %s with Q Value of %f - My Stats are Hunger = %f, Thirst = %f, Isolation = %f%n", d.toString(), temp, this.hunger, this.thirst, this.isolation);

        return Direction.getFromIndex(indexOfMax);
    }

    private boolean shouldSelectRandomDirection() {
        return Math.random() < this.explorationRate;
    }

    private double[] buildNeuralNetworkInputArray(double[] surroundingItemsNetworkInput, double[] statsArray) {

        double priorityConcernValue = 0;

        if (statsArray[0] > statsArray[1] && statsArray[0] > statsArray[2]) {
            priorityConcernValue = 1;
        }

        if (statsArray[1] > statsArray[0] && statsArray[1] > statsArray[2]) {
            priorityConcernValue = 2;
        }

        if (statsArray[2] > statsArray[0] && statsArray[2] > statsArray[1]) {
            priorityConcernValue = 3;
        }

        return DoubleStream.concat(Arrays.stream(surroundingItemsNetworkInput), Arrays.stream(new double[]{priorityConcernValue, statsArray[3]})).toArray();
    }

    @Override
    public void learn() throws InvalidTrainingDataException, InvalidMatrixShapeException {
        if (memory.size() == 0) {
            // This poor soul has no memories. Allow them to continue to wander aimlessly in the dystopian abyss
            // that I have created. Vaya con dios my friend.
            return;
        }

        double[][] inputs = new double[memory.size()][memory.get(0).oldSurroundingItems().length + 1];
        double[] scores = new double[memory.size()];
        double[][] newSurroundingItems = new double[memory.size()][memory.get(0).newSurroundingItems().length + 1];
        int[] actionsTaken = new int[memory.size()];

        for (int x = 0; x < memory.size(); x++) {
            inputs[x] = buildNeuralNetworkInputArray(memory.get(x).oldSurroundingItems(), memory.get(x).stats());
            scores[x] = memory.get(x).score();
            newSurroundingItems[x] = buildNeuralNetworkInputArray(memory.get(x).newSurroundingItems(), memory.get(x).stats());
            actionsTaken[x] = memory.get(x).action().getIndex();
        }

        NeuralNetworkTrainingData neuralNetworkTrainingData = new NeuralNetworkTrainingData(inputs, scores, newSurroundingItems, actionsTaken);
        neuralNetwork.fit(neuralNetworkTrainingData, 10);
    }

    @Override
    public String getCharacterCode() {

        if (this.energy == 0) {
            return "E";
        }

        if (!alive) {
            return "X";
        } else {
            return "O";
        }
    }

    @Override
    public double getWorldItemId() {
        return alive? WorldItemIds.LIVING_THING_ID : WorldItemIds.CORPSE;
    }

    @Override
    public String getColourCode() {
        if (!alive) {
            return "#000000";
        }

        if (this.hunger > this.thirst && this.hunger > this.isolation && hunger > 200) {
            return "#964B00";
        }

        if (this.thirst > this.hunger && this.thirst > this.isolation && thirst > 200) {
            return "#FFC0CB";
        }

        if (this.isolation > this.hunger && this.isolation > this.thirst && isolation > 200) {
            return "#FF0000";
        }

        return "#FFFFFF";
    }

    public boolean isAlive() {
        return alive;
    }

    @Override
    public Direction getDirectionIamFacing() {
        if (currentDirection == Direction.STAY_STILL) {
            return previousDirection;
        } else {
            return currentDirection;
        }
    }

    @Override
    public boolean isFitToBreed() {
        return this.isolation < 200 && this.hunger < 200 && this.thirst < 200;
    }

    @Override
    public int getFitnessScore() {
        return totalScore;
    }

    public NeuralNetwork getNeuralNetwork() {
        return this.neuralNetwork.copy();
    }

    public int getTicks() {
        return ticks;
    }
}

