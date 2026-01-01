package com.zenn.uno.domain.rule;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;

public interface CpuPolicy {
    Card selectCard(Game game, Player me);

    Color selectColor(Game game, Player me);
}
