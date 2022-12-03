package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.FightingThing;

public class BattleRoyaleSeeder implements Seeder {
    @Override
    public void seedWorld(WorldEngine worldEngine) {
        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {
                WorldItem worldItem = randomlySelectFighter(worldEngine);
                worldEngine.putItemAt(y, x, worldItem);
            }
        }
    }

    private WorldItem randomlySelectFighter(WorldEngine outsideWorld) {
        double relativeLivingThingChance = 0.007/2;

        double random = Math.random();

        if (random <= relativeLivingThingChance) {
            int teamNumber = Math.random() > 0.5 ? 1 : 2;
            return new FightingThing(outsideWorld, teamNumber);
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
