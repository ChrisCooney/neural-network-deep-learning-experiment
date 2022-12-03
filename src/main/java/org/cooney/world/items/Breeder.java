package org.cooney.world.items;

public interface Breeder {
    public boolean isFitToBreed();
    public int getFitnessScore();
    public Breeder copy();
}
