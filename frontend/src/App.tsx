import { useState, useEffect, useCallback, useRef } from 'react';
import { api } from './api';
import type { GameDto, Color } from './types';
import { GameBoard } from './components/GameBoard';
import './App.css';

function App() {
  const [gameId, setGameId] = useState<string | null>(null);
  const [game, setGame] = useState<GameDto | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [showUnoButton, setShowUnoButton] = useState(false);
  const [unoMessage, setUnoMessage] = useState<string | null>(null);

  const timerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  const startGame = async () => {
    try {
      setLoading(true);
      setError(null);
      const id = await api.start();
      setGameId(id);
      const initialGame = await api.getState(id);
      setGame(initialGame);
    } catch (e) {
      setError('Failed to start game');
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchState = useCallback(async () => {
    if (!gameId) return;
    try {
      const g = await api.getState(gameId);
      setGame(g);
    } catch (e) {
      console.error('Failed to fetch state', e);
    }
  }, [gameId]);

  // CPU Turn Loop
  useEffect(() => {
    if (!gameId || !game || game.state === 'FINISHED') return;

    const currentPlayer = game.players[game.currentPlayerIndex];
    if (currentPlayer.cpu) {
      const timer = setTimeout(async () => {
        try {
          const updatedGame = await api.processCpuTurn(gameId);
          setGame(updatedGame);
        } catch (e) {
          console.error("CPU turn failed", e);
        }
      }, 2000); // 2 second delay for visual pacing
      return () => clearTimeout(timer);
    }
  }, [gameId, game, fetchState]);

  // Handle Play
  const handlePlay = async (cardIndices: number[], declaredColor?: Color) => {
    if (!gameId) return;
    try {
      const updatedGame = await api.play(gameId, cardIndices, declaredColor);
      setGame(updatedGame);

      // UNO Rule Check
      const myPlayer = updatedGame.players.find(p => p.id === '0');
      // If played multiple, handSize might be small. 
      // If handSize is 1, check UNO.
      if (myPlayer && myPlayer.handSize === 1 && !updatedGame.players[updatedGame.currentPlayerIndex].cpu) {
        triggerUnoCheck();
      }
    } catch (e: any) {
      setError(e.message || 'Invalid move');
      setTimeout(() => setError(null), 3000);
    }
  };

  const triggerUnoCheck = () => {
    setShowUnoButton(true);
    timerRef.current = setTimeout(async () => {
      if (showUnoButton) { // If still showing
        setShowUnoButton(false);
        setUnoMessage("Forgot UNO! +1 Penalty");
        setTimeout(() => setUnoMessage(null), 3000);
        await handleDraw(); // Penalty Draw
      }
    }, 1000);
  };

  const handleUnoClick = () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    setShowUnoButton(false);
    setUnoMessage("SAFE!");
    setTimeout(() => setUnoMessage(null), 2000);
  };

  const handleDraw = async () => {
    if (!gameId || !game) return;

    // Warning Check: If I have playable cards, Warn.
    const myPlayer = game.players.find(p => p.id === '0');
    // Using current game state to check playability.
    // Note: 'game' state has 'myHand' but 'canPlay' logic is in backend usually or inferred.
    // However, GameBoard passes 'playableCards' logic. We don't have it here easily unless we duplicate logic.
    // Logic: Color match, Number match, Wild.
    // Let's implement simple check here or reuse logic?
    // Reuse specific check for warning.
    const hasPlayable = game.myHand.some(c =>
      c.type === 'WILD' || c.type === 'WILD_DRAW4' ||
      c.color === game.currentColor ||
      (c.type === 'NUMBER' && game.topCard.type === 'NUMBER' && c.number === game.topCard.number) ||
      (c.type !== 'NUMBER' && c.type === game.topCard.type) // Symbol match
    );

    if (hasPlayable) {
      // Show Warning Toast
      setError("まだ出せるカードがあります！ (Playable cards exist!)");
      setTimeout(() => setError(null), 2000);
      // Prevent Draw
      return;
    }

    try {
      const updatedGame = await api.draw(gameId);
      setGame(updatedGame);
    } catch (e: any) {
      setError(e.message || 'Cannot draw');
      setTimeout(() => setError(null), 3000);
    }
  };

  if (!gameId) {
    return (
      <div className="start-screen">
        <h1 className="title">UNO</h1>
        <button className="start-btn" onClick={startGame} disabled={loading}>
          {loading ? 'Starting...' : 'Start Game'}
        </button>
      </div>
    );
  }

  if (!game) {
    return <div className="loading">Loading game...</div>;
  }

  return (
    <div className="app-container">
      {error && <div className="error-toast">{error}</div>}
      {unoMessage && <div className="uno-message-toast">{unoMessage}</div>}

      <GameBoard
        game={game}
        onPlay={handlePlay}
        onDraw={handleDraw}
      />

      {showUnoButton && (
        <button className="uno-shout-btn" onClick={handleUnoClick}>
          UNO!
        </button>
      )}
    </div>
  );
}

export default App;
