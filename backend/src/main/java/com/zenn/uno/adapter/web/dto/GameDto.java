package com.zenn.uno.adapter.web.dto;

import com.zenn.uno.domain.model.Card;
import com.zenn.uno.domain.model.Color;
import com.zenn.uno.domain.model.Direction;
import com.zenn.uno.domain.model.Game;
import com.zenn.uno.domain.model.Game.GameState;
import com.zenn.uno.domain.model.Player;
import java.util.List;
import java.util.stream.Collectors;
import java.util.List;
import java.util.stream.Collectors;

//フロントエンドに渡すデータ
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

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public GameState getState() {
        return state;
    }

    public void setState(GameState state) {
        this.state = state;
    }

    public Color getCurrentColor() {
        return currentColor;
    }

    public void setCurrentColor(Color currentColor) {
        this.currentColor = currentColor;
    }

    public Card getTopCard() {
        return topCard;
    }

    public void setTopCard(Card topCard) {
        this.topCard = topCard;
    }

    public Direction getDirection() {
        return direction;
    }

    public void setDirection(Direction direction) {
        this.direction = direction;
    }

    public int getCurrentPlayerIndex() {
        return currentPlayerIndex;
    }

    public void setCurrentPlayerIndex(int currentPlayerIndex) {
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public String getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }

    public List<PlayerDto> getPlayers() {
        return players;
    }

    public void setPlayers(List<PlayerDto> players) {
        this.players = players;
    }

    public List<Card> getMyHand() {
        return myHand;
    }

    public void setMyHand(List<Card> myHand) {
        this.myHand = myHand;
    }

    public static class PlayerDto {
        private String id;
        private String name;
        private boolean isCpu;
        private int handSize;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isCpu() {
            return isCpu;
        }

        public void setCpu(boolean cpu) {
            isCpu = cpu;
        }

        public int getHandSize() {
            return handSize;
        }

        public void setHandSize(int handSize) {
            this.handSize = handSize;
        }
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
