package org.cooney.world.items.agents;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.neural.NeuralNetwork;
import org.cooney.neural.NeuralNetworkTrainingData;
import org.cooney.world.WorldEngine;
import org.cooney.world.items.Breeder;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;

public class SurvivingThing extends LivingEntity implements Breeder {
    private double hunger;
    private double thirst;
    private double isolation;
    private int totalScore;
    private double energy;

    public SurvivingThing(WorldEngine outsideWorld) {
        super(0.05,
                100,
                100,
                1000,
                new NeuralNetwork(7, 150, 5, 0.1),
                outsideWorld,
                0,
                0.95
        );

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;
        this.ticks = 0;
        this.energy = 500;
    }

    public SurvivingThing(WorldEngine outsideWorld, NeuralNetwork neuralNetwork, double explorationRate, int ticks) {
        super(0.05,
                100,
                100,
                1000,
                neuralNetwork,
                outsideWorld,
                ticks,
                explorationRate
        );

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;
        this.energy = 500;
    }

    protected void makeAMove(double[] surroundingItems) throws InvalidMatrixShapeException {
        double[] stats = createStatsArray();
        double[] networkInput = buildNeuralNetworkInputArray(surroundingItems, stats);
        Direction direction = energy == 0? Direction.STAY_STILL : decide(networkInput);
        move(direction);

        // If it went from moving to staying still, keep the previous direction set to what it was already.
        // If they've gone from moving in one direction to another, track the previous direction.
        updateDirection(direction);

        List<GridItem> surroundingGridItems = lookAround();
        List<GridItem> consumableGridItems = outsideWorld.getInteractableGridItems(this);
        double[] newStats = consumeResources(consumableGridItems);
        double moveScore = scoreTheMoveIMade(newStats);
        rememberThisDecision(surroundingItems, createStatsArray(), moveScore, gridItemsToNetworkInput(surroundingGridItems), newStats, direction);
        updateMyStats(newStats);

        if (direction != Direction.STAY_STILL) {
            energy = energy - 1;
        } else {
            energy += 100;
        }
    }

    @Override
    protected boolean shouldBeDead() {
        return thirst > 1000 || hunger > 1000;
    }

    private double[] createStatsArray() {
        return new double[]{this.hunger, this.thirst, this.isolation, this.energy};
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
            newSurroundingItems[x] = buildNeuralNetworkInputArray(memory.get(x).newSurroundingItems(), memory.get(x).newStats());
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

    public boolean isFitToBreed() {
        return true;
    }

    public int getFitnessScore() {
        return totalScore;
    }

    @Override
    public Breeder copy() {
        return new SurvivingThing(outsideWorld, this.getNeuralNetwork(), 0.05, this.getTicks());
    }
}

