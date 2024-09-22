const WEBSOCKET_URL = "ws://localhost:8080/game";
const CANVAS_ID = 'gameCanvas';  // Move this before canvas access
const COLLISION_SOUND_ID = 'gameoverSound';
const FOOD_EATEN_SOUND_ID = 'foodEatenSound';
const FRAME_INTERVAL = 8000; // 100ms per frame
const TILE_SIZE = 20;

const canvas = document.getElementById(CANVAS_ID);
const context = canvas.getContext('2d');
let food = generateRandomFoodPosition();  // Now the canvas is initialized before calling this

let currentDirection = 'RIGHT';
let snake = [{x: 5, y: 5}];
let gameInterval = null;

const socket = initializeWebSocket();

function initializeWebSocket() {
    const ws = new WebSocket(WEBSOCKET_URL);

    ws.onopen = () => console.log("WebSocket connected.");
    ws.onerror = (error) => console.error("WebSocket error:", error);
    ws.onmessage = handleServerMessage;

    return ws;
}

function handleServerMessage(event) {
    const data = JSON.parse(event.data);

    if (data.collision) {
        playSound(COLLISION_SOUND_ID);
    }

    if (data.foodEaten) {
        playSound(FOOD_EATEN_SOUND_ID);
    }

    if (data.snakes && data.foodPosition) {
        updateGameState(data.snakes, data.foodPosition);
    }
}

function playSound(soundId) {
    const soundElement = document.getElementById(soundId);
    if (soundElement) soundElement.play();
}

function startGame() {
    gameInterval = setInterval(updateGameBoard, FRAME_INTERVAL);
}

function updateGameState(snakes, foodPosition) {
    context.clearRect(0, 0, canvas.width, canvas.height);
    drawBackground();
    Object.values(snakes).forEach(snake => drawSnake(snake.body));
    drawFood(foodPosition);
}

function updateGameBoard() {
    context.clearRect(0, 0, canvas.width, canvas.height);
    drawBackground();
    drawSnake(snake); // Local snake for the player
    drawFood(food);   // Local food for the player
}

function moveSnake() {
    // Create a new head based on the current direction
    const head = {...snake[0]};

    switch (currentDirection) {
        case 'UP':
            head.y -= 1;
            break;
        case 'DOWN':
            head.y += 1;
            break;
        case 'LEFT':
            head.x -= 1;
            break;
        case 'RIGHT':
            head.x += 1;
            break;
    }

    if (head.x === food.x && head.y === food.y) {
        food = generateRandomFoodPosition();  // Generate new food
    } else {
        snake.pop();
    }

    // Add the new head to the front of the snake
    snake.unshift(head);
}

function drawSnake(snakeBody) {
    context.fillStyle = '#f62649';
    snakeBody.forEach(segment => {
        context.fillRect(segment.x * TILE_SIZE, segment.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
    });
}

function drawFood(position) {
    context.fillStyle = '#f6f14b';
    context.fillRect(position.x * TILE_SIZE, position.y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
}

function drawBackground() {
    context.fillStyle = '#1c1743';
    context.fillRect(0, 0, canvas.width, canvas.height);
}

function generateRandomFoodPosition() {
    return {
        x: Math.floor(Math.random() * (canvas.width / TILE_SIZE - 2)) + 1,
        y: Math.floor(Math.random() * (canvas.height / TILE_SIZE - 2)) + 1,
    };
}

document.addEventListener('keydown', handleDirectionChange);

function handleDirectionChange(event) {
    const newDirection = getDirectionFromKey(event.key);
    if (newDirection) {
        currentDirection = newDirection;
        sendDirectionToServer(newDirection);
    }
}

function getDirectionFromKey(key) {
    const oppositeDirection = {
        'ArrowUp': 'DOWN',
        'ArrowDown': 'UP',
        'ArrowLeft': 'RIGHT',
        'ArrowRight': 'LEFT',
    };
    return currentDirection !== oppositeDirection[key] ? key.replace('Arrow', '').toUpperCase() : null;
}

function sendDirectionToServer(direction) {
    const payload = {method: "play", direction};
    if (socket.readyState === WebSocket.OPEN) {
        socket.send(JSON.stringify(payload));
    } else {
        console.warn("WebSocket not open. Failed to send direction.");
    }
}

startGame();