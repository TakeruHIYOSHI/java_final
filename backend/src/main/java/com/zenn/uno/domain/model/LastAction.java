package com.zenn.uno.domain.model;

public class LastAction {
    private final String playerId;
    private final ActionType type;
    private final Card card; // Null if DRAW/PASS (or maybe show drawn card? No, hidden usually)

    public enum ActionType {
        PLAY,
        DRAW,
        PASS
    }

    public LastAction(String playerId, ActionType type, Card card) {
        this.playerId = playerId;
        this.type = type;
        this.card = card;
    }

    public String getPlayerId() {
        return playerId;
    }

    public ActionType getType() {
        return type;
    }

    public Card getCard() {
        return card;
    }
}
