package com.zenn.uno.domain.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Game {
    private final String id;
    private final List<Player> players;
    private final Deck deck;
    private final List<Card> discardPile;
    private final TurnManager turnManager;

    private GameState state;
    private Color currentColor; // Current active color (important for Wilds)
    private String winnerId; // ID of winner

    public String getId() {
        return id;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public Deck getDeck() {
        return deck;
    }

    public List<Card> getDiscardPile() {
        return discardPile;
    }

    public TurnManager getTurnManager() {
        return turnManager;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public enum GameState {
        WAITING,
        PLAYING,
        FINISHED
    }

    public Game(List<Player> players) {
        this.id = UUID.randomUUID().toString();
        this.players = players;
        this.deck = new Deck();
        this.discardPile = new ArrayList<>();
        this.turnManager = new TurnManager(players);
        this.state = GameState.WAITING;
    }

    public void start() {
        // Distribute 7 cards to each player
        for (int i = 0; i < 7; i++) {
            for (Player p : players) {
                p.addCard(deck.draw());
            }
        }

        // Draw first card for discard pile
        Card first = deck.draw();
        discardPile.add(first);

        // Ensure first card is not Wild Draw4 (as per official rules usually, but to
        // simplify, we accept anything)
        // If it's Wild, we set random color or wait for p1 to set?
        // Simplification: If Wild, set RED by default.
        if (first.isWild()) {
            this.currentColor = Color.RED;
        } else {
            this.currentColor = first.getColor();
        }

        this.state = GameState.PLAYING;
    }

    public Card getTopCard() {
        if (discardPile.isEmpty())
            return null;
        return discardPile.get(discardPile.size() - 1);
    }

    public void discard(Card card) {
        discardPile.add(card);
        // If not wild, update current color
        if (!card.isWild()) {
            this.currentColor = card.getColor();
        }
    }

    public boolean canPlay(Card card) {
        Card top = getTopCard();
        // Wilds are always playable
        if (card.isWild())
            return true;

        // Match color (currentColor)
        if (card.getColor() == currentColor)
            return true;

        // Match type
        if (card.getType() == top.getType())
            return true;

        // Match number
        if (card.getType() == CardType.NUMBER && top.getType() == CardType.NUMBER &&
                card.getNumber().equals(top.getNumber())) {
            return true;
        }

        return false;
    }

    // Check if player has won
    public void checkWinCondition() {
        for (Player p : players) {
            if (p.getHand().isEmpty()) {
                this.state = GameState.FINISHED;
                this.winnerId = p.getId();
                break;
            }
        }
    }
}
