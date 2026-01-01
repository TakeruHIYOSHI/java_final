package com.zenn.uno.application.input;

import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.domain.model.Game;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetGameStateUseCase {
    private final GameRepository repository;

    public GameDto execute(String gameId) {
        Game game = repository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("Game not found"));
        return GameDto.from(game);
    }
}
