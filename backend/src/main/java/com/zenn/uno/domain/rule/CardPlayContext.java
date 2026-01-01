package com.zenn.uno.domain.rule;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Player;

public class CardPlayContext {
    private final Card playedCard;
    private final Color declaredColor;
    private final Player player;

    private CardPlayContext(Builder builder) {
        this.playedCard = builder.playedCard;
        this.declaredColor = builder.declaredColor;
        this.player = builder.player;
    }

    public Card getPlayedCard() {
        return playedCard;
    }

    public Color getDeclaredColor() {
        return declaredColor;
    }

    public Player getPlayer() {
        return player;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private Card playedCard;
        private Color declaredColor;
        private Player player;

        public Builder playedCard(Card playedCard) {
            this.playedCard = playedCard;
            return this;
        }

        public Builder declaredColor(Color declaredColor) {
            this.declaredColor = declaredColor;
            return this;
        }

        public Builder player(Player player) {
            this.player = player;
            return this;
        }

        public CardPlayContext build() {
            return new CardPlayContext(this);
        }
    }
}
