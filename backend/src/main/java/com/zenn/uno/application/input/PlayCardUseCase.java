package com.zenn.uno.application.input;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;

import org.springframework.stereotype.Service;

@Service
public class PlayCardUseCase {
    private final GameRepository repository;
    private final TurnProcessingService turnService;

    public PlayCardUseCase(GameRepository repository, TurnProcessingService turnService) {
        this.repository = repository;
        this.turnService = turnService;
    }

    public GameDto execute(String gameId, String playerId, java.util.List<Integer> cardIndices, Color declaredColor) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getState() != Game.GameState.PLAYING) {
            throw new IllegalStateException("Game is not active");
        }

        Player player = game.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Player not found"));

        // Validate Turn
        if (!game.getTurnManager().getCurrentPlayer().getId().equals(playerId)) {
            throw new IllegalStateException("Not your turn");
        }

        if (cardIndices == null || cardIndices.isEmpty()) {
            throw new IllegalArgumentException("No cards selected");
        }

        // Validate Indices and Ownership
        java.util.List<Card> cardsToPlay = new java.util.ArrayList<>();
        // Sort indices descending to avoid shifting when removing (though here we just
        // get them first)
        // But validation logic is sequence based. User sends ordered list?
        // Let's assume user sends indices in order of play intent, or we just treat
        // them as a set.
        // Wait, if I play Red 5 and Blue 5. Which one is first? The order matters if
        // Top is Red 7.
        // User should send [Index of Red 5, Index of Blue 5].

        // Fetch cards
        for (Integer index : cardIndices) {
            if (index < 0 || index >= player.getHand().size()) {
                throw new IllegalArgumentException("Invalid card index: " + index);
            }
            cardsToPlay.add(player.getHand().get(index));
        }

        // Validate Multi-Play Rules
        // 1. First card must match Game rule (canPlay)
        // 2. Subsequent cards must match the NUMBER of the First card (User Rule: "Same
        // Number")

        Card firstCard = cardsToPlay.get(0);
        if (!game.canPlay(firstCard)) {
            throw new IllegalStateException("Cannot play first card: " + firstCard);
        }

        if (cardsToPlay.size() > 1) {
            Integer baseNumber = firstCard.getNumber();
            // Rule: "Same number".
            // If first card is Action? (Skip, Reverse, etc). Official: "Same Symbol/Rank".
            // Card.getNumber() returns null for Action.
            // If card is Action, we check Type.

            for (int i = 1; i < cardsToPlay.size(); i++) {
                Card c = cardsToPlay.get(i);
                if (baseNumber != null) {
                    // Number card
                    if (!baseNumber.equals(c.getNumber())) {
                        throw new IllegalArgumentException("All cards must have the same number: " + baseNumber);
                    }
                } else {
                    // Action card? Or Wild?
                    // If Action, types must match.
                    if (firstCard.getType() != c.getType()) {
                        throw new IllegalArgumentException("All cards must have the same type: " + firstCard.getType());
                    }
                    // Wild?
                    if (firstCard.isWild()) {
                        // Wild usually can't be stacked unless house rule.
                        // For simplicity: Allow stacking Wilds?
                        // "Same Number" usually implies Numbers. Let's allow Actions if Type matches.
                        // But forbid mixing Number and Action (covered by logic above).
                    }
                }
                // Colors can provide anything.
            }
        }

        if (firstCard.isWild() && declaredColor == null) {
            // If ANY card is wild, we need declared color? Usually last card counts.
            // If last card is Wild, we need color.
            // If I play Wild then Red 5? (Illogical if "Same Number").
            // If I play Red 5 then Wild? (Wild matches anything).
            // But rule is "Same Number". Wild doesn't have number.
            // So Multi-play is ONLY for non-wild cards usually?
            // Or Wilds matching Wilds.
            // Let's assume: If Last Card is Wild, need declaredColor.
            Card lastCard = cardsToPlay.get(cardsToPlay.size() - 1);
            if (lastCard.isWild() && declaredColor == null) {
                throw new IllegalArgumentException("Must declare color for Wild card");
            }
        }

        // Execution
        // We need to coordinate with TurnProcessingService.
        // It's cleaner to handle the "Batch" here or add a batch method to Service.
        // Problem: TurnProcessingService logic modifies Game state (discard, etc).
        // And checks Win validation.
        // If I play 2 cards, and 1st one emptied hand? (Impossible, I have 2).

        // Remove cards from hand (Indices change!) -> Better to remove by Object
        // instance?
        // Or remove by Index DESCENDING.

        // Strategy: Process one by one effectively.
        // BUT `processTurn` calls `nextTurn`. We don't want that for intermediate
        // cards.

        turnService.processTurnBatch(game, player, cardsToPlay, declaredColor);

        return GameDto.from(game);
    }
}
