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

        // Smart Select Logic: Find playable matches (Same Number/Type)
        // Rule: First card must be playable (handled by initial click check).
        // Subsequents: Can be ANY color as long as Number/Type matches.

        const matches = cards.map((c, i) => {
            if (i === index) return i;

            // Do NOT check playableCards[i] here. 
            // Different colors ARE allowed if numbers match.
            // But usually we can't play an invalid card *unless* we play a valid one first.
            // So we just check for content match.

            // Match Logic
            if (clickedCard.type === 'NUMBER' && c.type === 'NUMBER' && c.number === clickedCard.number) return i;
            // For actions, assuming we can stack same type
            if (clickedCard.type !== 'NUMBER' && c.type === clickedCard.type) return i;
            return -1;
        }).filter(i => i !== -1);

        // Ensure the CLICKED card (which is valid) is FIRST in the list.
        // matches is sorted by index usually. We need to move 'index' to front.
        const sortedMatches = [index, ...matches.filter(i => i !== index)];

        if (sortedMatches.length > 1) {
            // Auto-select all matches
            setSelectedIndices(sortedMatches);
        } else {
            // Single card -> Play immediately
            onPlayCard([index]);
            setSelectedIndices([]);
        }
    };

    const handlePlaySelected = () => {
        // Send sorted indices (Clicked one first)
        onPlayCard(selectedIndices);
        setSelectedIndices([]);
    };

    const handlePlaySingle = () => {
        // Play ONLY the first card (the one originally clicked and valid)
        if (selectedIndices.length > 0) {
            onPlayCard([selectedIndices[0]]);
        }
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
                        <button className="smart-btn play-one" onClick={handlePlaySingle}>
                            Play Just One
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
