package org.cooney.world.items.agents;

record LivingEntityMemory(double[] oldSurroundingItems, double[] stats, double score, double[] newSurroundingItems, double[] newStats, Direction action) {
}
