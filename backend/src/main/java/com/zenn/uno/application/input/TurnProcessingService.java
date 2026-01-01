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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TurnProcessingService {
    private final EffectRegistry effectRegistry;
    private final CpuPolicy cpuPolicy;
    private final GameRepository repository;

    public void processTurn(Game game, Player player, Card card, Color declaredColor) {
        // Remove card from hand
        player.removeCard(card);
        game.discard(card);

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

        // Move to next turn (unless effect already skipped/reversed logic inside
        // TurnManager?
        // No, effects manipulate TM state (skip/reverse). We always call nextTurn() at
        // the end?
        // Wait.
        // Normal card: nextTurn().
        // Skip card: skipTurn() (moves +2).
        // Reverse card: reverse(). Then we still need nextTurn()?
        // If Reverse flips direction, nextTurn() moves +1 in new direction. Correct.
        // My SkipEffect calls skipTurn().
        // My ReverseEffect calls reverse().
        // My Draw2Effect calls skipTurn() (victim draws 2 and is skipped).

        // ISSUE: If I call nextTurn() HERE, it might double-skip if effect also moved
        // it.
        // Let's refine effects.
        // Option A: Effects handle ALL turn movement.
        // Option B: Effects only handle SPECIAL movement. Normal flow is handled here.

        // Let's assume effects handle special stuff.
        // If card is NUMBER, we need nextTurn().
        // If card is SKIP, effect called skipTurn(). That effectively did +2.
        // BUT wait. If currentIndex is 0. Skip -> +2 -> 2.
        // If I call nextTurn() again -> 3. Too much.

        // Solution: TurnManager should NOT be mutated by Effects for "Next Turn" logic
        // EXCEPT for Skip/Reverse direction.
        // Actually, simpler:
        // 1. Apply effects.
        // 2. ALWAYS call nextTurn() UNLESS the effect EXPLICITLY managed the
        // transition?
        // No, standard Clean Arch: "Rules" define state change.

        // Let's stick to: Effect applies Changes to Game State.
        // Number Card -> No effect.
        // Skip -> Moves pointer?

        // Refactor: Let TurnManager be dumb. EffectRegistry has default effect for
        // NUMBER? No.

        // Backtrack: Standard UNO flow.
        // 1. Player plays card. checks valid.
        // 2. Card effect applied. (e.g. Skip increments index extra).
        // 3. Turn passes to next player.

        // If I play Skip:
        // Index 0. Direction +1.
        // Effect runs: skipTurn() -> index becomes 2 (P2).
        // If I run nextTurn() -> index becomes 3. WRONG.

        // CORRECT LOGIC:
        // Start: Index 0.
        // Play Skip.
        // Effect: set "skipNext" flag? Or modifying TM directly?

        // Let's look at my TM. skipTurn() does `currentIndex = calc(2)`.
        // This is "Move to next of next".
        // UseCase should NOT call nextTurn() if effect already moved it?

        // Better: CardEffect returns explicitly if it handled turn?
        // OR: UseCase calls `game.getTurnManager().nextTurn()` ONLY if card was Number?

        // Let's rely on Game state.
        // Actually, for simplicity in this short task:
        // Effects that change turn (Skip, Draw2, WildDraw4) calling
        // `game.getTurnManager().skipTurn()` sets the index to the NEXT ACTIVE player.
        // So the UseCase should NOT advance turn again.

        // Effects that DO NOT change turn (Number, Wild, Reverse, Swap):
        // Reverse changes direction, but index is same. We need nextTurn() to move to
        // neighbor.
        // Wild sets color. Index same. Need nextTurn().
        // Swap sets hands. Index same. Need nextTurn().
        // Number same. Need nextTurn().

        // So:
        // Skip, Draw2, WildDraw4 -> Effect handles turn advance (skipTurn).
        // Others -> UseCase handles turn advance (nextTurn).

        boolean effectAdvancedTurn = isTurnAdvancingCard(card);
        if (!effectAdvancedTurn) {
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

    public void processCpuTurns(Game game) {
        // While game is playing and current player is CPU
        while (game.getState() == Game.GameState.PLAYING &&
                game.getTurnManager().getCurrentPlayer().isCpu()) {

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
                // Draw 1
                if (game.getDeck().isEmpty()) {
                    game.getDeck().refill(game.getDiscardPile());
                }
                if (game.getDeck().isEmpty()) {
                    // Still empty? Hard limit. End game or just skip?
                    // Just skip turn to prevent infinite loop
                    game.getTurnManager().nextTurn();
                    continue;
                }

                Card drawn = game.getDeck().draw();
                cpu.addCard(drawn);

                // Can play?
                if (game.canPlay(drawn)) {
                    // Check policy? Or just play it if CPU policy says so?
                    // Standard CPU: if can play drawn, play it.
                    // Re-evaluate policy for this single card?
                    // Simplification: Always play if possible.
                    Color color = null;
                    if (drawn.isWild()) {
                        color = cpuPolicy.selectColor(game, cpu);
                    }
                    processTurn(game, cpu, drawn, color);
                } else {
                    game.getTurnManager().nextTurn();
                }
            }
        }
        repository.save(game);
    }
}
