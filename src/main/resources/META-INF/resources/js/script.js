class SnakeGame {
    constructor(webSocketUrl, canvasId) {
        this.WEBSOCKET_URL = webSocketUrl;
        this.CANVAS_ID = canvasId;
        this.COLLISION_SOUND_ID = 'crashSound';
        this.FOOD_EATEN_SOUND_ID = 'foodEatenSound';
        this.FRAME_INTERVAL = 100;
        this.TILE_SIZE = 20;
        this.canvas = document.getElementById(this.CANVAS_ID);
        this.context = this.canvas.getContext('2d');
        this.food = null;
        this.snake = [{x: 5, y: 5}];
        this.snakes = {};
        this.scores = {};
        this.currentDirection = 'RIGHT';
        this.gameInterval = null;
        this.socket = this.initializeWebSocket();
        this.startGame();
        this.bindKeyEvents();
    }

    initializeWebSocket() {
        const ws = new WebSocket(this.WEBSOCKET_URL);

        ws.onopen = () => {
            console.log("WebSocket connected.");
            ws.send(JSON.stringify({method: 'requestState'}));
        };

        ws.onerror = (error) => console.error("WebSocket error:", error);

        ws.onmessage = (event) => this.handleServerMessage(event);

        return ws;
    }

    handleServerMessage(event) {
        const data = JSON.parse(event.data);

        if (data.collision) {
            this.playSound(this.COLLISION_SOUND_ID);
            this.gameOver();
        }

        if (data.foodEaten) {
            this.playSound(this.FOOD_EATEN_SOUND_ID);
        }

        if (data.snakes && data.foodPosition) {
            this.snakes = data.snakes;
            this.food = data.foodPosition;
            this.updateGameBoard();
        }

        if (data.scores) {
            this.scores = data.scores;
        }
    }

    startGame() {
        this.gameInterval = setInterval(() => {
            this.moveSnake();
            this.updateGameBoard();
            this.sendDirectionToServer(this.currentDirection);
        }, this.FRAME_INTERVAL);
    }

    bindKeyEvents() {
        document.addEventListener('keydown', (event) => this.handleDirectionChange(event));
    }

    updateGameBoard() {
        this.context.clearRect(0, 0, this.canvas.width, this.canvas.height);
        this.drawBackground();
        Object.values(this.snakes).forEach(snake => this.drawSnake(snake.body));

        if (this.food) {
            this.drawFood(this.food);
        }

        this.drawScores();
    }

    moveSnake() {
        const head = {...this.snake[0]};
        switch (this.currentDirection) {
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

        if (this.checkWallCollision(head)) {
            this.playSound(this.COLLISION_SOUND_ID);
            this.gameOver();
            return;
        }

        if (this.checkSelfCollision(head)) {
            this.playSound(this.COLLISION_SOUND_ID);
            this.gameOver();
            return;
        }

        if (this.checkCollisionWithOtherSnakes(head)) {
            this.playSound(this.COLLISION_SOUND_ID);
            this.gameOver();
            return;
        }

        if (this.food && head.x === this.food.x && head.y === this.food.y) {
            this.playSound(this.FOOD_EATEN_SOUND_ID);
            this.socket.send(JSON.stringify({method: "foodEaten", playerId: 'local'}));
            this.food = this.generateRandomFoodPosition();
            this.scores['local'] += 1;

            if (typeof this.scores['local'] === 'number') {
                this.scores['local'] += 1;
            } else {
                this.scores['local'] = 1;
            }
        } else {
            this.snake.pop();
        }

        this.snake.unshift(head);
        this.snakes['local'] = {body: this.snake};
    }

    checkWallCollision(head) {
        return head.x < 0 || head.x >= this.canvas.width / this.TILE_SIZE || head.y < 0 || head.y >= this.canvas.height / this.TILE_SIZE;
    }

    checkSelfCollision(head) {
        for (let i = 1; i < this.snake.length; i++) {
            if (head.x === this.snake[i].x && head.y === this.snake[i].y) {
                return true;
            }
        }
        return false;
    }

    checkCollisionWithOtherSnakes(head) {
        for (let snakeId in this.snakes) {
            if (snakeId !== 'local') {
                const otherSnake = this.snakes[snakeId].body;
                for (let segment of otherSnake) {
                    if (head.x === segment.x && head.y === segment.y) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    handleDirectionChange(event) {
        const newDirection = this.getDirectionFromKey(event.key);
        if (newDirection && this.canChangeDirection(newDirection)) {
            this.currentDirection = newDirection;
        }
    }

    getDirectionFromKey(key) {
        const directionMap = {
            'ArrowUp': 'UP',
            'ArrowDown': 'DOWN',
            'ArrowLeft': 'LEFT',
            'ArrowRight': 'RIGHT',
        };
        return directionMap[key];
    }

    canChangeDirection(newDirection) {
        const oppositeDirection = {
            'UP': 'DOWN',
            'DOWN': 'UP',
            'LEFT': 'RIGHT',
            'RIGHT': 'LEFT',
        };
        return this.currentDirection !== oppositeDirection[newDirection];
    }

    generateRandomFoodPosition() {
        const boundaryOffset = 1;
        let newPosition;
        let isOnSnake;

        do {
            newPosition = {
                x: Math.floor(Math.random() * (this.canvas.width / this.TILE_SIZE - 2 * boundaryOffset)) + boundaryOffset,
                y: Math.floor(Math.random() * (this.canvas.height / this.TILE_SIZE - 2 * boundaryOffset)) + boundaryOffset,
            };
            isOnSnake = this.snake.some(segment => segment.x === newPosition.x && segment.y === newPosition.y);
        } while (isOnSnake);

        return newPosition;
    }

    sendDirectionToServer(direction) {
        if (this.socket.readyState === WebSocket.OPEN) {
            this.socket.send(JSON.stringify({method: "play", direction}));
        } else {
            console.warn("WebSocket not open. Failed to send direction.");
        }
    }

    playSound(soundId) {
        const soundElement = document.getElementById(soundId);
        if (soundElement) {
            soundElement.play().catch((error) => console.error("Error playing sound:", error));
        }
    }

    drawSnake(snakeBody) {
        this.context.fillStyle = '#f62649';
        snakeBody.forEach(segment => {
            this.context.fillRect(segment.x * this.TILE_SIZE, segment.y * this.TILE_SIZE, this.TILE_SIZE, this.TILE_SIZE);
        });
    }

    drawFood(position) {
        this.context.fillStyle = '#f6f14b';
        this.context.fillRect(position.x * this.TILE_SIZE, position.y * this.TILE_SIZE, this.TILE_SIZE, this.TILE_SIZE);
    }

    drawBackground() {
        this.context.fillStyle = '#1c1743';
        this.context.fillRect(0, 0, this.canvas.width, this.canvas.height);
    }

    drawScores() {
        this.context.fillStyle = '#ffffff';
        this.context.font = '20px Arial';

        let yOffset = 20;
        Object.keys(this.scores).forEach((playerId, index) => {
            let score = this.scores[playerId];
            if (typeof score !== 'number') {
                score = 0;
            }
            this.context.fillText(`Player ${index + 1}: ${score}`, 10, yOffset);
            yOffset += 30;
        });
    }

    gameOver() {
        clearInterval(this.gameInterval);
        setTimeout(() => {
            location.reload();
        }, 2000);
    }
}

const game = new SnakeGame("ws://localhost:8080/game", "gameCanvas");