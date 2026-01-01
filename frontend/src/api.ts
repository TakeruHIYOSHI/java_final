import { GameDto, Color } from './types';

const API_BASE = 'http://localhost:8080/api/games';

export const api = {
    start: async (): Promise<string> => {
        const res = await fetch(`${API_BASE}/start`, { method: 'POST' });
        const data = await res.json();
        return data.gameId;
    },

    getState: async (gameId: string): Promise<GameDto> => {
        const res = await fetch(`${API_BASE}/${gameId}/state`);
        return await res.json();
    },

    play: async (gameId: string, cardIndex: number, declaredColor?: Color): Promise<GameDto> => {
        const res = await fetch(`${API_BASE}/${gameId}/actions/play`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerId: '0', cardIndex, declaredColor })
        });
        if (!res.ok) {
            try {
                const err = await res.json();
                throw new Error(err.message || 'Error playing card');
            } catch (e) {
                throw new Error('Error playing card');
            }
        }
        return await res.json();
    },

    draw: async (gameId: string): Promise<GameDto> => {
        const res = await fetch(`${API_BASE}/${gameId}/actions/draw`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ playerId: '0' })
        });
        if (!res.ok) throw new Error('Error drawing card');
        return await res.json();
    }
};
