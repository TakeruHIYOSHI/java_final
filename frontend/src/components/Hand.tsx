import React, { useState } from 'react';
import type { Card as CardType } from '../types';
import { Card } from './Card';
import './Hand.css';

interface HandProps {
    cards: CardType[];
    onPlayCard: (indices: number[]) => void;
    isMyTurn: boolean;
    playableCards: boolean[]; // Array indicating if each card is playable
}

export const Hand: React.FC<HandProps> = ({ cards, onPlayCard, isMyTurn, playableCards }) => {
    const [selectedIndices, setSelectedIndices] = useState<number[]>([]);

    const handleCardClick = (index: number) => {
        if (!isMyTurn || !playableCards[index]) return;

        const clickedCard = cards[index];

        // Smart Select Logic: Find playable matches
        const matches = cards.map((c, i) => {
            if (i === index) return i;
            if (!playableCards[i]) return -1; // Only playable duplicates

            // Match Logic
            if (clickedCard.type === 'NUMBER' && c.type === 'NUMBER' && c.number === clickedCard.number) return i;
            // For actions, assuming we can stack same type
            if (clickedCard.type !== 'NUMBER' && c.type === clickedCard.type) return i;
            return -1;
        }).filter(i => i !== -1);

        if (matches.length > 1) {
            // Auto-select all matches
            setSelectedIndices(matches);
        } else {
            // Single card -> Play immediately
            onPlayCard([index]);
            setSelectedIndices([]); // Just in case
        }
    };

    const handlePlaySelected = () => {
        onPlayCard(selectedIndices);
        setSelectedIndices([]);
    };

    const handleCancelSelection = () => {
        setSelectedIndices([]);
    };

    return (
        <div className="hand-container">
            {selectedIndices.length > 0 && (
                <div className="smart-play-overlay">
                    <div className="smart-play-buttons">
                        <button className="smart-btn play-all" onClick={handlePlaySelected}>
                            Play All ({selectedIndices.length})
                        </button>
                        <button className="smart-btn cancel" onClick={handleCancelSelection}>
                            Cancel
                        </button>
                    </div>
                </div>
            )}

            <div className={`hand ${selectedIndices.length > 0 ? 'dimmed' : ''}`}>
                {cards.map((card, index) => {
                    const isSelected = selectedIndices.includes(index);
                    const isPlayable = isMyTurn && playableCards[index];
                    const isDimmed = isMyTurn && !isPlayable;

                    return (
                        <div
                            key={`${index}-${card.color}-${card.type}`} // Index key acceptable for this scope
                            className={`hand-card-wrapper ${isSelected ? 'selected' : ''} ${isDimmed ? 'unplayable' : ''}`}
                            style={{ '--i': index } as React.CSSProperties}
                        >
                            <Card
                                card={card}
                                // Click triggers smart logic
                                onClick={() => handleCardClick(index)}
                                playable={isPlayable}
                            />
                            {isSelected && (
                                <div className="selected-indicator">✓</div>
                            )}
                        </div>
                    );
                })}
            </div>
        </div>
    );
};
