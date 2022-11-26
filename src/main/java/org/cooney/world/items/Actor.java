package org.cooney.world.items;

import org.cooney.world.items.agents.Direction;
import org.cooney.world.map.GridItem;

import java.util.List;

public interface Actor extends WorldItem {
    public void wakeUp();
    public void act(List<GridItem> nearbyGridItems);
    public boolean isAlive();
    public Direction getDirectionIamFacing();
}
