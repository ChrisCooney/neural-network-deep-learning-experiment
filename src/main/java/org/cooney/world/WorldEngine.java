package org.cooney.world;

import org.cooney.world.items.Actor;
import org.cooney.world.items.EmptyWorldItem;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;
import org.cooney.world.utils.ChanceUtils;

import java.util.*;
import java.util.stream.IntStream;

public class WorldEngine {

    private static final int VIEW_RANGE = 20;
    private final GridItem[][] world;

    private final List<Actor> actorsInWorld;

    private final Map<WorldItem, int[]> coordsLookupMap;

    private List<Thread> actorThreads;

    private final int width;
    private final int height;

    public WorldEngine(int height, int width) {
        this.world = new GridItem[height][width];
        this.width = width;
        this.height = height;
        coordsLookupMap = new HashMap<>();
        actorsInWorld = new ArrayList<>();
        actorThreads = new ArrayList<>();

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
        boolean isLivingThing = ChanceUtils.rollTheDice(0.3);

        if (isLivingThing) {
            LivingThing lt = new LivingThing(this);
            actorsInWorld.add(lt);
            return lt;
        }

        boolean isFood = ChanceUtils.rollTheDice(0.4);

        if (isFood) return new Food();

        boolean isWater = ChanceUtils.rollTheDice(0.3);

        if (isWater) return new Water();

        return new EmptyWorldItem();
    }

    public List<GridItem> getGridItemsActorCanSee(Actor actor) {
        int[] coordinates = coordsLookupMap.get(actor);
        return getGridItemsAroundCoordinates(coordinates, VIEW_RANGE);
    }

    public List<GridItem> getGridItemsActorCanInteractWith(Actor actor) {
        int[] coordinates = coordsLookupMap.get(actor);
        return getGridItemsAroundCoordinates(coordinates, 2);
    }

    private List<GridItem> getGridItemsAroundCoordinates(int[] coordinates, int range) {
        int[] coordinateDeltas = IntStream.range(range * -1, range + 1).toArray();
        int[][] surroundingCoords = getSurroundingCoordinates(coordinates, coordinateDeltas);

        return Arrays.stream(surroundingCoords)
                .map(coords -> getItemAt(coords[0], coords[1])).toList();
    }

    private int[][] getSurroundingCoordinates(int[] coordinates, int[] coordinateDeltas) {
        int y = coordinates[0];
        int x = coordinates[1];

        int[][] allSurroundingCoords = new int[1680][2];

        int count = 0;

        for(int dx : coordinateDeltas) {
            for(int dy : coordinateDeltas) {
                if (dx == 0 && dy == 0) continue;
                allSurroundingCoords[count] = new int[]{Math.floorMod(y + dy,height), Math.floorMod(x + dx,width)};
                count ++;
            }
        }

        return allSurroundingCoords;
    }

    public void begin() {
        for(Actor actor : actorsInWorld) {
            Thread t = new Thread(actor::wakeUp);
            actorThreads.add(t);
            t.start();
        }
    }

    public void moveActor(Actor actor, int xDelta, int yDelta) {
        int[] currentCoords = coordsLookupMap.get(actor);

        int oldY = currentCoords[0];
        int oldX = currentCoords[1];

        int newY = Math.floorMod(oldY + yDelta, height);
        int newX = Math.floorMod(oldX + xDelta, width);

        if (world[newY][newX].getWorldItem().getWorldItemId() != WorldItemIds.LIVING_THING_ID) {
            // Prevent the living things from trampling food and water out of existence.
            putItemAt(oldY, oldX, world[newY][newX].getWorldItem());
        } else {
            putItemAt(oldY, oldX, new EmptyWorldItem());
        }

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
