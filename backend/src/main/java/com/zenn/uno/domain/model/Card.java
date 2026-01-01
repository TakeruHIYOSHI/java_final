package com.zenn.uno.domain.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class Card {
    private final Color color;
    private final CardType type;
    private final Integer number; // 0-9, null for actions

    public Card(Color color, CardType type, Integer number) {
        this.color = color;
        this.type = type;
        this.number = number;
    }

    public static Card createNumber(Color color, int number) {
        return new Card(color, CardType.NUMBER, number);
    }

    public static Card createAction(Color color, CardType type) {
        return new Card(color, type, null);
    }

    public static Card createWild(CardType type) {
        return new Card(Color.BLACK, type, null);
    }

    public boolean isWild() {
        return color == Color.BLACK;
    }

    public boolean matches(Card other) {
        // Can play if colors match
        if (this.color == other.color)
            return true;
        // Can play if types match (e.g. Skip on Skip)
        if (this.type == other.type)
            return true;
        // Can play if numbers match (e.g. Red 5 on Blue 5)
        if (this.type == CardType.NUMBER && other.type == CardType.NUMBER &&
                this.number != null && this.number.equals(other.number)) {
            return true;
        }
        return false;
    }
}
