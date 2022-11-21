package org.cooney.world;

import org.cooney.world.items.Actor;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;
import org.cooney.world.utils.ChanceUtils;

import java.util.*;

public class WorldEngine {
    private final GridItem[][] world;

    private final List<Actor> actorsInWorld;

    private final Map<WorldItem, int[]> coordsLookupMap;

    private final int width;
    private final int height;

    public WorldEngine(int height, int width) {
        this.world = new GridItem[height][width];
        this.width = width;
        this.height = height;
        coordsLookupMap = new HashMap<>();
        actorsInWorld = new ArrayList<>();

        seedEmptyWorld();
    }
    private void seedEmptyWorld() {
        // Need to make a world and seed it.
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                WorldItem worldItem = decideWorldItemByChance();
                this.putItemAt(y, x, worldItem);

                this.coordsLookupMap.put(worldItem, new int[]{y, x});
            }
        }
    }
    private WorldItem decideWorldItemByChance() {
        boolean isLivingThing = ChanceUtils.rollTheDice(0.1);

        if (isLivingThing) {
            LivingThing lt = new LivingThing(this);
            actorsInWorld.add(lt);
            return lt;
        }

        boolean isFood = ChanceUtils.rollTheDice(1);

        if (isFood) return new Food();

        boolean isWater = ChanceUtils.rollTheDice(2);

        if (isWater) return new Water();

        return new EmptyWorldItem();
    }

    public List<GridItem> getGridItemsAroundActor(Actor actor) {
        int[] coordinates = coordsLookupMap.get(actor);
        return getGridItemsAroundCoordinates(coordinates);
    }

    private List<GridItem> getGridItemsAroundCoordinates(int[] coordinates) {
        int[][] surroundingCoords = getSurroundingCoordinates(coordinates);

        return Arrays.stream(surroundingCoords)
                .map(coords -> getItemAt(coords[0], coords[1])).toList();
    }

    private int[][] getSurroundingCoordinates(int[] coordinates) {
        int y = coordinates[0];
        int x = coordinates[1];

        int[][] allSurroundingCoords = new int[8][2];

        allSurroundingCoords[0] = new int[]{Math.floorMod(y + 1,height), x};
        allSurroundingCoords[1] = new int[]{Math.floorMod(y - 1,height), x};
        allSurroundingCoords[2] = new int[]{Math.floorMod(y + 1,height), Math.floorMod(x + 1,width)};
        allSurroundingCoords[3] = new int[]{Math.floorMod(y + 1,height), Math.floorMod(x - 1,width)};
        allSurroundingCoords[4] = new int[]{y, Math.floorMod(x + 1, width)};
        allSurroundingCoords[5] = new int[]{y, Math.floorMod(x - 1, width)};
        allSurroundingCoords[6] = new int[]{Math.floorMod(y - 1,height), Math.floorMod(x - 1,width)};
        allSurroundingCoords[7] = new int[]{Math.floorMod(y - 1,height), Math.floorMod(x + 1,width)};

        return allSurroundingCoords;
    }

    public void tick() {
        for(Actor actor : actorsInWorld) {
            int[] actorCoords = coordsLookupMap.get(actor);
            List<GridItem> surroundingItems = getGridItemsAroundCoordinates(actorCoords);

            actor.act(surroundingItems);
        }
    }

    public void moveActor(Actor actor, int xDelta, int yDelta) {
        int[] currentCoords = coordsLookupMap.get(actor);

        int oldY = currentCoords[0];
        int oldX = currentCoords[1];

        int newY = Math.floorMod(oldY + yDelta, height);
        int newX = Math.floorMod(oldX + xDelta, width);

        putItemAt(oldY, oldX, world[newY][newX].getWorldItem());
        putItemAt(newY, newX, actor);

        coordsLookupMap.put(actor, new int[]{newY, newX});
    }

    public void putItemAt(int y, int x, WorldItem worldItem) {
        GridItem gridItem = new GridItem(worldItem);
        world[y][x] = gridItem;
    }

    public GridItem getItemAt(int y, int x) {
        return world[y][x];
    }
}
