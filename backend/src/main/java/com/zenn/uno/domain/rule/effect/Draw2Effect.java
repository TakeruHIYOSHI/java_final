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
public class Draw2Effect implements CardEffect {
    @Autowired
    private EffectRegistry registry;

    @PostConstruct
    public void register() {
        registry.register(CardType.DRAW2, this);
    }

    @Override
    public void apply(Game game, CardPlayContext context) {
        // Draw 2 for next player
        Player victim = game.getTurnManager().getNextPlayer();
        for (int i = 0; i < 2; i++) {
            if (game.getDeck().isEmpty()) {
                game.getDeck().refill(game.getDiscardPile()); // Simplified refill logic
            }
            if (!game.getDeck().isEmpty()) {
                victim.addCard(game.getDeck().draw());
            }
        }
        game.getTurnManager().skipTurn(); // Skip the victim
    }
}
