package com.zenn.uno.application.input;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PlayCardUseCase {
    private final GameRepository repository;
    private final TurnProcessingService turnService;

    public GameDto execute(String gameId, String playerId, Integer cardIndex, Color declaredColor) {
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

        // Validate Card Ownership
        if (cardIndex < 0 || cardIndex >= player.getHand().size()) {
            throw new IllegalArgumentException("Invalid card index");
        }
        Card card = player.getHand().get(cardIndex);

        // Validate Rule
        if (!game.canPlay(card)) {
            throw new IllegalStateException("Cannot play this card: " + card);
        }

        if (card.isWild() && declaredColor == null) {
            throw new IllegalArgumentException("Must declare color for Wild card");
        }

        // Process Human Turn
        turnService.processTurn(game, player, card, declaredColor);

        // Process CPU Turns
        turnService.processCpuTurns(game);

        return GameDto.from(game);
    }
}
