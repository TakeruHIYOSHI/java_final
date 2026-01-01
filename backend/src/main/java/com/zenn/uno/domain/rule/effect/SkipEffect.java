package com.zenn.uno.domain.rule.effect;

import com.zenn.uno.domain.model.CardType;
import com.zenn.uno.domain.rule.CardEffect;
import com.zenn.uno.domain.rule.CardPlayContext;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.rule.EffectRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SkipEffect implements CardEffect {
    @Autowired
    private EffectRegistry registry;

    @PostConstruct
    public void register() {
        registry.register(CardType.SKIP, this);
    }

    @Override
    public void apply(Game game, CardPlayContext context) {
        game.getTurnManager().skipTurn();
    }
}
