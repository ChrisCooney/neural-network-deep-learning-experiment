package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.utils.ChanceUtils;

public class RandomWorldSeeder implements Seeder {

    private static final double LIVING_THING_CHANCE = 0.015;
    private static final double FOOD_CHANCE = 0.3;
    private static final double WATER_CHANCE = 0.3;

    @Override
    public void seedWorld(WorldEngine worldEngine) {

        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {
                WorldItem worldItem = decideWorldItemByChance(worldEngine);
                worldEngine.putItemAt(y, x, worldItem);
            }
        }
    }

    private WorldItem decideWorldItemByChance(WorldEngine worldEngine) {
        double relativeLivingThingChance = LIVING_THING_CHANCE / 4;
        double relativeFoodChance = FOOD_CHANCE / 4;
        double relativeWaterChance = WATER_CHANCE / 4;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            return new LivingThing(worldEngine);
        } else if (random <= (relativeLivingThingChance + relativeFoodChance)) {
            return new Food();
        } else if (random <= (relativeLivingThingChance + relativeFoodChance + relativeWaterChance)) {
            return new Water();
        } else {
            return new EmptyWorldItem();
        }
    }
}
