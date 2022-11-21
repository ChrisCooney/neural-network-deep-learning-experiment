package org.cooney.world.items;

public interface Consumable {
    public void consume(Actor actor);
    public double getResourceCount();
    public void regenerate();
    public boolean shouldRegenerate();
}
