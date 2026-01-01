package com.zenn.uno.application.output;

import com.zenn.uno.domain.model.Game;
import java.util.Optional;

public interface GameRepository {
    void save(Game game);

    Optional<Game> findById(String id);
}
