package com.zenn.uno.application.input;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;
import com.zenn.uno.domain.rule.CardEffect;
import com.zenn.uno.domain.rule.CardPlayContext;
import com.zenn.uno.domain.rule.CpuPolicy;
import com.zenn.uno.domain.rule.EffectRegistry;
import java.util.Optional;

import com.zenn.uno.domain.model.LastAction;
import org.springframework.stereotype.Service;

@Service
public class TurnProcessingService {
    private final EffectRegistry effectRegistry;
    private final CpuPolicy cpuPolicy;
    private final GameRepository repository;

    public TurnProcessingService(EffectRegistry effectRegistry, CpuPolicy cpuPolicy, GameRepository repository) {
        this.effectRegistry = effectRegistry;
        this.cpuPolicy = cpuPolicy;
        this.repository = repository;
    }

    public void processTurnBatch(Game game, Player player, java.util.List<Card> cards, Color declaredColor) {
        // Iterate through all cards
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            boolean isLast = (i == cards.size() - 1);

            // For intermediate cards, we do NOT advance turn.
            // For last card, we DO advance turn (if rule says so).

            // However, effect logic inside processTurn() handles turn advancement.
            // We need a version of processTurn that takes a flag "advanceTurn".

            // Actually, if I play Skip1, Skip2.
            // Skip1 effect: skipTurn().
            // Skip2 effect: skipTurn().
            // Total: Skip 2 players?
            // If stacking is allowed, effects usually stack.
            // So we SHOULD apply effects for every card.

            // BUT for turn ADVANCEMENT (standard +1), it should only happen at the end.
            // My processTurn() logic:
            // 1. Apply Effect.
            // 2. If Effect NOT Skip/Draw2/etc, call nextTurn().

            // If I play Number1, Number2.
            // Number1: No effect. nextTurn(). -> Turn moves.
            // Number2: ... Wait, if turn moved, next player is current.
            // But batch play means *I* play all of them.

            // So we need `processTurnInternal(game, player, card, color, boolean
            // isFinalCard)`?
            processTurnInternal(game, player, card, isLast ? declaredColor : null, isLast);
        }
    }

    public void processTurn(Game game, Player player, Card card, Color declaredColor) {
        processTurnInternal(game, player, card, declaredColor, true);
    }

    private void processTurnInternal(Game game, Player player, Card card, Color declaredColor, boolean isFinalCard) {
        // Remove card from hand
        player.removeCard(card);
        game.discard(card);
        // Only set Last Action for the LAST card played? Or all?
        // UI wants to see "Played Red 5 (and Blue 5)".
        // Simple: Set LastAction for each. UI bubbles might overwrite or flash.
        // Let's set it.
        game.setLastAction(new LastAction(player.getId(), LastAction.ActionType.PLAY, card));

        // Apply Effect
        CardEffect effect = effectRegistry.getEffect(card.getType());
        CardPlayContext context = CardPlayContext.builder()
                .playedCard(card)
                .declaredColor(declaredColor) // can be null
                .player(player)
                .build();
        effect.apply(game, context);

        // Check Win
        game.checkWinCondition();
        if (game.getState() == Game.GameState.FINISHED) {
            return;
        }

        // Turn Advancement Logic
        // Only trigger standard nextTurn() if it's the Final Card AND effect didn't
        // move turn.
        // Wait, if intermediate card is "Skip", it moves turn pointer.
        // If I play Skip, then Number.
        // Skip -> Moves pointer +2 (skipping next).
        // Then I play Number. Valid?
        // "Same Number" usually applies to Number cards. You can't play Skip then
        // Reverse.
        // But if I play Skip Red, Skip Blue.
        // Skip Red -> effect skipTurn() (Next player skipped).
        // Skip Blue -> effect skipTurn() (Next-Next player skipped).
        // This effectively skips 2 people.
        // This seems correct for "Stacking".

        // However, standard Number cards do NOT invoke effect.
        // If I play Red 5, Blue 5.
        // Red 5: No effect.
        // Blue 5: No effect.
        // If I don't call nextTurn(), turn stays with me. Correct.
        // So for intermediate cards, we skip the `nextTurn()` call.

        // CAUTION: Effect implementations (Draw2, Skip) CALL `skipTurn()`.
        // This modifies TurnManager state immediately.
        // This is fine. We want effects to apply.

        // The only thing we suppress is the *automatic* `nextTurn()` for non-action
        // cards,
        // UNLESS it's the last card.

        boolean effectAdvancedTurn = isTurnAdvancingCard(card); // Skip, Draw2, WildDraw4

        if (isFinalCard && !effectAdvancedTurn) {
            game.getTurnManager().nextTurn();
        }
    }

    private boolean isTurnAdvancingCard(Card card) {
        switch (card.getType()) {
            case SKIP:
            case DRAW2:
            case WILD_DRAW4:
                return true;
            default:
                return false;
        }
    }

    /**
     * Executes a SINGLE turn for the current CPU player.
     * 
     * @param game The game instance
     * @return true if a CPU turn was processed, false if it's not a CPU turn or
     *         game not playing.
     */
    public boolean processSingleCpuTurn(Game game) {
        if (game.getState() != Game.GameState.PLAYING ||
                !game.getTurnManager().getCurrentPlayer().isCpu()) {
            return false;
        }

        Player cpu = game.getTurnManager().getCurrentPlayer();

        // 1. Select Card
        Card card = cpuPolicy.selectCard(game, cpu);

        if (card != null) {
            // Play
            Color color = null;
            if (card.isWild()) {
                color = cpuPolicy.selectColor(game, cpu);
            }
            processTurn(game, cpu, card, color);
        } else {
            // Draw
            if (game.getDeck().isEmpty()) {
                game.getDeck().refill(game.getDiscardPile());
            }

            if (game.getDeck().isEmpty()) {
                // Skip if deck still empty
                game.getTurnManager().nextTurn();
                repository.save(game);
                return true;
            }

            Card drawn = game.getDeck().draw();
            cpu.addCard(drawn);

            // Can play?
            if (game.canPlay(drawn)) {
                // Determine color for wild if needed
                Color color = null;
                if (drawn.isWild()) {
                    color = cpuPolicy.selectColor(game, cpu);
                }
                processTurn(game, cpu, drawn, color);
            } else {
                game.setLastAction(new LastAction(cpu.getId(), LastAction.ActionType.DRAW, null));
                game.getTurnManager().nextTurn();
            }
        }

        repository.save(game);
        return true;
    }

    // Deprecated/Removed: processCpuTurns (batch processing)

}
