package com.zenn.uno.adapter.persistence;

import com.zenn.uno.application.output.GameRepository;
import com.zenn.uno.domain.model.Game;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryGameRepository implements GameRepository {
    private final Map<String, Game> store = new ConcurrentHashMap<>();

    @Override
    public void save(Game game) {
        store.put(game.getId(), game);
    }

    @Override
    public Optional<Game> findById(String id) {
        return Optional.ofNullable(store.get(id));
    }
}
