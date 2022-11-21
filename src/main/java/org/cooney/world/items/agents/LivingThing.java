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

    private final NeuralNetwork neuralNetwork;
    private final List<LivingThingMemory> memory;

    private final WorldEngine outsideWorld;

    private double hunger;

    private double thirst;

    private double isolation;

    private int ticksSinceLastMeditation;

    public LivingThing(WorldEngine outsideWorld) {
        neuralNetwork = new NeuralNetwork(8, 30, 3, 1);
        this.memory = new ArrayList<>();

        this.hunger = 0;
        this.thirst = 0;
        this.isolation = 0;
        this.ticksSinceLastMeditation = 0;

        this.outsideWorld = outsideWorld;
    }

    @Override
    public void act(List<GridItem> gridItems) {
        double[] input = gridItems.stream()
                .map(GridItem::getWorldItem)
                .map(WorldItem::getWorldItemId)
                .mapToDouble(Double::doubleValue)
                .toArray();

        try {
            if (ticksSinceLastMeditation < MEDITATION_CADENCE_IN_TICKS) {
                makeAMove(input, gridItems);
            } else {
                meditateOnWhatYouHaveLearned();
                ticksSinceLastMeditation = 0;
            }

        } catch (InvalidMatrixShapeException | InvalidTrainingDataException e) {
            throw new RuntimeException(e);
        }
    }

    private void makeAMove(double[] input, List<GridItem> gridItems) throws InvalidMatrixShapeException {
        double[] decisionData = decide(gridItems, input);
        move(decisionData[0], decisionData[1], decisionData[2]);
        List<GridItem> surroundingGridItems = lookAround();
        double[] newStats = consumeResources(surroundingGridItems);
        boolean wasAGoodMove = decideIfIMadeAGoodMove(newStats);
        updateMyStats(newStats);
        rememberThisDecision(input, decisionData, wasAGoodMove);
        this.ticksSinceLastMeditation ++;
    }

    private void rememberThisDecision(double[] input, double[] output, boolean wasAGoodMove) {
        this.memory.add(new LivingThingMemory(input, output, wasAGoodMove));
    }

    private void updateMyStats(double[] newStats) {
        this.hunger = newStats[0];
        this.thirst = newStats[1];
        this.isolation = newStats[2];
    }

    private boolean decideIfIMadeAGoodMove(double[] newStats) {
        boolean wasAGoodMove = false;

        if (newStats[0] < this.hunger) {
            // We've lowered hunger!
            wasAGoodMove = true;
        } else if (newStats[1] < this.thirst) {
            // We've lowered thirst!
            wasAGoodMove = true;
        } else if (newStats[2] < this.isolation) {
            // We've lowered isolation!
            wasAGoodMove = true;
        }

        return wasAGoodMove;
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
                        newStats[0] = this.hunger - 1;
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
                        newStats[1] = this.thirst > 0 ? this.thirst - 1 : 0;
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

    private void move(double speed, double xDelta, double yDelta) {
        if (speed <= 0) {
            return;
        }

        int xDirection = xDelta > 0 ? 1 : -1;
        int yDirection = yDelta > 0 ? 1 : -1;

        outsideWorld.moveActor(this, xDirection, yDirection);
    }

    private double[] decide(List<GridItem> gridItems, double[] networkInput) throws InvalidMatrixShapeException {
        return neuralNetwork.predict(networkInput);
    }

    private void meditateOnWhatYouHaveLearned() throws InvalidTrainingDataException, InvalidMatrixShapeException {
        List<LivingThingMemory> happyMemories = memory.stream().filter(LivingThingMemory::isGoodMemory).toList();

        if (happyMemories.size() == 0) {
            // This poor soul has no happy memories.
            return;
        }

        double[][] happyInputs = new double[happyMemories.size()][8];
        double[][] happyOutputs = new double[happyMemories.size()][3];
        for(int x = 0; x < happyMemories.size(); x++) {
            happyInputs[x] = happyMemories.get(x).input();
            happyOutputs[x] = happyMemories.get(x).output();
        }

        NeuralNetworkTrainingData neuralNetworkTrainingData = new NeuralNetworkTrainingData(happyInputs, happyOutputs);
        neuralNetwork.fit(neuralNetworkTrainingData, 100);
    }

    @Override
    public void learn() {

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

record LivingThingMemory(double[] input, double[] output, boolean isGoodMemory) {}
