package com.zenn.uno.domain.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

public class Deck {
    private final Stack<Card> cards;

    public Deck() {
        this.cards = new Stack<>();
        initialize();
    }

    private void initialize() {
        // Red, Blue, Green, Yellow
        for (Color color : List.of(Color.RED, Color.BLUE, Color.GREEN, Color.YELLOW)) {
            // 0: 1 per color
            cards.add(Card.createNumber(color, 0));

            // 1-9: 2 per color
            for (int i = 1; i <= 9; i++) {
                cards.add(Card.createNumber(color, i));
                cards.add(Card.createNumber(color, i));
            }

            // Actions: 2 per color
            for (CardType type : List.of(CardType.SKIP, CardType.REVERSE, CardType.DRAW2)) {
                cards.add(Card.createAction(color, type));
                cards.add(Card.createAction(color, type));
            }

            // SWAP: 1 per color (Custom Extension)
            cards.add(Card.createAction(color, CardType.SWAP));
        }

        // Wilds
        for (int i = 0; i < 4; i++) {
            cards.add(Card.createWild(CardType.WILD));
            cards.add(Card.createWild(CardType.WILD_DRAW4));
        }

        shuffle();
    }

    public void shuffle() {
        Collections.shuffle(cards);
    }

    public Card draw() {
        if (cards.isEmpty()) {
            throw new IllegalStateException("Deck is empty");
        }
        return cards.pop();
    }

    public boolean isEmpty() {
        return cards.isEmpty();
    }

    public void refill(List<Card> discards) {
        cards.addAll(discards);
        shuffle();
    }
}
