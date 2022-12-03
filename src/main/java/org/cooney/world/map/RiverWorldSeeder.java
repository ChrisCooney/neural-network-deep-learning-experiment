package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.SurvivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;

public class RiverWorldSeeder implements Seeder {

    private static final int RIVER_WIDTH = 5;

    @Override
    public void seedWorld(WorldEngine worldEngine) {

        int riverStartIndex = findRiverStartIndex(worldEngine.getWidth());
        int riverEndIndex = riverStartIndex + RIVER_WIDTH;

        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {

                WorldItem worldItem;

                if (x >= riverStartIndex && x <= riverEndIndex) {
                    worldItem = new Water();
                } else {
                    worldItem = randomlySelectWorldItem(worldEngine);
                }

                worldEngine.putItemAt(y, x, worldItem);
            }
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

    private WorldItem randomlySelectWorldItem(WorldEngine worldEngine) {
        double relativeLivingThingChance = 0.01/3;
        double relativeFoodChance = 0.015/3;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            return new SurvivingThing(worldEngine);
        } else if (random <= (relativeLivingThingChance + relativeFoodChance)) {
            return new Food();
        } else {
            return new EmptyWorldItem();
        }
    }

    private int findRiverStartIndex(int width) {
        int midPoint = width / 2;

        return midPoint - (RIVER_WIDTH / 2);
    }
}
