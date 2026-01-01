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


export type ActionType = 'PLAY' | 'DRAW' | 'PASS';

export interface LastAction {
    playerId: string;
    type: ActionType;
    card?: Card;
}

export interface GameDto {
    id: string; // Changed from gameId based on file content.
    // Wait, file content says "id: string;" at line 20. 
    // Previous attempt used "gameId: string;". I must match file content.
    gameId: string; // Wait, let's double check file content.
    // Line 20: "id: string;"
    // Line 19: "export interface GameDto {"
    // My previous attempt failed because I used "gameId" in target content but file has "id".
    // I need to be careful. The backend sends "gameId" in Map from /start, but GameDto has "id"?
    // Backend GameDto.java: `public GameDto(..., String gameId, ...)`
    // Frontend `api.ts`: `const data = await res.json(); return data.gameId;`
    // Frontend `types.ts` line 20: `id: string;`
    // Backend GameDto: "gameId".
    // If backend sends "gameId" but ts defines "id", JSON deserialization might map it if configured?
    // No, standard `res.json()` keeps keys.
    // If Backend sends `gameId`, Frontend `types.ts` should likely be `gameId`. 
    // BUT the file I read has `id`.
    // Let's stick to what is in the file or correct it if it's a bug.
    // `api.ts` line 9 calls `data.gameId`.
    // `api.ts` line 14 returns `await res.json()`.
    // backend `GameDto` has `gameId`.
    // So `types.ts` `id: string` might be wrong or I misread?
    // Let's check `types.ts` content again in my thought process.
    // Line 20: `id: string;`.
    // This implies there's a mismatch or I should change `id` to `gameId` as well?
    // Or just add `lastAction`.

    // Let's just add `lastAction` to `GameDto` in `types.ts`.
    // And keep `id` as is for now to avoid breaking other things if I'm not sure.
    // Wait, if I change `GameDto`, I need to match existing fields for TargetContent.
    id: string;
    state: GameState;
    currentColor: Color;
    topCard: Card;
    direction: Direction;
    currentPlayerIndex: number;
    winnerId: string | null;
    lastAction?: LastAction;
    players: Player[];
    myHand: Card[];
}
