package org.cooney.world.items;

public class EmptyWorldItem implements WorldItem{
    @Override
    public String getCharacterCode() {
        return " ";
    }

    @Override
    public double getWorldItemId() {
        return WorldItemIds.EMPTY;
    }

    @Override
    public boolean getIsMovingWorldItem() {
        return false;
    }

    @Override
    public String getColourCode() {
        return null;
    }
}
