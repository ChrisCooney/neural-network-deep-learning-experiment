package org.cooney.world;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.Button;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import org.cooney.world.items.Actor;
import org.cooney.world.items.WorldItem;
import org.cooney.world.items.WorldItemIds;
import org.cooney.world.items.agents.Direction;
import org.cooney.world.map.FoodOnlySeeder;
import org.cooney.world.map.RandomWorldSeeder;
import org.cooney.world.map.RiverWorldSeeder;
import org.cooney.world.map.SoloActorSeeder;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class WorldView {
    private static final int WORLD_HEIGHT =300;
    private static final int WORLD_WIDTH = 75;
    private final WorldEngine worldEngine = new WorldEngine(WORLD_HEIGHT, WORLD_WIDTH, new RandomWorldSeeder());
    public void render(Terminal terminal) throws IOException {
        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for(int x = 0; x < WORLD_WIDTH; x++) {
                WorldItem worldItem = worldEngine.getItemAt(y, x).getWorldItem();
                terminal.setCursorPosition(y, x);
                terminal.setBackgroundColor(TextColor.Factory.fromString(worldItem.getColourCode()));
                terminal.putString(worldItem.getCharacterCode());
            }
        }

        List<Actor> actorsInWorldCopy = new ArrayList<>(worldEngine.getActorsInWorld());

        for(Actor actor : actorsInWorldCopy) {
            if(actor == null) {
                // Dunno why this happens lol
                continue;
            }
            Direction getFacingDirection = actor.getDirectionIamFacing();

            int[] coords = worldEngine.getActorCoords(actor);

            if (coords == null) {
                System.out.println("Race condition detected. Let's move on from this swiftly...");
                continue;
            }

            if (!actor.isAlive()) {
                continue;
            }

            int x = coords[1];
            int y = coords[0];

            for(int[][] sightLine : getFacingDirection.getFieldOfVisionCoordinateDeltas()) {
                for(int[] cellInSight : sightLine) {
                    int cellXCoord = x + cellInSight[1];

                    if (cellXCoord < 0) {
                        cellXCoord = 0;
                    } else if (cellXCoord >= WORLD_WIDTH) {
                        cellXCoord = WORLD_WIDTH - 1;
                    }

                    int cellYCoord = y + cellInSight[0];

                    if (cellYCoord < 0) {
                        cellYCoord = 0;
                    } else if (cellYCoord >= WORLD_HEIGHT) {
                        cellYCoord = WORLD_HEIGHT - 1;
                    }

                    WorldItem worldItem = worldEngine.getItemAt(cellYCoord, cellXCoord).getWorldItem();

                    if (worldItem.getWorldItemId() == WorldItemIds.EMPTY) {
                        terminal.setCursorPosition(cellYCoord, cellXCoord);
                        terminal.setBackgroundColor(TextColor.Factory.fromString("#222222"));
                        terminal.putString(" ");
                    }
                }
            }

        }

        terminal.setCursorPosition(WORLD_HEIGHT + 20, 10);
        terminal.putString("Total number in Game: " + worldEngine.getActorsInWorld().size());
        terminal.setCursorPosition(1000, 1000);

        terminal.flush();
    }
    public void run() {
        try {
            worldEngine.begin();
            Terminal terminal = new DefaultTerminalFactory()
                    .setTerminalEmulatorFontConfiguration(SwingTerminalFontConfiguration.getDefaultOfSize(10))
                    .setInitialTerminalSize(new TerminalSize(WORLD_HEIGHT + 100, WORLD_WIDTH))
                    .createTerminal();

            while(true) {
                terminal.clearScreen();
                render(terminal);
                terminal.flush();
                Thread.sleep(10);
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
