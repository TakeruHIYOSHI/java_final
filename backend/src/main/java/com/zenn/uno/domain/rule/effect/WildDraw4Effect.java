package com.zenn.uno.domain.rule.effect;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.CardType;
import com.zenn.uno.domain.model.Player;
import com.zenn.uno.domain.rule.CardEffect;
import com.zenn.uno.domain.rule.CardPlayContext;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.rule.EffectRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WildDraw4Effect implements CardEffect {
    @Autowired
    private EffectRegistry registry;

    @PostConstruct
    public void register() {
        registry.register(CardType.WILD_DRAW4, this);
    }

    @Override
    public void apply(Game game, CardPlayContext context) {
        // Set Color
        if (context.getDeclaredColor() != null) {
            game.setCurrentColor(context.getDeclaredColor());
        }

        // Draw 4 for next player
        Player victim = game.getTurnManager().getNextPlayer();
        for (int i = 0; i < 4; i++) {
            if (game.getDeck().isEmpty()) {
                game.reshuffle();
            }
            if (!game.getDeck().isEmpty()) {
                victim.addCard(game.getDeck().draw());
            }
        }

        game.getTurnManager().skipTurn(); // Skip the victim
    }
}
