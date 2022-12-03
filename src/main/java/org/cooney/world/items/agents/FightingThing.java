package org.cooney.world.items.agents;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.neural.NeuralNetwork;
import org.cooney.neural.NeuralNetworkTrainingData;
import org.cooney.world.WorldEngine;
import org.cooney.world.items.Breeder;
import org.cooney.world.items.Fighter;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.map.GridItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.DoubleStream;

public class FightingThing extends LivingEntity implements Fighter, Breeder {
    private final int teamNumber;
    private int healthPoints;
    private int fightsWon;

    public FightingThing(WorldEngine outsideWorld, int teamNumber) {
        super(0.05,
                100,
                100,
                1000,
                new NeuralNetwork(7, 100, 5, 0.1),
                outsideWorld,
                0,
                0.95
        );

        this.teamNumber = teamNumber;
        this.healthPoints = 100;
    }

    public FightingThing(WorldEngine outsideWorld, NeuralNetwork neuralNetwork, double explorationRate, int ticks, int teamNumber) {
        super(
                0.05,
                100,
                100,
                1000,
                neuralNetwork,
                outsideWorld,
                ticks,
                explorationRate);

        this.teamNumber = teamNumber;
        this.healthPoints = 100;
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
            inputs[x] = buildNeuralNetworkInputArray(memory.get(x).oldSurroundingItems(), memory.get(x).stats()[0], memory.get(x).stats()[1]);
            scores[x] = memory.get(x).score();
            newSurroundingItems[x] = buildNeuralNetworkInputArray(memory.get(x).newSurroundingItems(), memory.get(x).newStats()[0], memory.get(x).newStats()[1]);
            actionsTaken[x] = memory.get(x).action().getIndex();
        }

        NeuralNetworkTrainingData neuralNetworkTrainingData = new NeuralNetworkTrainingData(inputs, scores, newSurroundingItems, actionsTaken);
        neuralNetwork.fit(neuralNetworkTrainingData, 10);
    }

    @Override
    public String getCharacterCode() {
        return this.alive ? " " : "X";
    }

    @Override
    public String getColourCode() {
        return teamNumber == 1 ? "#0000FF":"#FF0000";
    }

    @Override
    protected boolean shouldBeDead() {
        return healthPoints <= 0;
    }

    public double getWorldItemId() {
        if (!this.alive) {
            return WorldItemIds.CORPSE;
        }

        return WorldItemIds.FIGHTING_THING_ID;
    }

    @Override
    protected void makeAMove(double[] input) throws InvalidMatrixShapeException {
        int currentHealthPoints = this.healthPoints;
        double[] networkInput = buildNeuralNetworkInputArray(input, currentHealthPoints, this.fightsWon);
        Direction direction = decide(networkInput);
        move(direction);
        updateDirection(direction);

        List<GridItem> visibleGridItems = lookAround();
        List<Fighter> enemiesInRange = findNearbyEnemies(outsideWorld.getInteractableGridItems(this));
        List<Fighter> enemiesICanBeat = findEnemiesICanBeat(enemiesInRange);
        int newFightsWon = attack(enemiesICanBeat);

        double moveScore = scoreTheMoveIMade(newFightsWon, currentHealthPoints);
        rememberThisDecision(
                input,
                createStatsArray(this.fightsWon, currentHealthPoints),
                moveScore,
                gridItemsToNetworkInput(visibleGridItems),
                createStatsArray(newFightsWon, this.healthPoints),
                direction
        );
        updateMyStats(newFightsWon);
    }

    private void updateMyStats(int newFightsWon) {
        this.fightsWon = newFightsWon;
    }

    private double[] createStatsArray(int fightsWon, int healthPoints) {
        return new double[]{healthPoints, fightsWon};
    }

    private double scoreTheMoveIMade(int newFightsWon, int oldHealthPoints) {
        double score = 0;

        if (newFightsWon > this.fightsWon) {
            score += 10;
        }

        // This is updated by other actors so we have to tackle the other way around.
        if (oldHealthPoints <= healthPoints) {
            score += 1;
        }

        return score;
    }

    private List<Fighter> findEnemiesICanBeat(List<Fighter> fighters) {
        int myNearbyAllies = getNearbyAlliesCount();

        return fighters.stream()
                .filter(fighter -> fighter.getNearbyAlliesCount() < myNearbyAllies)
                .toList();
    }

    private List<Fighter> findNearbyEnemies(List<GridItem> gridItems) {
        return gridItems.stream()
                .map(GridItem::getWorldItem)
                .filter(worldItem -> worldItem.getWorldItemId() == WorldItemIds.FIGHTING_THING_ID)
                .map(worldItem -> (Fighter)worldItem)
                .filter(fighter -> fighter.getTeamNumber() != this.teamNumber)
                .toList();
    }

    private double[] buildNeuralNetworkInputArray(double[] surroundingFighters, double healthPoints, double fightsWon) {
        return DoubleStream
                .concat(
                        Arrays.stream(new double[]{this.healthPoints, fightsWon}),
                        Arrays.stream(surroundingFighters))
                .toArray();
    }

    public int getTeamNumber() {
        return this.teamNumber;
    }

    @Override
    public void takeAHit() {
        this.healthPoints -= 10;
    }

    @Override
    public int attack(List<Fighter> fighters) {
        fighters.forEach(Fighter::takeAHit);

        if (fighters.size() > 0) {
            System.out.println("Attacking - " + fighters);
        }

        return this.fightsWon + fighters.size();
    }

    @Override
    public int getNearbyAlliesCount() {
        return (int)outsideWorld.getInteractableGridItems(this).stream()
                .map(GridItem::getWorldItem)
                .filter(worldItem -> worldItem.getWorldItemId() == WorldItemIds.FIGHTING_THING_ID)
                .map(worldItem -> (Fighter)worldItem)
                .filter(fighter -> fighter.getTeamNumber() == this.teamNumber)
                .count();
    }

    @Override
    public int getNearbyEnemiesCount() {
        return findNearbyEnemies(outsideWorld.getInteractableGridItems(this)).size();
    }

    public String toString() {
        return String.format("FW = %d - HP = %d - Team = %d", fightsWon, healthPoints, teamNumber);
    }

    @Override
    public boolean isFitToBreed() {
        return true;
    }

    @Override
    public int getFitnessScore() {
        return fightsWon;
    }

    @Override
    public Breeder copy() {
        return new FightingThing(outsideWorld, this.getNeuralNetwork().copy(), 0.05, this.getTicks(), this.teamNumber);
    }
}
