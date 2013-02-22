# Event Management

1. Keyboard events are read from lwjgl and copied to a queue.
2. The client thread reads from this queue, and marks the events it read with a timestamp.
3. The server thread reads from this queue, and marks the events it read with its own timestamp.
4. When an event has both a client timestamp and a server timestamp, it is removed from the queue.
