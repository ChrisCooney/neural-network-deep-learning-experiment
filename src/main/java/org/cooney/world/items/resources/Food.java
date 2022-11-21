package org.cooney.world.items.resources;

import org.cooney.world.items.Actor;
import org.cooney.world.items.Consumable;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.utils.ChanceUtils;

public class Food implements WorldItem, Consumable {

    private static final double MAX_RESOURCE_COUNT = 10;

    private int resourceCount = 30;

    @Override
    public void consume(Actor actor) {
        resourceCount--;
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
        return ChanceUtils.rollTheDice(3);
    }

    @Override
    public String getCharacterCode() {
        return " ";
    }

    @Override
    public double getWorldItemId() {
        return WorldItemIds.FOOD_ID;
    }

    @Override
    public String getColourCode() {
        return "GREEN";
    }
}
