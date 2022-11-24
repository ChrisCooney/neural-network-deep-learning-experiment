package org.cooney.world.items.resources;

import org.cooney.world.items.Actor;
import org.cooney.world.items.Consumable;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.utils.ChanceUtils;

public class Water implements Consumable, WorldItem {

    private static final double MAX_RESOURCE_COUNT = 30;
    private double resourceCount = 300;
    @Override
    public void consume(Actor actor) {
        //this.resourceCount --;
    }

    @Override
    public double getResourceCount() {
        return resourceCount;
    }

    @Override
    public void regenerate() {
        if (resourceCount < MAX_RESOURCE_COUNT) {
            resourceCount ++;
        }
    }

    @Override
    public boolean shouldRegenerate() {
        return ChanceUtils.rollTheDice(20);
    }

    @Override
    public String getCharacterCode() {
        return " ";
    }

    @Override
    public double getWorldItemId() {
        return WorldItemIds.WATER_ID;
    }

    @Override
    public String getColourCode() {
        return resourceCount > 0 ? "BLUE": "GREY";
    }
}
