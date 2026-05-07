# Plan for the API

## What should the API actually do?
The Spring API in our project will handle the actual state of the game. Every calculation should be done at the API level. Desktop clients should be able to establish a socket connection with our API. There should be a constant flow of data open between the clients and the API.

## Game state
We will start small. The game state will just be contained in a **thread-safe Java object** (e.g. `ConcurrentHashMap`). This option will be viable for us, because our app will be relatively small. It will probably only use a one-instance server, we probably won't need any complicated caching mechanisms.

For now, let's say we will use a `ConcurrentHashMap` matching battle ids to the battle objects.

## API structure
If we have too much time on our hands, we may think about some account system, which would be handled by HTTP requests.
However, the most important part of our API (the game part) will be a WebSocket server. We will have one endpoint exposed (let's say `/game`), on which socket clients will register and communicate with our server. This is where the game data will flow.
