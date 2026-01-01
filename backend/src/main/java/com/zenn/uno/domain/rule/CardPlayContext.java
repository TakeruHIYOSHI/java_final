package com.zenn.uno.domain.rule;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Player;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CardPlayContext {
    private final Card playedCard;
    private final Color declaredColor;
    private final Player player;
}
