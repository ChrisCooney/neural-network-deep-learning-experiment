package org.cooney.world.items.agents;

record LivingThingMemory(double[] oldSurroundingItems, double[] stats, double score, double[] newSurroundingItems, double[] newStats, Direction action) {
}
