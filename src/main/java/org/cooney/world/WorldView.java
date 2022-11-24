package org.cooney.world;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.SwingTerminalFontConfiguration;
import org.cooney.world.items.WorldItem;
import org.cooney.world.map.RandomWorldSeeder;
import org.cooney.world.map.RiverWorldSeeder;

import java.io.IOException;

public class WorldView {
    private static final int WORLD_HEIGHT =150;
    private static final int WORLD_WIDTH = 50;
    private final WorldEngine worldEngine = new WorldEngine(WORLD_HEIGHT, WORLD_WIDTH, new RiverWorldSeeder());
    public void render(Terminal terminal) throws IOException {
        for(int y = 0; y < WORLD_HEIGHT; y++) {
            for(int x = 0; x < WORLD_WIDTH; x++) {
                WorldItem worldItem = worldEngine.getItemAt(y, x).getWorldItem();
                terminal.setCursorPosition(y, x);
                terminal.setBackgroundColor(TextColor.Factory.fromString(worldItem.getColourCode()));
                terminal.putString(worldItem.getCharacterCode());
            }
        }

        terminal.flush();
    }
    public void run() {
        try {
            worldEngine.begin();
            Terminal terminal = new DefaultTerminalFactory().setTerminalEmulatorFontConfiguration(SwingTerminalFontConfiguration.getDefaultOfSize(10)).createTerminal();

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
