package org.cooney.world.items;

import java.util.List;

public interface Fighter {
    public int attack(List<Fighter> fighter);
    public int getNearbyAlliesCount();
    public int getNearbyEnemiesCount();
    public int getTeamNumber();

    public void takeAHit();
}
