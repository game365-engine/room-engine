import { PartyGameClient } from "./index.js";

const roomId = new URLSearchParams(window.location.search).get("roomId");
const playerId = new URLSearchParams(window.location.search).get("playerId");

if (roomId && playerId) {
  const client = new PartyGameClient({
    url: "ws://localhost:8081/ws/party",
    roomId,
    playerId,
  });
  client.onConnectionChange((connected) => console.log("connected", connected));
  client.onMessage((message) => console.log("server message", message));
  client.connect();
}
