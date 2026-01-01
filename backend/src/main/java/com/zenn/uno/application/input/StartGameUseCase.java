package com.zenn.uno.application.input;

import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Player;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class StartGameUseCase {
    private final GameRepository repository; // RepositotyにGameオブジェクトを保存する

    public StartGameUseCase(GameRepository repository) {
        this.repository = repository;
    }

    public String execute() {
        // Create 4 players
        List<Player> players = new ArrayList<>();
        players.add(new Player("0", "You", false)); // Human
        players.add(new Player("1", "CPU1", true));
        players.add(new Player("2", "CPU2", true));
        players.add(new Player("3", "CPU3", true));

        Game game = new Game(players);
        game.start();
        repository.save(game); // RepositoryにGameオブジェクトを保存する

        return game.getId();
    }
}
