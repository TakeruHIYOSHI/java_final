package com.zenn.uno.application.input;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;

import org.springframework.stereotype.Service;

@Service
public class DrawCardUseCase {
    private final GameRepository repository;
    private final TurnProcessingService turnService;

    public DrawCardUseCase(GameRepository repository, TurnProcessingService turnService) {
        this.repository = repository;
        this.turnService = turnService;
    }

    public GameDto execute(String gameId, String playerId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getState() != Game.GameState.PLAYING) {
            throw new IllegalStateException("Game is not active");
        }

        // Validate Turn
        Player currentPlayer = game.getTurnManager().getCurrentPlayer();
        if (!currentPlayer.getId().equals(playerId)) {
            throw new IllegalStateException("Not your turn");
        }

        // Draw Logic
        if (game.getDeck().isEmpty()) {
            game.getDeck().refill(game.getDiscardPile());
        }
        if (game.getDeck().isEmpty()) {
            throw new IllegalStateException("Deck empty");
        }

        Card drawn = game.getDeck().draw();
        currentPlayer.addCard(drawn);

        // Rule Check: Can play immediately?
        // If NO -> Next Turn.
        // If YES -> Player keeps turn (Client needs to prompt user to play it or keep
        // it).
        // Wait, typical API for web game:
        // If I draw, I get the card. I can then call Play API immediately if I want.
        // BUT if I CANNOT play it, I am forced to pass.
        // To simplify UX:
        // Return State. User sees new card. If they can play, they click it.
        // If they cannot/don't want to play (if we allow passing), they need a "Pass"
        // action.
        // Requirement said: "POST /draw -> state". And "If Human...".
        // Let's implement auto-pass if unplayable.

        if (!game.canPlay(drawn)) {
            // Cannot play -> Turn Ends
            game.getTurnManager().nextTurn();
            // And Process CPUs
            turnService.processCpuTurns(game);
        } else {
            // Can play -> Turn stays with Human. Human must perform another action (Play).
            // What if user wants to keep it? We need a "Pass" API?
            // Requirement didn't list Pass API.
            // "POST /api/games/{id}/actions/play" and "draw".
            // Assuming strict rule: If you draw and can play, you MUST play? Or you CAN
            // play?
            // Usually you CAN play.
            // If I don't implement Pass, user is stuck if they draw a playable card but
            // don't want to play it (strategic hoarding).
            // But for this MVP, let's assume:
            // If can play, wait for user input (Play). User needs to explicitly click the
            // card.
            // If user doesn't want to play... they are stuck?
            // Let's assume standard UNO: You draw. If playable, you can play. Use Play API.
            // If user wants to pass... we need Pass API.
            // Given constraints "API（増やさない）", maybe Draw acts as Pass if called again? No.
            // Let's assume: If playable, user IS expected to play it. Or we just don't
            // support Pass for playable card.
            repository.save(game);
        }

        return GameDto.from(game);
    }
}
