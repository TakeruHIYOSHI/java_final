package com.zenn.uno.application.input;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Game;

import org.springframework.stereotype.Service;

@Service
public class ProcessCpuTurnUseCase {
    private final GameRepository repository;
    private final TurnProcessingService turnService;

    public ProcessCpuTurnUseCase(GameRepository repository, TurnProcessingService turnService) {
        this.repository = repository;
        this.turnService = turnService;
    }

    public GameDto execute(String gameId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));

        if (game.getState() != Game.GameState.PLAYING) {
            throw new IllegalStateException("Game is not active");
        }

        // Execute single turn
        turnService.processSingleCpuTurn(game);

        return GameDto.from(game);
    }
}
