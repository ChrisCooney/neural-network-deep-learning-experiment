package org.cooney.world;

import org.cooney.world.items.*;
import org.cooney.world.items.agents.Direction;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.items.resources.Food;
import org.cooney.world.items.resources.Water;
import org.cooney.world.map.GridItem;
import org.cooney.world.map.Seeder;
import org.cooney.world.utils.ChanceUtils;

import java.util.*;
import java.util.stream.IntStream;

public class WorldEngine {

    private static final int VIEW_RANGE = 3;
    private final GridItem[][] world;

    private final List<Actor> actorsInWorld;

    private final Map<WorldItem, int[]> coordsLookupMap;

    private final List<Thread> actorThreads;



    private final int width;
    private final int height;

    public WorldEngine(int height, int width, Seeder seeder) {
        this.world = new GridItem[height][width];
        this.width = width;
        this.height = height;
        coordsLookupMap = new HashMap<>();
        actorsInWorld = new ArrayList<>();
        actorThreads = new ArrayList<>();

        seeder.seedWorld(this);
        populateOptimizedDataStructures();
    }

    private void populateOptimizedDataStructures() {
        for(int y = 0; y < height; y++) {
            for(int x = 0; x < width; x++) {
                WorldItem worldItem = world[y][x].getWorldItem();

                if (worldItem.getWorldItemId() == WorldItemIds.LIVING_THING_ID) {
                    this.coordsLookupMap.put(worldItem, new int[]{y, x});
                    this.actorsInWorld.add((Actor)worldItem);
                }
            }
        }
    }

    public List<GridItem> getGridItemsActorCanSee(Actor actor) {
        int[] coordinates = coordsLookupMap.get(actor);
        return getGridItemsAroundCoordinates(coordinates, VIEW_RANGE);
    }

    public List<GridItem> getGridItemsInLineOfSight(Actor actor, Direction direction) {
        int[][] coordsInLineOfSight = new int[9][2];
        int[] coords = coordsLookupMap.get(actor);

        // Something about looping over an offset and multiply the range values by the offset
        // To compute the line of sight in a given direction. The signs are already in the Direction.

        return null;
    }

    public List<GridItem> getGridItemsActorCanInteractWith(Actor actor) {
        int[] coordinates = coordsLookupMap.get(actor);
        return getGridItemsAroundCoordinates(coordinates, 1);
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

        int[][] allSurroundingCoords = new int[48][2];

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

    public void addActorNextToActor(Actor existingItem, Actor newItem) {
        int[] currentCoords = coordsLookupMap.get(existingItem);
        int[] newCoords = new int[]{Math.floorMod(currentCoords[0] + 1, height), Math.floorMod(currentCoords[1] + 1, width)};
        coordsLookupMap.put(newItem, newCoords);
        actorsInWorld.add(newItem);
        this.putItemAt(newCoords[0], newCoords[1], newItem);
    }

    public void begin() {
        for(Actor actor : actorsInWorld) {
            Thread t = new Thread(actor::wakeUp);
            actorThreads.add(t);
            t.start();
        }

        Thread reproduceThread = new Thread(() -> {
            try {
                reproduceInPopulation();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });
        reproduceThread.start();
    }

    private void asyncWakeUp(Actor actor) {
        Thread t = new Thread(actor::wakeUp);
        actorThreads.add(t);
        t.start();
    }

    private void reproduceInPopulation() throws InterruptedException {
        while(true) {
            Thread.sleep(10000);
            System.out.println("Reproduce Cycle Occurring");

            List<Breeder> breeders = actorsInWorld.stream().filter(Actor::isAlive).map(actor -> ((Breeder)actor)).toList();
            List<Breeder> orderedByPerformance = breeders.stream()
                    .sorted(Comparator.comparingInt(Breeder::getFitnessScore).reversed()).toList();
            System.out.println(orderedByPerformance.size() + " living things fit to breed. The top 3 will reproduce.");

            System.out.println(orderedByPerformance.stream().map(Breeder::getFitnessScore).toList().toString());

            int newChildCount = Math.min(orderedByPerformance.size(), 3);

            for(int x = 0; x < newChildCount; x++) {
                LivingThing parent = (LivingThing) orderedByPerformance.get(x);
                LivingThing child = new LivingThing(this, parent.getNeuralNetwork(), 0.05);
                this.addActorNextToActor(parent, child);
                asyncWakeUp(child);
            }
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

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
