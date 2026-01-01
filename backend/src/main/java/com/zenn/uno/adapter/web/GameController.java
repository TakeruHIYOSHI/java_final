package com.zenn.uno.adapter.web;

import com.zenn.uno.adapter.web.dto.GameDto;
import com.zenn.uno.application.input.DrawCardUseCase;
import com.zenn.uno.application.input.GetGameStateUseCase;
import com.zenn.uno.application.input.PlayCardUseCase;
import com.zenn.uno.application.input.ProcessCpuTurnUseCase;
import com.zenn.uno.application.input.StartGameUseCase;
import com.zenn.uno.domain.model.Color;
import java.util.Map;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/games")
@CrossOrigin(origins = "*") // Allow frontend dev server
public class GameController {
    private final StartGameUseCase startGameUseCase;
    private final GetGameStateUseCase getGameStateUseCase;
    private final PlayCardUseCase playCardUseCase;
    private final DrawCardUseCase drawCardUseCase;
    private final ProcessCpuTurnUseCase processCpuTurnUseCase;

    public GameController(StartGameUseCase startGameUseCase,
            GetGameStateUseCase getGameStateUseCase,
            PlayCardUseCase playCardUseCase,
            DrawCardUseCase drawCardUseCase,
            ProcessCpuTurnUseCase processCpuTurnUseCase) {
        this.startGameUseCase = startGameUseCase;
        this.getGameStateUseCase = getGameStateUseCase;
        this.playCardUseCase = playCardUseCase;
        this.drawCardUseCase = drawCardUseCase;
        this.processCpuTurnUseCase = processCpuTurnUseCase;
    }

    @PostMapping("/start")
    public Map<String, String> start() {
        String gameId = startGameUseCase.execute();
        return Map.of("gameId", gameId);
    }

    @GetMapping("/{id}/state")
    public GameDto getState(@PathVariable String id) {
        return getGameStateUseCase.execute(id);
    }

    @PostMapping("/{id}/actions/play")
    public GameDto play(
            @PathVariable String id,
            @RequestBody PlayRequest request) {
        return playCardUseCase.execute(id, request.playerId(), request.cardIndices(), request.declaredColor());
    }

    @PostMapping("/{id}/actions/draw")
    public GameDto draw(
            @PathVariable String id,
            @RequestBody DrawRequest request) {
        return drawCardUseCase.execute(id, request.playerId());
    }

    @PostMapping("/{id}/actions/cpu-turn")
    public GameDto processCpuTurn(@PathVariable String id) {
        return processCpuTurnUseCase.execute(id);
    }

    public record PlayRequest(String playerId, java.util.List<Integer> cardIndices, Color declaredColor) {
    }

    public record DrawRequest(String playerId) {
    }
}
