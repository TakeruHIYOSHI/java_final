import React from 'react';
import type { Card as CardType } from '../types';
import './Card.css';

interface CardProps {
    card: CardType;
    onClick?: () => void;
    playable?: boolean;
    hidden?: boolean;
    size?: 'small' | 'medium' | 'large';
}

export const Card: React.FC<CardProps> = ({ card, onClick, playable = false, hidden = false, size = 'medium' }) => {
    if (hidden) {
        return (
            <div className={`uno-card back ${size}`} onClick={onClick}>
                <div className="inner">
                    <span className="logo">UNO</span>
                </div>
            </div>
        );
    }

    const { color, type, number } = card;

    const getCardContent = () => {
        if (type === 'NUMBER') return number;
        if (type === 'SKIP') return '⊘';
        if (type === 'REVERSE') return '⇄';
        if (type === 'DRAW2') return '+2';
        if (type === 'WILD') return '🌈';
        if (type === 'WILD_DRAW4') return '+4';
        return type;
    };

    const cardColorClass = color ? `color-${color.toLowerCase()}` : 'color-black';

    return (
        <div
            className={`uno-card ${cardColorClass} ${playable ? 'playable' : ''} ${size}`}
            onClick={playable ? onClick : undefined}
        >
            <div className="inner">
                <span className="center-symbol">{getCardContent()}</span>
                <span className="corner-symbol top-left">{getCardContent()}</span>
                <span className="corner-symbol bottom-right">{getCardContent()}</span>
            </div>
        </div>
    );
};
