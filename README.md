# Snake Game

This is a multiplayer Snake Game implemented using Quarkus and WebSockets, allowing players to interact across multiple
browser tabs or devices. This guide will help you get started with running the game locally, testing it, and
understanding how to reset and refresh the game in different tabs.

---

## Getting Started

### Prerequisites

- IntelliJ or IDE from your preference
- **Java 11+**
- **Maven 3.8+**
- A modern web browser (for testing WebSockets across tabs).

### Running the Application

- Install maven dependencies

- Execute the application by the "play" btn on configurations.

- The game will be accessible at: `http://localhost:8080`

### Testing the Game

Once the application is running:

1. **Open multiple browser tabs**: Navigate to `http://localhost:8080` in each tab.
2. **Start playing**: The game will connect each tab to a shared WebSocket server, allowing real-time interaction.
3. **Control the Snake**: Each player can move their snake by sending WebSocket messages with the arrow keys.
4. **Refresh to reset**: To reset the game for any player, simply refresh the browser tab. This will start a new session
   for that player while the others can continue.

**Tip:** You can simulate multiple players by using different tabs or browsers and controlling the snake in each one.

## WebSocket Testing and Game Features

- **WebSocket Communication**: The game uses WebSockets to allow real-time communication between multiple players. Each
  player’s move is sent to the server and broadcasted to all connected clients.
- **Browser Tab Usage**: You can open the game in multiple tabs or even different browsers to simulate a multiplayer
  experience.
- **Game Reset**: To reset the game state, simply refresh a tab. Other players won’t be affected unless they also
  refresh.

---

## Troubleshooting

- **WebSocket not connecting**: Ensure that your application is running and you’re accessing `http://localhost:8080` in
  each tab.
- **Game not responding**: If the game hangs or doesn’t respond, try refreshing the page or restarting the application.
- **Connection issues**: If testing across devices, make sure your firewall or network isn’t blocking WebSocket
  connections.

---

## Related Guides

- [Quarkus WebSocket Guide](https://quarkus.io/guides/websockets): Learn more about how WebSockets work with Quarkus.
- [Quarkus Native Executables](https://quarkus.io/guides/maven-tooling): Learn more about building native executables
  with Quarkus.


