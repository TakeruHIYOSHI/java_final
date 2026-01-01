package com.zenn.uno.domain.rule.effect;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.CardType;
import com.zenn.uno.domain.model.Direction;
import com.zenn.uno.domain.model.Player;
import com.zenn.uno.domain.rule.CardEffect;
import com.zenn.uno.domain.rule.CardPlayContext;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.rule.EffectRegistry;
import jakarta.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SwapEffect implements CardEffect {
    @Autowired
    private EffectRegistry registry;

    @PostConstruct
    public void register() {
        registry.register(CardType.SWAP, this);
    }

    @Override
    public void apply(Game game, CardPlayContext context) {
        List<Player> players = game.getPlayers();
        int size = players.size();
        Direction direction = game.getTurnManager().getDirection();
        int shift = (direction == Direction.CLOCKWISE) ? 1 : -1;

        // Snapshot all hands first
        List<List<Card>> snapshots = players.stream()
                .map(p -> new ArrayList<>(p.getHand()))
                .collect(Collectors.toList());

        // Apply swap
        // Player i gets hand of Player (i - shift)
        // If CLOCKWISE (+1): P1 gets P0, P2 gets P1... P0 gets P3.
        // Wait, "Swap with neighbor".
        // Requirement: "Next neighbor" (direction based).
        // If I am P0, CLOCKWISE, my next is P1.
        // Do I swap with P1? Or does EVERYONE rotate hands?
        // Requirement: "Everyone swaps hand with neighbor".
        // Interpretation: Hands rotate around the table.
        // Clockwise: Hand 0 -> Hand 1 -> Hand 2 -> Hand 3 -> Hand 0

        for (int i = 0; i < size; i++) {
            // Source index from where this player receives the hand
            int sourceIndex = (i - shift) % size;
            if (sourceIndex < 0)
                sourceIndex += size;

            players.get(i).setHand(snapshots.get(sourceIndex));
        }
    }
}
