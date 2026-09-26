export type PartyPhase = "WAITING" | "PLAYING" | "FINISHED";

export type PartyPlayer = {
  playerId: string;
  displayName: string;
  ready: boolean;
  score: number;
};

export type PartyGameState = {
  roomId: string;
  capacity: number;
  phase: PartyPhase;
  roundNumber: number;
  turnNumber: number;
  currentPlayerId: string | null;
  hostPlayerId: string | null;
  players: Record<string, PartyPlayer>;
  lastAction: { playerId: string; action: string; value: number } | null;
  winner: string | null;
};

export type PartyMessage =
  | { type: "pong"; timestamp: string }
  | { type: "game_state"; state: PartyGameState }
  | { type: "error"; code: string; message: string };

export type PartyClientOptions = {
  url: string;
  roomId: string;
  playerId: string;
  reconnect?: boolean;
  maxReconnectDelayMs?: number;
};

export class PartyGameClient {
  private socket?: WebSocket;
  private reconnectTimer?: ReturnType<typeof setTimeout>;
  private reconnectAttempt = 0;
  private closedByClient = false;
  private readonly options: Required<PartyClientOptions>;
  private readonly messageListeners = new Set<(message: PartyMessage) => void>();
  private readonly connectionListeners = new Set<(connected: boolean) => void>();

  constructor(options: PartyClientOptions) {
    this.options = {
      ...options,
      reconnect: options.reconnect ?? true,
      maxReconnectDelayMs: options.maxReconnectDelayMs ?? 5000,
    };
  }

  connect(): void {
    this.closedByClient = false;
    const url = new URL(this.options.url);
    url.searchParams.set("roomId", this.options.roomId);
    url.searchParams.set("playerId", this.options.playerId);
    this.socket = new WebSocket(url);
    this.socket.onopen = () => {
      this.reconnectAttempt = 0;
      this.connectionListeners.forEach((listener) => listener(true));
    };
    this.socket.onmessage = (event) => {
      this.messageListeners.forEach((listener) => listener(JSON.parse(event.data) as PartyMessage));
    };
    this.socket.onclose = () => {
      this.connectionListeners.forEach((listener) => listener(false));
      if (this.options.reconnect && !this.closedByClient) {
        const delay = Math.min(250 * 2 ** this.reconnectAttempt, this.options.maxReconnectDelayMs);
        this.reconnectAttempt += 1;
        this.reconnectTimer = setTimeout(() => {
          this.reconnectTimer = undefined;
          if (!this.closedByClient) {
            this.connect();
          }
        }, delay);
      }
    };
  }

  disconnect(): void {
    this.closedByClient = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = undefined;
    }
    this.socket?.close();
  }

  ping(): void {
    this.send({ type: "ping" });
  }

  ready(): void {
    this.send({ type: "ready" });
  }

  startGame(): void {
    this.send({ type: "start_game" });
  }

  turnAction(action: "roll" | "pass"): void {
    this.send({ type: "turn_action", action });
  }

  onMessage(listener: (message: PartyMessage) => void): () => void {
    this.messageListeners.add(listener);
    return () => this.messageListeners.delete(listener);
  }

  onConnectionChange(listener: (connected: boolean) => void): () => void {
    this.connectionListeners.add(listener);
    return () => this.connectionListeners.delete(listener);
  }

  private send(payload: object): void {
    if (this.socket?.readyState !== WebSocket.OPEN) {
      throw new Error("Party game WebSocket is not connected");
    }
    this.socket.send(JSON.stringify(payload));
  }
}
