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
        if (resourceCount > 0) {
            // resourceCount --;
        } else {
            System.out.println("FOOD SOURCE DEPLETED");
        }
    }

    @Override
    public double getResourceCount() {
        return resourceCount;
    }

    @Override
    public void regenerate() {
        if (resourceCount < MAX_RESOURCE_COUNT) {
            resourceCount += 5;
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
    public boolean getIsMovingWorldItem() {
        return false;
    }

    @Override
    public String getColourCode() {
        return resourceCount > 0 ? "GREEN" : "YELLOW";
    }
}
