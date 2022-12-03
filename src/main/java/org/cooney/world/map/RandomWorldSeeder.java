package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.SurvivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;

public class RandomWorldSeeder implements Seeder {

    private static final double LIVING_THING_CHANCE = 0.005;
    private static final double FOOD_CHANCE = 0.03;
    private static final double WATER_CHANCE = 0.05;

    @Override
    public void seedWorld(WorldEngine worldEngine) {

        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {
                WorldItem worldItem = decideWorldItemByChance(worldEngine);
                worldEngine.putItemAt(y, x, worldItem);
            }
        }
    }

    protected WorldItem decideWorldItemByChance(WorldEngine worldEngine) {
        double relativeLivingThingChance = LIVING_THING_CHANCE / 4;
        double relativeFoodChance = FOOD_CHANCE / 4;
        double relativeWaterChance = WATER_CHANCE / 4;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            return new SurvivingThing(worldEngine);
        } else if (random <= (relativeLivingThingChance + relativeFoodChance)) {
            return new Food();
        } else if (random <= (relativeLivingThingChance + relativeFoodChance + relativeWaterChance)) {
            return new Water();
        } else {
            return new EmptyWorldItem();
        }
    }

    public int getReproduceRateInMillis() {
        return 10000;
    }

    @Override
    public int getNewGenerationCount() {
        return 5;
    }

    @Override
    public int getPopulationCap() {
        return 30;
    }
}
