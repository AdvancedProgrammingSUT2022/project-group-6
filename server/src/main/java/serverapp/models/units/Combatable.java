package serverapp.models.units;

import serverapp.models.Player;

public interface Combatable {
    Player getOwner();

    String attack(Combatable defender);

    String defend(Combatable attacker);

    void healPerTurn();

    boolean isInPossibleCombatRange(int x, int y, int seenRange, int attackerX, int attackerY);

    int getX();

    int getY();
}
