package org.cooney.world.items.agents;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.neural.NeuralNetwork;
import org.cooney.neural.NeuralNetworkTrainingData;
import org.cooney.world.WorldEngine;
import org.cooney.world.items.Actor;
import org.cooney.world.items.Learner;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LivingThing implements Actor, WorldItem, Learner {

    private static final int MEDITATION_CADENCE_IN_TICKS = 100;
    private static final int MAX_MEMORY_SIZE = 10000;

    private final NeuralNetwork neuralNetwork;
    private final List<LivingThingMemory> memory;

    private final WorldEngine outsideWorld;

    private double hunger;

    private double thirst;

    private double isolation;

    private int ticksSinceLastMeditation;

    public LivingThing(WorldEngine outsideWorld) {
        neuralNetwork = new NeuralNetwork(8, 30, 9, 0.01);
        this.memory = new ArrayList<>();

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;
        this.ticksSinceLastMeditation = 0;

        this.outsideWorld = outsideWorld;
    }

    @Override
    public void act(List<GridItem> gridItems) {
        double[] input = gridItemsToNetworkInput(gridItems);

        try {
            if (ticksSinceLastMeditation < MEDITATION_CADENCE_IN_TICKS) {
                makeAMove(input);
            } else {
                learn();
                ticksSinceLastMeditation = 0;
            }

        } catch (InvalidMatrixShapeException | InvalidTrainingDataException e) {
            throw new RuntimeException(e);
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
        List<GridItem> surroundingGridItems = lookAround();
        double[] newStats = consumeResources(surroundingGridItems);
        double moveScore = scoreTheMoveIMade(newStats);
        updateMyStats(newStats);
        rememberThisDecision(input, moveScore, gridItemsToNetworkInput(surroundingGridItems), direction);
        this.ticksSinceLastMeditation ++;
    }

    private void rememberThisDecision(double[] input, double moveScore, double[] newSurroundingItems, Direction direction) {
        this.memory.add(new LivingThingMemory(input, moveScore, newSurroundingItems, direction));

        if (this.memory.size() > MAX_MEMORY_SIZE) {
            // Get rid of the oldest memory from the working set.
            this.memory.remove(0);
        }
    }

    private void updateMyStats(double[] newStats) {
        this.hunger = newStats[0];
        this.thirst = newStats[1];
        this.isolation = newStats[2];
    }

    private double scoreTheMoveIMade(double[] newStats) {
        double score = 0;

        if (hunger > thirst && hunger > isolation) {
            // Hunger is the primary motivator. Score based on that.
            if (newStats[0] < hunger) {
                // We reduced hunger and that was the goal! Yay!
                score += 10;
            }
        }

        if (thirst > hunger && thirst > isolation) {
            // Thirst is the most important...
            if (newStats[1] < thirst) {
                score += 10;
            }
        }

        if (isolation > hunger && isolation > thirst) {
            // Isolation is the most important...
            if (newStats[2] < isolation) {
                score += 10;
            }
        }

        System.out.println("SCORE: " + score);

        return score;
    }

    private double[] consumeResources(List<GridItem> gridItems) {
        // Look to see if there is food nearby.
        Map<Double, List<WorldItem>> mappedById = gridItems.stream()
                .map(GridItem::getWorldItem).collect(Collectors.groupingBy(WorldItem::getWorldItemId));

        double[] newStats = new double[3];

        if (!mappedById.containsKey(WorldItemIds.FOOD_ID)) {
            newStats[0] = this.hunger + 1;
        } else {
            mappedById.get(WorldItemIds.FOOD_ID).stream()
                    .map(worldItem -> (Food)worldItem)
                    .max((f1, f2) -> (int) (f1.getResourceCount() - f2.getResourceCount()))
                    .ifPresent((bestFoodSource) -> {
                        bestFoodSource.consume(this);
                        newStats[0] = 0;
                    });
        }



        if (!mappedById.containsKey(WorldItemIds.WATER_ID)) {
            newStats[1] = this.thirst + 1;
        } else {
            mappedById.get(WorldItemIds.WATER_ID).stream()
                    .map(worldItem -> (Water) worldItem)
                    .max((w1, w2) -> (int) (w1.getResourceCount() - w2.getResourceCount()))
                    .ifPresent((bestWaterSource) -> {
                        bestWaterSource.consume(this);
                        newStats[1] = 0;
                    });
        }


        if (!mappedById.containsKey(WorldItemIds.LIVING_THING_ID)) {
            newStats[2] = this.isolation + 1;
        } else {
            newStats[2] = this.isolation > 0 ? this.isolation - 1 : 0;
        }

        return newStats;
    }

    private List<GridItem> lookAround() {
        return outsideWorld.getGridItemsAroundActor(this);
    }

    private void move(Direction direction) {
        int xDirection = direction.getXDirection();
        int yDirection = direction.getYDirection();

        outsideWorld.moveActor(this, xDirection, yDirection);
    }

    private Direction decide(double[] networkInput) throws InvalidMatrixShapeException {
        double[] possibleQValues = neuralNetwork.predict(networkInput);

        int indexOfMax = -1;
        double temp = -1;

        for(int x = 0; x < possibleQValues.length; x++) {
            if (possibleQValues[x] > temp) {
                indexOfMax = x;
                temp = possibleQValues[x];
            }
        }

        Direction d = Direction.getFromIndex(indexOfMax);

        System.out.printf("I am deciding to go %s - My Stats are Hunger = %f, Thirst = %f, Isolation = %f%n", d.toString(), this.hunger, this.thirst, this.isolation);

        return Direction.getFromIndex(indexOfMax);
    }

    @Override
    public void learn() throws InvalidTrainingDataException, InvalidMatrixShapeException {
        if (memory.size() == 0) {
            // This poor soul has no memories. Allow them to continue to wander aimlessly in the dystopian abyss
            // that I have created. Vaya con dios my friend.
            return;
        }

        double[][] inputs = new double[memory.size()][8];
        double[] scores = new double[memory.size()];
        double[][] newSurroundingItems = new double[memory.size()][8];
        int[] actionsTaken = new int[memory.size()];

        for (int x = 0; x < memory.size(); x++) {
            inputs[x] = memory.get(x).input();
            scores[x] = memory.get(x).score();
            newSurroundingItems[x] = memory.get(x).newSurroundingItems();
            actionsTaken[x] = memory.get(x).action().getIndex();
        }

        NeuralNetworkTrainingData neuralNetworkTrainingData = new NeuralNetworkTrainingData(inputs, scores, newSurroundingItems, actionsTaken);
        neuralNetwork.fit(neuralNetworkTrainingData, 100);
    }

    @Override
    public String getCharacterCode() {
        return " ";
    }

    @Override
    public double getWorldItemId() {
        return WorldItemIds.LIVING_THING_ID;
    }

    @Override
    public String getColourCode() {
        return "RED";
    }
}

record LivingThingMemory(double[] input, double score, double[] newSurroundingItems, Direction action) {}
