package org.cooney.world.items.agents;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;
import org.cooney.world.items.Actor;
import org.cooney.world.items.Breeder;
import org.cooney.world.items.Learner;
import org.cooney.world.items.WorldItem;
import org.cooney.world.map.GridItem;

import java.util.List;

public class FightingThing implements Actor, WorldItem, Learner, Breeder {



    @Override
    public void wakeUp() {

    }

    @Override
    public void act(List<GridItem> nearbyGridItems) {

    }

    @Override
    public boolean isAlive() {
        return false;
    }

    @Override
    public Direction getDirectionIamFacing() {
        return null;
    }

    @Override
    public boolean isFitToBreed() {
        return false;
    }

    @Override
    public int getFitnessScore() {
        return 0;
    }

    @Override
    public void learn() throws InvalidTrainingDataException, InvalidMatrixShapeException {

    }

    @Override
    public String getCharacterCode() {
        return null;
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
