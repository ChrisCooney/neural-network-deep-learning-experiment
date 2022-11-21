package org.cooney.world.items;

public class EmptyWorldItem implements WorldItem{
    @Override
    public String getCharacterCode() {
        return " ";
    }

    @Override
    public double getWorldItemId() {
        return 0;
    }

    @Override
    public String getColourCode() {
        return null;
    }
}
