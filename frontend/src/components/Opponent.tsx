import React from 'react';
import type { Player } from '../types';
import { Card } from './Card'; // To render back of cards
import './Opponent.css';

interface OpponentProps {
    player: Player;
    position: 'top' | 'left' | 'right';
    isCurrent: boolean;
}

export const Opponent: React.FC<OpponentProps> = ({ player, position, isCurrent }) => {
    // Generate dummy cards for visual representation of hand size
    // Cap at 10 for visuals to prevent overflow
    const visualHandSize = Math.min(player.handSize, 10);
    const dummyCards = Array(visualHandSize).fill(null);

    return (
        <div className={`opponent ${position} ${isCurrent ? 'current-turn' : ''}`}>
            <div className="player-info">
                <span className="player-name">{player.name}</span>
                <span className="player-cards-count">{player.handSize} Cards</span>
            </div>
            <div className="opponent-hand">
                {dummyCards.map((_, i) => (
                    <div key={i} className="mini-card-wrapper" style={{ '--i': i } as React.CSSProperties}>
                        <Card
                            card={{ color: 'BLACK', type: 'NUMBER', number: 0 }} // Dummy
                            hidden={true}
                            size="small"
                        />
                    </div>
                ))}
            </div>
        </div>
    );
};
