package org.cooney.world.map;

import org.cooney.world.WorldEngine;

public interface Seeder {
    public void seedWorld(WorldEngine worldEngine);
    public int getPopulationCap();
    public int getReproduceRateInMillis();

    public int getNewGenerationCount();
}
