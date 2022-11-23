package org.cooney.world.items;

import org.cooney.matrix.InvalidMatrixShapeException;
import org.cooney.neural.InvalidTrainingDataException;

public interface Learner {
    void learn() throws InvalidTrainingDataException, InvalidMatrixShapeException;
}
