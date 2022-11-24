package org.cooney.world.map;

import org.cooney.world.WorldEngine;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.utils.ChanceUtils;

public class FarmWorldSeeder implements Seeder{

    @Override
    public void seedWorld(WorldEngine worldEngine) {
        for(int y = 0; y < worldEngine.getHeight(); y++) {
            for(int x = 0; x < worldEngine.getWidth(); x++) {

                WorldItem worldItem;

                int FARM_HEIGHT = 10;
                int WATER_START_POINT = 10;

                if (x <= FARM_HEIGHT) {
                    worldItem = new Food();
                } else if (x >= worldEngine.getWidth() - WATER_START_POINT) {
                    worldItem = new Water();
                }
                else {
                    worldItem = ChanceUtils.rollTheDice(0.5) ? new LivingThing(worldEngine) : new EmptyWorldItem();
                }

                worldEngine.putItemAt(y, x, worldItem);
            }
        }
    }
}
