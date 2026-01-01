package com.zenn.uno.domain.rule.cpu;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.CardType;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;
import com.zenn.uno.domain.rule.CpuPolicy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CpuPolicyImpl implements CpuPolicy {

    @Value("${uno.cpu.difficulty:NORMAL}")
    private String difficulty;

    @Override
    public Card selectCard(Game game, Player me) {
        List<Card> playable = me.getHand().stream()
                .filter(game::canPlay)
                .collect(Collectors.toList());

        if (playable.isEmpty()) {
            return null; // Must draw
        }

        if ("HARD".equalsIgnoreCase(difficulty)) {
            return selectHard(game, me, playable);
        } else {
            return selectNormal(game, me, playable);
        }
    }

    @Override
    public Color selectColor(Game game, Player me) {
        // Find most frequent color in hand
        return me.getHand().stream()
                .filter(c -> c.getColor() != Color.BLACK)
                .collect(Collectors.groupingBy(Card::getColor, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(Color.RED); // Default if only wild
    }

    private Card selectNormal(Game game, Player me, List<Card> playable) {
        // Priority: Blocker (Skip, Draw2, Reverse, WildDraw4) > Others
        // Check next player's hand size
        Player nextPlayer = game.getTurnManager().getNextPlayer();
        boolean danger = nextPlayer.getHandSize() <= 2;

        return playable.stream()
                .sorted((c1, c2) -> {
                    int s1 = getBlockerScore(c1);
                    int s2 = getBlockerScore(c2);
                    if (s1 != s2)
                        return s2 - s1; // Descending
                    // Prefer Wild over WildDraw4 if not danger? No, Rule says Wild is most freq
                    // color.
                    // Just return first
                    return 0;
                })
                .findFirst()
                .orElse(playable.get(0));
    }

    private int getBlockerScore(Card c) {
        switch (c.getType()) {
            case WILD_DRAW4:
                return 10;
            case DRAW2:
                return 8;
            case SKIP:
                return 6;
            case REVERSE:
                return 5; // Changes flow, good
            case SWAP:
                return 9; // Chaos
            case WILD:
                return 1;
            default:
                return 0;
        }
    }

    private Card selectHard(Game game, Player me, List<Card> playable) {
        // Scoring based on:
        // - Base score (Action > Number)
        // - Next player hand size (Target low hand size players with attacks)
        // - Keep Wilds for end game (unless emergency)
        // - Swap if I have many cards and target has few

        Player nextPlayer = game.getTurnManager().getNextPlayer();

        return playable.stream()
                .max(Comparator.comparingInt(c -> calculateScore(c, me, nextPlayer)))
                .orElse(playable.get(0));
    }

    private int calculateScore(Card c, Player me, Player next) {
        int score = 0;

        // Base value
        if (c.getType() == CardType.NUMBER)
            score += 1;
        else
            score += 5;

        // Attack bonus if next player is winning
        if (next.getHandSize() <= 3) {
            if (c.getType() == CardType.DRAW2)
                score += 50;
            if (c.getType() == CardType.WILD_DRAW4)
                score += 60;
            if (c.getType() == CardType.SKIP)
                score += 30;
        }

        // Swap strategy: If I have many cards (>5) and neighbor has few (<3), High
        // Priority
        // NOTE: Exact direction matters for Swap but let's assume Clockwise logic for
        // simplicity or check TurnManager
        if (c.getType() == CardType.SWAP) {
            if (me.getHandSize() > 5 && next.getHandSize() < 3) {
                score += 100;
            } else if (me.getHandSize() < next.getHandSize()) {
                score -= 50; // Don't swap if I have fewer
            }
        }

        // Save Wilds
        if (c.getType() == CardType.WILD || c.getType() == CardType.WILD_DRAW4) {
            if (me.getHandSize() > 2)
                score -= 10; // Save for later
        }

        return score;
    }
}
