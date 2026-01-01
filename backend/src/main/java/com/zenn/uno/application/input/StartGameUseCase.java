package com.zenn.uno.application.input;

import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class StartGameUseCase {
    private final GameRepository repository;

    public String execute() {
        // Create 4 players
        List<Player> players = new ArrayList<>();
        players.add(new Player("0", "You", false)); // Human
        players.add(new Player("1", "CPU1", true));
        players.add(new Player("2", "CPU2", true));
        players.add(new Player("3", "CPU3", true));

        Game game = new Game(players);
        game.start();
        repository.save(game);

        return game.getId();
    }
}
