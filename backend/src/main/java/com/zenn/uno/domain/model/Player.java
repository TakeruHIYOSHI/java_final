package com.zenn.uno.domain.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
public class Player {
    private final String id;
    private final String name;
    private final boolean isCpu;
    private final List<Card> hand;

    // For CPU, we might inject a Strategy later, but keep it simple in model

    public Player(String id, String name, boolean isCpu) {
        this.id = id;
        this.name = name;
        this.isCpu = isCpu;
        this.hand = new ArrayList<>();
    }

    public void addCard(Card card) {
        hand.add(card);
    }

    public void removeCard(Card card) {
        hand.remove(card);
    }

    public void setHand(List<Card> newHand) {
        this.hand.clear();
        this.hand.addAll(newHand);
    }

    public int getHandSize() {
        return hand.size();
    }
}
