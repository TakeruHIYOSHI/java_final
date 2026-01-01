export type Color = 'RED' | 'BLUE' | 'GREEN' | 'YELLOW' | 'BLACK';
export type CardType = 'NUMBER' | 'SKIP' | 'REVERSE' | 'DRAW2' | 'WILD' | 'WILD_DRAW4' | 'SWAP';
export type GameState = 'WAITING' | 'PLAYING' | 'FINISHED';
export type Direction = 'CLOCKWISE' | 'COUNTER_CLOCKWISE';

export interface Card {
    color: Color;
    type: CardType;
    number: number | null;
}

export interface Player {
    id: string;
    name: string;
    cpu: boolean;
    handSize: number;
}

export interface GameDto {
    id: string;
    state: GameState;
    currentColor: Color;
    topCard: Card;
    direction: Direction;
    currentPlayerIndex: number;
    winnerId: string | null;
    players: Player[];
    myHand: Card[]; // Only populated for Human
}
