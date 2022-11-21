package org.cooney.world.map;

import org.cooney.world.items.WorldItem;

public class GridItem {
    private WorldItem worldItem;

    public GridItem(WorldItem worldItem) {
        this.worldItem = worldItem;
    }

    public WorldItem getWorldItem() {
        return worldItem;
    }

    public void setWorldItem(WorldItem worldItem) {
        this.worldItem = worldItem;
    }
}
