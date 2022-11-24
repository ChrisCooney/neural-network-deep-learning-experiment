package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.utils.ChanceUtils;

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

    private WorldItem randomlySelectWorldItem(WorldEngine worldEngine) {
        double relativeLivingThingChance = 0.01/3;
        double relativeFoodChance = 0.04/3;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            return new LivingThing(worldEngine);
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
