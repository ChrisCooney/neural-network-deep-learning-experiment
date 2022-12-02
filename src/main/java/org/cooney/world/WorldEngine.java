package org.cooney.world;

import org.cooney.world.items.*;
import org.cooney.world.items.agents.Direction;
import org.cooney.world.items.agents.LivingThing;
import org.cooney.world.map.GridItem;
import org.cooney.world.map.Seeder;

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

    public List<GridItem> getGridItemsInActorLineOfSight(Actor actor) {
        int[] actorCoords = coordsLookupMap.get(actor);

        int x = actorCoords[1];
        int y = actorCoords[0];

        Direction d = actor.getDirectionIamFacing();

        int[][][] fieldOfVisionCoords = d.getFieldOfVisionCoordinateDeltas();

        List<GridItem> gridItemsInLineOfSight = new ArrayList<>();

        for(int sightLineIndex = 0; sightLineIndex < fieldOfVisionCoords.length; sightLineIndex ++) {

            boolean worldItemInLineOfSight = false;

            for(int[] cellInSight : fieldOfVisionCoords[sightLineIndex]) {
                int cellXCoord = x + cellInSight[1];

                if (cellXCoord < 0) {
                    cellXCoord = 0;
                } else if (cellXCoord >= width) {
                    cellXCoord = width - 1;
                }

                int cellYCoord = y + cellInSight[0];

                if (cellYCoord < 0) {
                    cellYCoord = 0;
                } else if (cellYCoord >= height) {
                    cellYCoord = height - 1;
                }

                GridItem gridItem = this.getItemAt(cellYCoord, cellXCoord);

                if (gridItem.getWorldItem().getWorldItemId() != WorldItemIds.EMPTY) {
                    worldItemInLineOfSight = true;
                    gridItemsInLineOfSight.add(gridItem);
                    break;
                }
            }

            if (!worldItemInLineOfSight) {
                gridItemsInLineOfSight.add(new GridItem(new EmptyWorldItem()));
            }
        }

        return gridItemsInLineOfSight;
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

    public void addActorInRandomPlace(Actor newItem) {
        int randomY = (int)(Math.random() * height - 1);
        int randomX = (int)(Math.random() * width - 1);

        int[] newCoords = new int[]{randomY, randomX};
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
            Thread.sleep(7000);
            System.out.println("Reproduce Cycle Occurring");

            if (actorsInWorld.size() > 25) {
                System.out.println("Already at population cap.");
                continue;
            }

            List<Breeder> breeders = actorsInWorld.stream()
                    .filter(Actor::isAlive)
                    .map(actor -> ((Breeder)actor))
                    .filter(Breeder::isFitToBreed).toList();

            List<Breeder> orderedByPerformance = breeders.stream()
                    .sorted(Comparator.comparingInt(Breeder::getFitnessScore).reversed()).toList();
            System.out.println(orderedByPerformance.size() + " to breed. The top 3 will reproduce.");

            int newChildCount = Math.min(orderedByPerformance.size(), 3);

            for(int x = 0; x < newChildCount; x++) {
                LivingThing parent = (LivingThing) orderedByPerformance.get(x);

                LivingThing child = new LivingThing(this, parent.getNeuralNetwork(), 0.05, parent.getTicks());

                this.addActorInRandomPlace(child);
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

    public List<Actor> getActorsInWorld() {
        return actorsInWorld;
    }

    public int[] getActorCoords(Actor actor) {
        return coordsLookupMap.get(actor);
    }

    public void cleanUpCorpse(Actor actor) {
        coordsLookupMap.remove(actor);
        actorsInWorld.remove(actor);
    }
}
