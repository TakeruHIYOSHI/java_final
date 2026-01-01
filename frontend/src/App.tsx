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

  const [showSwapAnimation, setShowSwapAnimation] = useState(false);
  const [showUnoShout, setShowUnoShout] = useState(false);

  // Track previous hand sizes to detect UNO
  const prevHandSizesRef = useRef<number[]>([]);

  // CPU Turn Loop
  useEffect(() => {
    if (!gameId || !game || game.state === 'FINISHED') return;

    // Detect Swap Event by checking top card changes? 
    // Or better: Use turnEvent if available, or just heuristic.
    // Heuristic: If top card becomes 'SWAP' and it wasn't before?
    // But 'SWAP' card stays on pile.
    // If 'game.topCard' changes to 'SWAP' from something else? 
    // Yes, that indicates a play.

    // We need 'prevTopCard'.
  }, [gameId, game, fetchState]);

  // Actually, let's track previous top card in a ref to detect changes
  const prevTopCardRef = useRef<string | null>(null);

  useEffect(() => {
    if (!game) return;

    const currentTop = game.topCard.type + game.topCard.color + game.topCard.number;

    if (prevTopCardRef.current && prevTopCardRef.current !== currentTop) {
      // Card changed
      if (game.topCard.type === 'SWAP') {
        // A SWAP card was just played!
        setShowSwapAnimation(true);
        setTimeout(() => setShowSwapAnimation(false), 2500); // Show for 2.5s
      }
    }

    prevTopCardRef.current = currentTop;

    // 2. UNO Shout Detection
    // Check each player: if handSize became 1 (and was > 1 or undefined before)
    game.players.forEach((p, index) => {
      const prevSize = prevHandSizesRef.current[index];
      // Note: If prevSize is undefined (first load), don't shout.
      if (prevSize !== undefined && prevSize > 1 && p.handSize === 1 && p.id === '0') {
        // Trigger UNO Shout! (Only for Human)
        setShowUnoShout(true);
        setTimeout(() => setShowUnoShout(false), 2000);
      }
    });
    // Update refs
    prevHandSizesRef.current = game.players.map(p => p.handSize);
  }, [game]);


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
      }, 3000); // Increased delay to allow animations
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
      // Since handlePlay is user action, we only check if *I* have 1 card.
      if (myPlayer && myPlayer.handSize === 1) {
        triggerUnoCheck();
      }
    } catch (e: any) {
      setError(e.message || 'Invalid move');
      setTimeout(() => setError(null), 3000);
    }
  };

  // UNO Rule Check
  const triggerUnoCheck = () => {
    // Show UNO Button
    setShowUnoButton(true);

    // Timer to automatically fail if not clicked
    timerRef.current = setTimeout(async () => {
      // Check if button is still showing (meaning user hasn't clicked it)
      setShowUnoButton(false);
      setUnoMessage("Forgot UNO! +1 Penalty"); // Penalty reduced to +1
      setTimeout(() => setUnoMessage(null), 1000);

      // Penalty Draw
      await api.draw(gameId!); // Draw 1
    }, 1000); // 1 second window (Hard Mode!)
  };

  const handleUnoClick = () => {
    if (timerRef.current) {
      clearTimeout(timerRef.current);
      timerRef.current = null;
    }
    setShowUnoButton(false);
    setUnoMessage("UNO! SAFE!");
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

      {/* Swap Animation Overlay */}
      {showSwapAnimation && (
        <div className="swap-overlay">
          <div className="swap-icon">🔄</div>
          <div className="swap-text">HANDS SWAPPING!</div>
        </div>
      )}

      {/* UNO Shout Animation */}
      {showUnoShout && (
        <div className="global-uno-shout">
          UNO!
        </div>
      )}
    </div>
  );
}

export default App;
