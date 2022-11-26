package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;

public class FoodOnlySeeder implements Seeder {
    private static final double LIVING_THING_CHANCE = 0.005;
    private static final double FOOD_CHANCE = 0.02;

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
        double relativeLivingThingChance = LIVING_THING_CHANCE / 3;
        double relativeFoodChance = FOOD_CHANCE / 3;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            return new LivingThing(worldEngine);
        } else if (random <= (relativeLivingThingChance + relativeFoodChance)) {
            return new Food();
        } else {
            return new EmptyWorldItem();
        }
    }
}
