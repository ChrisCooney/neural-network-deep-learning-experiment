package org.cooney.world.utils;

public class ChanceUtils {
    /**
     * Utility function for chance based events.
     * @return A boolean value indicating if the win scenario has happened or not.
     */
    public static boolean rollTheDice(double winPercentageChance) {
        if (winPercentageChance >= 100) {
            return true;
        }

        if (winPercentageChance <= 0) {
            return false;
        }

        double outcome = Math.random() * 100;

        return outcome <= winPercentageChance;
    }
}
