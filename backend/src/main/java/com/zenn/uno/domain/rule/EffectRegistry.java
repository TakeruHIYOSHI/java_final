package com.zenn.uno.domain.rule;

import com.zenn.uno.domain.model.CardType;
import java.util.EnumMap;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class EffectRegistry {
    private final Map<CardType, CardEffect> effects = new EnumMap<>(CardType.class);

    public void register(CardType type, CardEffect effect) {
        effects.put(type, effect);
    }

    public CardEffect getEffect(CardType type) {
        return effects.getOrDefault(type, (game, context) -> {
        }); // Default NoOp
    }
}
