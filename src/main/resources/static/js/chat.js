// Example using Socket.IO
let socket = null;

function connectSocket(jwtToken) {
    // Assumes Socket.IO client is loaded in your HTML
    socket = io('/', {
        auth: { token: jwtToken }
    });

    socket.on('connect', () => {
        console.log('Connected to chat server');
    });

    socket.on('message', (msg) => {
        // Handle incoming message (append to chat, etc.)
        if (window.onChatMessage) window.onChatMessage(msg);
    });

    socket.on('disconnect', () => {
        console.log('Disconnected from chat server');
    });
}

function sendMessage(roomOrUser, text) {
    if (socket && socket.connected) {
        socket.emit('message', { to: roomOrUser, text });
    }
}

function disconnectSocket() {
    if (socket) socket.disconnect();
}

// Export functions if using modules
// export { connectSocket, sendMessage, disconnectSocket };
