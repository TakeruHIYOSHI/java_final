package com.zenn.uno.adapter.web.dto;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Direction;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Game.GameState;
import com.zenn.uno.domain.model.Player;
import java.util.List;
import java.util.stream.Collectors;
import lombok.Data;

//フロントエンドに渡すデータ
@Data 
public class GameDto {
    private String id;
    private GameState state;
    private Color currentColor;
    private Card topCard;
    private Direction direction;
    private int currentPlayerIndex;
    private String winnerId;
    private List<PlayerDto> players;
    private List<Card> myHand; // Only for Human (Player 0)

    @Data
    public static class PlayerDto {
        private String id;
        private String name;
        private boolean isCpu;
        private int handSize;
    }

    public static GameDto from(Game game) {
        GameDto dto = new GameDto();
        dto.setId(game.getId());
        dto.setState(game.getState());
        dto.setCurrentColor(game.getCurrentColor());
        dto.setTopCard(game.getTopCard());
        dto.setDirection(game.getTurnManager().getDirection());
        dto.setCurrentPlayerIndex(game.getTurnManager().getCurrentIndex());
        dto.setWinnerId(game.getWinnerId());

        dto.setPlayers(game.getPlayers().stream().map(p -> {
            PlayerDto pd = new PlayerDto();
            pd.setId(p.getId());
            pd.setName(p.getName());
            pd.setCpu(p.isCpu());
            pd.setHandSize(p.getHandSize());
            return pd;
        }).collect(Collectors.toList()));

        // Assuming Request always comes from P0 (Human) for single player view
        // In a real multi-user app we'd pass requester ID. Here P0 is always Human.
        dto.setMyHand(game.getPlayers().get(0).getHand());

        return dto;
    }
}
