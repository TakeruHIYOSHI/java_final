import React, { useState } from 'react';
import type { GameDto, Color, Card as CardData } from '../types';
import { Hand } from './Hand';
import { Opponent } from './Opponent';
import { Card } from './Card';
import './GameBoard.css';

interface GameBoardProps {
    game: GameDto;
    onPlay: (indices: number[], declaredColor?: Color) => void;
    onDraw: () => void;
}

export const GameBoard: React.FC<GameBoardProps> = ({ game, onPlay, onDraw }) => {
    // Derived state for Action Animations
    const lastAction = game.lastAction;
    const [animationKey, setAnimationKey] = React.useState(0);

    // Trigger animation when lastAction changes
    React.useEffect(() => {
        if (lastAction?.type === 'PLAY') {
            setAnimationKey(k => k + 1);
        }
    }, [lastAction]);

    const getActionAnimation = () => {
        if (!lastAction) return null;
        if (lastAction.type !== 'PLAY') return null;

        const card = lastAction.card;
        if (!card) return null;

        if (card.type === 'SWAP') return <div key={animationKey} className="big-animation swap">SWAP!</div>;
        if (card.type === 'SKIP') return <div key={animationKey} className="big-animation skip">SKIP!</div>;
        if (card.type === 'DRAW2') return <div key={animationKey} className="big-animation draw2">DRAW +2</div>;
        if (card.type === 'WILD_DRAW4') return <div key={animationKey} className="big-animation wild4">DRAW +4</div>;

        return null;
    };

    const [showColorPicker, setShowColorPicker] = useState(false);
    const [pendingCardIndices, setPendingCardIndices] = useState<number[] | null>(null);

    // Identify players
    const myPlayer = game.players.find(p => p.id === '0');
    const opponents = game.players.filter(p => p.id !== '0');
    const leftOpponent = opponents.find(p => p.id === '1');
    const topOpponent = opponents.find(p => p.id === '2');
    const rightOpponent = opponents.find(p => p.id === '3');

    // Determine playable status
    const canPlay = (card: CardData): boolean => {
        if (game.currentPlayerIndex !== 0) return false; // Not my turn
        if (card.type === 'WILD' || card.type === 'WILD_DRAW4') return true;

        // Color Match (Use currentColor which includes declared colors from Wilds)
        if (card.color === game.currentColor) return true;

        // Number Match
        if (card.type === 'NUMBER' && game.topCard.type === 'NUMBER' && card.number === game.topCard.number) return true;

        // Symbol Match (same Action type) - ignoring color (handled above)
        // e.g. Blue Skip on Red Skip.
        if (card.type !== 'NUMBER' && card.type === game.topCard.type) return true;

        return false;
    };

    const playableCards = myPlayer?.id === '0'
        ? game.myHand.map(card => canPlay(card))
        : [];

    const handleCardPlayRequest = (indices: number[]) => {
        // Validation: If Wild exists, show Picker.
        // If multiple cards and one is Wild?
        // Logic: if first card is wild (or any), we need color.
        // Assuming user played [Wild, ...] or [Number, ...]
        // Check first card.
        const firstIndex = indices[0];
        const card = game.myHand[firstIndex];

        if (card.type === 'WILD' || card.type === 'WILD_DRAW4') {
            setPendingCardIndices(indices);
            setShowColorPicker(true);
        } else {
            onPlay(indices);
        }
    };

    const handleColorPick = (color: Color) => {
        if (pendingCardIndices !== null) {
            onPlay(pendingCardIndices, color);
            setShowColorPicker(false);
            setPendingCardIndices(null);
        }
    };

    // Current turn message
    const isMyTurn = game.players[game.currentPlayerIndex].id === '0';
    const currentTurnPlayer = game.players[game.currentPlayerIndex];
    const turnMessage = isMyTurn ? "Your Turn!" : `${currentTurnPlayer.name}'s Turn`;

    return (
        <div className="game-board">
            {getActionAnimation()}

            {/* Opponents */}
            {topOpponent && <Opponent player={topOpponent} position="top" isCurrent={game.players[game.currentPlayerIndex].id === topOpponent.id} />}
            {leftOpponent && <Opponent player={leftOpponent} position="left" isCurrent={game.players[game.currentPlayerIndex].id === leftOpponent.id} />}
            {rightOpponent && <Opponent player={rightOpponent} position="right" isCurrent={game.players[game.currentPlayerIndex].id === rightOpponent.id} />}

            {/* Center Area */}
            <div className="center-area">
                <div className="deck-area" onClick={isMyTurn ? onDraw : undefined} title="Draw Card">
                    <Card card={{ color: 'BLACK', type: 'NUMBER', number: 0 }} hidden={true} playable={isMyTurn} />
                </div>

                <div className="discard-area">
                    <Card card={game.topCard} />
                </div>

                <div className={`color-indicator color-${game.currentColor.toLowerCase()}`}>
                    Current Color: {game.currentColor}
                </div>

                <div className="turn-indicator">
                    {turnMessage}
                    <div className={`direction-arrow ${game.direction === 'CLOCKWISE' ? 'cw' : 'ccw'}`}>
                        {game.direction === 'CLOCKWISE' ? '↻' : '↺'}
                    </div>
                </div>

                {showColorPicker && (
                    <div className="color-picker-overlay">
                        <div className="color-picker">
                            <h3>Select Color</h3>
                            <div className="color-buttons">
                                <button className="btn-red" onClick={() => handleColorPick('RED')}>Red</button>
                                <button className="btn-blue" onClick={() => handleColorPick('BLUE')}>Blue</button>
                                <button className="btn-green" onClick={() => handleColorPick('GREEN')}>Green</button>
                                <button className="btn-yellow" onClick={() => handleColorPick('YELLOW')}>Yellow</button>
                            </div>
                        </div>
                    </div>
                )}
            </div>

            {/* My Hand */}
            {myPlayer && (
                <div className={`my-section ${isMyTurn ? 'active-turn' : ''}`}>
                    <Hand
                        cards={game.myHand}
                        onPlayCard={handleCardPlayRequest}
                        isMyTurn={isMyTurn}
                        playableCards={playableCards}
                    />
                </div>
            )}

            {/* Game Over Overlay */}
            {game.state === 'FINISHED' && (
                <div className="game-over-overlay">
                    <div className="game-over-modal">
                        <h1>Game Over!</h1>
                        <h2>Winner: {game.players.find(p => p.id === game.winnerId)?.name}</h2>
                        <button onClick={() => window.location.reload()}>Play Again</button>
                    </div>
                </div>
            )}
        </div>
    );
};
