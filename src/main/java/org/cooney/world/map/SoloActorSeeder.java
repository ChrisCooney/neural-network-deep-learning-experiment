package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;

public class SoloActorSeeder implements Seeder {

    @Override
    public void seedWorld(WorldEngine worldEngine) {

        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {
                WorldItem worldItem = decideWorldItemByChance(worldEngine);
                worldEngine.putItemAt(y, x, worldItem);
            }
        }

        int xRand = (int) (Math.random() * worldEngine.getWidth());
        int yRand = (int) (Math.random() * worldEngine.getHeight());

        worldEngine.putItemAt(yRand, xRand, new LivingThing(worldEngine));
    }

    protected WorldItem decideWorldItemByChance(WorldEngine worldEngine) {
        double relativeFoodChance = 0.05 / 3;
        double relativeWaterChance = 0.03 / 3;

        double random = Math.random();

        if (random <= (relativeFoodChance)) {
            return new Food();
        } else if (random <= (relativeFoodChance + relativeWaterChance)) {
            return new Water();
        } else {
            return new EmptyWorldItem();
        }
    }
}
