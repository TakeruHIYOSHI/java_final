package com.zenn.uno.domain.model;

import java.util.List;

public class TurnManager {
    private final List<Player> players;
    private int currentIndex;
    private Direction direction;

    public List<Player> getPlayers() {
        return players;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public Direction getDirection() {
        return direction;
    }

    public TurnManager(List<Player> players) {
        this.players = players;
        this.currentIndex = 0; // P0 starts
        this.direction = Direction.CLOCKWISE;
    }

    public Player getCurrentPlayer() {
        return players.get(currentIndex);
    }

    public Player getNextPlayer() {
        int nextIndex = calculateNextIndex(1);
        return players.get(nextIndex);
    }

    public void nextTurn() {
        currentIndex = calculateNextIndex(1);
    }

    public void skipTurn() {
        currentIndex = calculateNextIndex(2); // Move 2 steps
    }

    public void reverse() {
        if (direction == Direction.CLOCKWISE) {
            direction = Direction.COUNTER_CLOCKWISE;
        } else {
            direction = Direction.CLOCKWISE;
        }
        // In 2 player game, reverse acts like skip. But we have fixed 4 players.
        // So reverse just swaps direction.
        // Wait, standard UNO rule: reverse changes direction. The NEXT player is
        // determined by new direction.
        // Yes, handled by nextTurn() using current direction.
    }

    private int calculateNextIndex(int step) {
        int dir = (direction == Direction.CLOCKWISE) ? 1 : -1;
        int size = players.size();
        int next = (currentIndex + (step * dir)) % size;
        if (next < 0)
            next += size;
        return next;
    }
}
