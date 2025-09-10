
// Dropdown for room menu


document.addEventListener('DOMContentLoaded', function () {
    const privateUsersList = document.getElementById('private-users-list');
    const chatHeader = document.getElementById('chat-header');
    const messagesArea = document.getElementById('messages-area');
    const chatForm = document.getElementById('chat-form');
    const chatInput = document.getElementById('chat-input');
    const logout = document.getElementById('log-out');
    const sendBtn = chatForm.querySelector('.send-btn');
    const user = document.getElementById('user');
    let currentChat = null; // { type: 'private', username: '...' }
    let stompClient = null;
    let  roomId = null;
    let activePrivateChatUser = null;
    window.activeRoom = null;
    const BACKEND_URL = "https://hosted-projects-production.up.railway.app";

    const currentUsername = localStorage.getItem('username') || 'User';
    const token = localStorage.getItem('token');

    user.innerHTML = `
    <button id="user-btn" class="user-btn">
        <span class="user-icon">👤</span>
        <span class="username">${currentUsername} &#9432;</span>
    </button>
    <div id="user-popup" class="user-popup">
        <p><strong>Username:</strong> ${currentUsername}</p>
        <p><strong>Email:</strong> ${localStorage.getItem("email") || "Not available"}</p>
    </div>
`;

    const userBtn = document.getElementById("user-btn");
    const userPopup = document.getElementById("user-popup");

// Toggle popup on click
    userBtn.addEventListener("click", () => {
        userPopup.classList.toggle("show");
    });

// Hide when clicking outside
    document.addEventListener("click", (e) => {
        if (!user.contains(e.target)) {
            userPopup.classList.remove("show");
        }
    });

    // Ask user permission for notifications
    if ("Notification" in window) {
        if (Notification.permission !== "granted") {
            Notification.requestPermission();
        }
    }


    // --- WebSocket Connect ---
    function connectWebSocket() {
        const socket = new SockJS('http://localhost:8080/ws');
        stompClient = Stomp.over(socket);

        stompClient.connect(
            { Authorization: "Bearer " + token },
            function () {

                // Public messages
                stompClient.subscribe('/topic/public', (frame) => {
                    const msg = JSON.parse(frame.body);
                    handleIncoming(msg);
                });

                // Private messages
                stompClient.subscribe('/user/queue/messages', (frame) => {
                    const msg = JSON.parse(frame.body);
                    handleIncoming(msg);
                });

                // Announce join if it is a room chat type
                if (currentChat && currentChat.type === 'room') {
                    stompClient.send('/app/chat.addUser', {}, JSON.stringify({
                        sender: currentUsername,
                        type: 'JOIN'
                    }));
                }

            },
            function (error) {
                setTimeout(connectWebSocket, 3000);
            }
        );
    }

    // --- Incoming messages handler ---
    function handleIncoming(message) {
        switch (message.type) {
            case 'JOIN':
            case 'LEAVE':
                loadOnlineUsers();
                appendMessage(message, 'system');
                break;

            case 'CHAT':
                appendMessage(message, 'public');
                break;

            case 'PRIVATE':
                if (activePrivateChatUser &&
                    (message.sender === activePrivateChatUser || message.receiver === activePrivateChatUser)) {
                    appendMessage(message, 'private');
                }
                break;

            case 'ROOM':
                appendMessage(message, 'room'); // ✅ important
                break;

            default:
                console.warn('Unknown message type');
        }
    }


    // --- Append messages ---
    function appendMessage(message, chatType) {
        messagesArea.classList.remove('centered');

        const msgDiv = document.createElement('div');
        const isSelf = message.sender === currentUsername;

        msgDiv.className = isSelf ? 'message self' : 'message';

        const now = new Date(message.timestamp || Date.now());
        const HH = String(now.getHours()).padStart(2, '0');
        const MM = String(now.getMinutes()).padStart(2, '0');

        const content = message.content || (message.type === 'JOIN' ? `${message.sender} joined` : `${message.sender} left`);

        // Only show private messages if this is the correct chat
        if (chatType === 'private') {
            if (!currentChat || currentChat.type !== 'private') return;
            if (
                message.sender !== currentChat.username &&
                message.receiver !== currentChat.username &&
                !isSelf
            ) return;
        }

        if (chatType === 'room') {
            if (!window.activeRoom || message.roomId !== window.activeRoom.id) return;
        }

        msgDiv.innerHTML = `
            ${escapeHtml(content)}
            <div class="meta">${isSelf ? 'You' : message.sender.split(" ")[0]} · ${HH}:${MM}</div>
        `;

        messagesArea.appendChild(msgDiv);
        messagesArea.scrollTop = messagesArea.scrollHeight;
    }

    function appendSystemMessage(message) {
        messagesArea.classList.remove('centered');

        const msgDiv = document.createElement('div');
        msgDiv.className = 'message system';

        const now = new Date(message.timestamp || Date.now());
        const HH = String(now.getHours()).padStart(2, '0');
        const MM = String(now.getMinutes()).padStart(2, '0');

        const content = message.type === 'JOIN' ? `${message.sender} joined` : `${message.sender} left`;

        msgDiv.innerHTML = `
            ${escapeHtml(content)}
            <div class="meta">System · ${HH}:${MM}</div>
        `;

        messagesArea.appendChild(msgDiv);
        messagesArea.scrollTop = messagesArea.scrollHeight;
    }

    function escapeHtml(str) {
        return String(str)
            .replaceAll('&', '&amp;')
            .replaceAll('<', '&lt;')
            .replaceAll('>', '&gt;');
    }

    // --- Welcome screen ---
    function showWelcome() {
        const welcomeText = document.getElementById('welcome-text');
        if (welcomeText) welcomeText.textContent = `Welcome, ${currentUsername.split(" ")[0]}!`;

        messagesArea.classList.add('centered');
        messagesArea.innerHTML = `
            <div class="welcome-message">Welcome to Chat App created by Sujay Kumar Kotal
                  <span class="line top"></span>
                  <span class="line right"></span>
                  <span class="line bottom"></span>
                  <span class="line left"></span>
            </div>
        `;
        chatInput.disabled = true;
        sendBtn.disabled = true;
    }

    // --- Logout ---
    if (logout) {
        logout.onclick = async function () {
            try {
                await fetch('http://localhost:8080/api/users/logout', {
                    method: 'POST',
                    headers: {
                        Authorization: `Bearer ${token}`,
                        'Content-Type': 'application/json',
                    },
                });
            } catch (err) {
                console.error('Logout error occurred');
            }
            if (stompClient) stompClient.disconnect();
            localStorage.removeItem('token');
            localStorage.removeItem('username');
            window.location.href = 'index.html';
        };
    }

    // --- Load online users ---
    async function loadOnlineUsers() {
        try {
            const res = await fetch('http://localhost:8080/api/users/online', {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (res.ok) {
                const users = await res.json();
                populateUsersList(users || []);
            } else {
                console.warn('Failed to fetch online users:');
            }
        } catch (e) {
            console.error('Error loading online users:');
        }
    }

    function populateUsersList(users) {
        privateUsersList.innerHTML = '';
        users.forEach((user) => {
            if (user && user !== currentUsername) {
                const el = document.createElement('div');
                el.className = 'private-user';
                el.textContent = user;
                el.dataset.username = user;
                privateUsersList.appendChild(el);
            }
        });

        if (
            currentChat &&
            currentChat.type === 'private' &&
            !users.includes(currentChat.username)
        ) {
            currentChat = null;
            showWelcome();
        }
    }

    //Load chat history
    async function loadConversation(user) {
        try {
            const res = await fetch(`http://localhost:8080/api/messages/conversation/${currentUsername}/${user}`, {
                headers: { Authorization: `Bearer ${token}` },
            });
            if (res.ok) {
                const messages = await res.json();
                messagesArea.innerHTML = ''; // clear old messages
                messages.forEach(msg => {
                    appendMessage(msg, 'private');
                });
                messagesArea.scrollTop = messagesArea.scrollHeight;
            } else {
                console.error("Failed to load conversation:", res.status);
            }
        } catch (err) {
            console.error("Error loading conversation:");
        }
    }




    // --- Select private user ---
    privateUsersList.addEventListener('click', (e) => {
        const user = e.target.closest('.private-user');
        if (!user) return;

        // Remove active from ALL private users
        privateUsersList.querySelectorAll('.private-user.active')
            .forEach(el => el.classList.remove('active'));

// Remove active from all rooms too
        document.querySelectorAll('.room.active')
            .forEach(el => el.classList.remove('active'));

        user.classList.add('active');


        chatHeader.innerHTML = `
           <div class="chat-header-user">
            <span>Chat with </span>
            <span class="name" style="font-weight: bold">${user.textContent.split(" ")[0]}</span>
            </div>
            <div class="room-menu">
                <button onclick="toggleDropdown()" id="roomMenuBtn">＋</button>
                <div id="roomDropdown">
                    <button onclick="openModal('createRoomModal')">➕ Create Room</button>
                    <button onclick="openModal('joinRoomModal')">🔗 Join Room</button>
                </div>
            </div>
        `;

        activePrivateChatUser = user.dataset.username;
        currentChat = { type: 'private', username:  activePrivateChatUser};
        messagesArea.classList.remove('centered');
        messagesArea.innerHTML = '';
        chatInput.disabled = false;
        sendBtn.disabled = false;
        chatInput.focus();
        loadConversation(user.dataset.username);
    });

    // --- Send message ---
    chatForm.addEventListener('submit', (e) => {
        e.preventDefault();
        if (!stompClient) return;

        const text = chatInput.value.trim();
        if (!text) return;

        let message;

        if (currentChat && currentChat.type === 'private') {
            // Private chat → correct
            message = {
                sender: currentUsername,
                receiver: currentChat.username,
                content: text,
                type: "PRIVATE",
                timestamp: new Date().toISOString(),
            };
            stompClient.send('/app/chat.privateMessage', {}, JSON.stringify(message));

        } else if (window.activeRoom) {
            // Room chat → correct
            message = {
                sender: currentUsername,
                content: text,
                type: "ROOM",
                roomId: window.activeRoom.id,
                timestamp: new Date().toISOString(),
            };
            stompClient.send('/app/chat.roomMessage', {}, JSON.stringify(message));

        } else {
            // Public chat
            message = {
                sender: currentUsername,
                content: text,
                type: "CHAT",
                timestamp: new Date().toISOString(),
            };
            stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
        }

        chatInput.value = '';
        chatInput.focus();
    });



    document.getElementById("create-room-form").addEventListener("submit", async function (e) {
        e.preventDefault();

        const roomName = document.getElementById("create-room-input").value.trim();
        const description = document.getElementById("Description").value.trim();
        const errorDiv = document.getElementById("create-room-error");

        errorDiv.style.display = "none";

        if (!roomName || !description) {
            alert("Please fill in all fields.");
            return;
        }

        try {
            const response = await fetch(`api/rooms/create`, {
                method: "POST",
                headers: {
                    "Content-Type": "application/json",
                    // if you’re using JWT auth
                    "Authorization": "Bearer " + localStorage.getItem("token")
                },
                body: JSON.stringify({
                    name: roomName,
                    description: description
                })
            });

            if (response.ok) {
                const room = await response.json();
                // alert("Room created successfully: " + room.name);
                chatHeader.innerHTML=`
                <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;" class="room-name">
                             <span class="room-name">${room.name}</span>
                </div>
                <div class="room-menu">
                    <button onclick="toggleDropdown()" id="roomMenuBtn">＋</button>
                    <div id="roomDropdown">
                        <button onclick="openModal('createRoomModal')">➕ Create Room</button>
                        <button onclick="openModal('joinRoomModal')">🔗 Join Room</button>
                    </div>
                </div>`;

                document.querySelectorAll(".room").forEach(r => r.classList.remove("active"));
                document.querySelectorAll(".private-user.active")
                    .forEach(el => el.classList.remove("active"));
                const clickedRoom = document.querySelector(`.room[data-room-id="${room.id}"]`);
                clickedRoom.classList.add("active");
                renderRoom(room);

                // close modal & reset form
                closeModal('createRoomModal');
                document.getElementById("create-room-form").reset();
                renderRoom(room);
            } else {

            }
        } catch (err) {
            console.error("Error creating room:");
            errorDiv.textContent = "Something went wrong! Please try again.";
            errorDiv.style.display = "block";
        }
    });

// Render room in sidebar
    function renderRoom(room) {
        const roomDiv = document.createElement("div");
        roomDiv.classList.add("room");
        roomDiv.textContent = room.name;
        roomDiv.dataset.roomId = room.id;

        roomDiv.addEventListener("click", () => {
            joinRoomUI(room);
        });

        document.getElementById("rooms-list").appendChild(roomDiv);
    }

// Handle room selection
    function joinRoomUI(room) {
        if(!room){
            console.log("Invalid room");
            return;
        }
        window.activeRoom = room;

        currentChat = {
            type: "room",
            roomId: room.id,
            subscription: null
        };



        // join room
        stompClient.subscribe(`/topic/rooms/${room.id}`, (frame) => {
            const msg = JSON.parse(frame.body);
            handleIncoming(msg); // appendMessage with chatType='room'
        });
        // Remove highlight from all rooms
        document.querySelectorAll(".room").forEach(r => r.classList.remove("active"));
// Remove highlight from all private users
        document.querySelectorAll(".private-user.active")
            .forEach(el => el.classList.remove("active"));

// Highlight only this room
        const clickedRoom = document.querySelector(`.room[data-room-id="${room.id}"]`);
        if (clickedRoom) clickedRoom.classList.add("active");


        // Update header with name + id
        const chatHeader = document.getElementById("chat-header");
        chatHeader.innerHTML = `
        <div style="display: flex; justify-content: space-between; align-items: center; width: 100%;">
            <span class="room-name" style="font-weight: bold">${room.name}</span>
            <button class="info-btn" style="font-family: 'Times New Roman',sans-serif; font-weight: bold;  font-size: 15px" onclick="showRoomInfo('${room.id}')">View Room Details ℹ️</button>
            <button onclick="leaveRoom()" class="leave-btn"><img src="https://freeimghost.net/images/2025/09/09/leave-room.png" alt="Leave" style="cursor: pointer"></button>
        </div>
        <div class="room-menu">
            <button onclick="toggleDropdown()" id="roomMenuBtn">＋</button>
            <div id="roomDropdown">
                <button onclick="openModal('createRoomModal')">➕ Create Room</button>
                <button onclick="openModal('joinRoomModal')">🔗 Join Room</button>
            </div>
        </div>
    `;

        const copyBtn = document.getElementById('copyBtn');

        copyBtn.addEventListener('click', () => {
            // Copy the room ID to clipboard
            navigator.clipboard.writeText(room.id)
                .then(() => {
                    copyBtn.textContent = 'Copied!';
                    setTimeout(() => copyBtn.textContent = 'Copy', 1500);
                })
                .catch(err => {
                    console.error('Failed to copy. ');
                });
        });

        // Enable chat input + send button
        document.getElementById("chat-input").disabled = false;
        document.querySelector(".chat-input-area button").disabled = false;

        // Clear old messages
        const messagesArea = document.getElementById("messages-area");
        messagesArea.innerHTML = "";
        messagesArea.classList.remove("centered");
        loadRoomMessages(room.id);
    }

// Handle sending messages
    document.getElementById("chat-form").addEventListener("submit", async (e) => {
        e.preventDefault();

        const input = document.getElementById("chat-input");
        const message = input.value.trim();
        if (!message || !window.activeRoom) return;

        // join room
        stompClient.subscribe(`/topic/rooms/${activeRoom.id}`, (frame) => {
            const msg = JSON.parse(frame.body);
            handleIncoming(msg); // appendMessage with chatType='room'
        });

        try {
            const response = await fetch(`/rooms/${window.activeRoom.id}/messages`, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ content: message })
            });

            if (!response.ok) throw new Error("Failed to send message");

            input.value = "";
        } catch (err) {
            console.error("Failed to send message", err);
        }
    });

    async function loadRooms(username) {
        try {
            const token = localStorage.getItem("token"); // JWT from login
            const response = await fetch(`/api/rooms/my?user=${encodeURIComponent(username)}`, {
                headers: {
                    "Authorization": `Bearer ${token}`,
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                const text = await response.text();
                throw new Error(text || "Failed to fetch rooms");
            }

            const rooms = await response.json();
            const roomsList = document.getElementById("rooms-list");
            roomsList.innerHTML = "";

            rooms.forEach(room => renderRoom(room));
        } catch (err) {
            console.error("Error loading rooms:", err);
        }
    }

    async function loadRoomMessages(roomId) {
        try {
            const res = await fetch(`/api/messages/room/${roomId}`, {
                headers: { "Authorization": `Bearer ${token}` }
            });
            if (!res.ok) throw new Error("Failed to load room messages");

            const messages = await res.json();
            const messagesArea = document.getElementById("messages-area");
            messagesArea.innerHTML = ""; // Clear old messages

            messages.forEach(msg => appendMessage(msg, 'room'));
            messagesArea.scrollTop = messagesArea.scrollHeight;
        } catch (err) {
            console.error("Error loading room messages:", err);
        }
    }

    window.joinRoomFromInput = function joinRoomFromInput() {
        const roomId = document.getElementById("join-room-input").value.trim();
       // or however you store current user

        if (!roomId) {
            alert("Please enter a room ID");
            return;
        }

        if (!currentUsername) {
            alert("User not logged in");
            return;
        }

        joinRoom(roomId, currentUsername); // ✅ call the joinRoom function I gave you earlier
    };


    window.joinRoom =  async function joinRoom(roomId, username) {
        let activeRoomId;
        try {
            const response = await fetch(`/api/rooms/${roomId}/join?username=${username}`, {
                method: "POST",
                headers: {
                    "Authorization": "Bearer " + localStorage.getItem("token"),
                    "Content-Type": "application/json"
                }
            });

            if (!response.ok) {
                const errorText = await response.text();
               throw new Error(errorText);
            }

            const room2 = await response.json();
            window.location.reload();
            renderRoom(room2); // Add room to sidebar if not already present


            // ✅ Subscribe immediately after joining
            if (stompClient && stompClient.connected) {
                stompClient.subscribe(`/topic/rooms/${room2.id}`, (payload) => {
                    const message = JSON.parse(payload.body);
                    handleIncoming(message);
                });
            }

            joinRoomUI(room2); // Update UI to show this room

            closeModal('joinRoomModal');
            document.getElementById("join-room-form").reset();

            // ✅ Select this room as active immediately
            activeRoomId = room2.id;
            document.getElementById("room-title").innerText = `Room: ${room2.name}`;
            document.getElementById("message-container").innerHTML = "";

            alert(`You joined room ${room2.name}`);
        } catch (err) {
            console.error("Error joining room:", err);
        }
    };
    function clearActiveSidebar() {
        document.querySelectorAll(".sidebar-item.active")
            .forEach(el => el.classList.remove("active"));
    }


   window.leaveRoom = function leaveRoom (){
        if (confirm("Are you sure you want to leave this room?")) {
            if (currentChat && currentChat.type === "room") {
                // 1. Unsubscribe from WebSocket topic
                if (currentChat.subscription) {
                    currentChat.subscription.unsubscribe();
                }

                // 2. Remove from DB participants
                fetch(`/api/rooms/${currentChat.roomId}/leave`, {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify({ username: currentUsername })
                })
                    .then(res => {
                        if (res.ok) {

                        } else {
                            console.error("❌ Failed to leave room");
                        }
                    })
                    .catch(err => console.error("❌ Error leaving room:", err));

                // 3. Reset UI
                alert(`You left the room: ${currentChat.roomId}`);
                window.location.reload();
                currentChat = null;
                document.getElementById("chatWindow").innerHTML = "";
                document.getElementById("roomTitle").innerText = "No Room Selected";
            }
        }
    }


    window.showRoomInfo = async function(roomId) {
        try {
            const res = await fetch(`/api/rooms/${roomId}`, {
                headers: { "Authorization": "Bearer " + localStorage.getItem("token") }
            });
            if (!res.ok) throw new Error("Failed to fetch room details");

            const room = await res.json();


            document.getElementById("room-title").innerText = room.name;
            document.getElementById("roomId").innerText = roomId;
            document.getElementById("room-desc").innerText = room.description || "No description";
            const participantsList = document.getElementById("room-participants");
            participantsList.innerHTML = ""; // clear old list
            if (room.participants && room.participants.length > 0) {
                room.participants.forEach(p => {
                    const li = document.createElement("li");
                    li.innerText = p;
                    participantsList.appendChild(li);
                });
            } else {
                const li = document.createElement("li");
                li.innerText = "No participants yet";
                participantsList.appendChild(li);
            }
            document.getElementById("room-creator").innerText = room.createdBy || "Unknown";

            document.getElementById("room-info-popup").style.display = "block";
        } catch (err) {
            console.error("Error loading room info:");
        }
    };

    window.closeRoomInfo = function() {
        document.getElementById("room-info-popup").style.display = "none";
    };




    // --- Init ---
    showWelcome();
    loadOnlineUsers();
    connectWebSocket();
    loadRooms(currentUsername);
    // setInterval(loadOnlineUsers,10000);
    // setInterval(() => loadRooms(currentUsername),1000);
});
