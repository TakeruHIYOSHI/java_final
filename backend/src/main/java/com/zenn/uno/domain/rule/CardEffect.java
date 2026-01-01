package com.zenn.uno.domain.rule;

import com.zenn.uno.domain.model.Game;

public interface CardEffect {
    void apply(Game game, CardPlayContext context);
}
