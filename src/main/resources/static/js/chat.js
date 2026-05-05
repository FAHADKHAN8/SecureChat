let stompClient = null;
let currentChatUser = null;
let myUsername = null;
let lastEncryptedMessages = [];

/**
 * Initializes the chat page by loading the authenticated username, checking for the local
 * private key, and opening the WebSocket connection used to exchange encrypted payloads.
 */
function initChat() {
    const serverData = document.getElementById('serverData');
    myUsername = serverData ? serverData.dataset.currentUser : null;

    if (!myUsername) {
        showToast('Could not determine the authenticated user.', 'error', 4000);
        return;
    }

    if (!localStorage.getItem('privateKey_' + myUsername)) {
        showToast('Private key not found in this browser. Messages sent to you cannot be decrypted here.', 'warning', 5000);
    }

    connectWebSocket();
}

/**
 * Connects to the SockJS/STOMP endpoint so encrypted chat payloads can be delivered in real time.
 */
function connectWebSocket() {
    const socket = new SockJS('/ws');
    stompClient = Stomp.over(socket);
    stompClient.debug = () => {};

    stompClient.connect({}, () => {
        stompClient.subscribe('/user/queue/messages', (message) => {
            handleIncomingMessage(JSON.parse(message.body));
        });

        stompClient.subscribe('/topic/public', (message) => {
            handleIncomingMessage(JSON.parse(message.body));
        });

        stompClient.send('/app/chat.join', {}, JSON.stringify({ type: 'JOIN' }));
        showToast('Secure WebSocket connected.', 'success', 2500);
    }, () => {
        showToast('WebSocket connection lost. Retrying...', 'error', 3000);
        window.setTimeout(connectWebSocket, 3000);
    });
}

/**
 * Encrypts plaintext in the browser with AES-256-CBC and RSA-OAEP, then sends only encrypted
 * fields to the WebSocket endpoint so the server never sees message contents.
 */
async function sendMessage() {
    const input = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');
    const plaintext = input.value.trim();

    if (!currentChatUser) {
        showToast('Select a user before sending a message.', 'warning', 3000);
        return;
    }

    if (!plaintext) {
        return;
    }

    if (!stompClient || !stompClient.connected) {
        showToast('Chat connection is not ready yet.', 'error', 3000);
        return;
    }

    sendBtn.disabled = true;

    try {
        const publicKeyResponse = await fetch('/api/public-key/' + encodeURIComponent(currentChatUser));
        if (!publicKeyResponse.ok) {
            throw new Error('Could not fetch recipient public key.');
        }

        const keyData = await publicKeyResponse.json();
        const encryptedPayload = await CryptoUtils.encryptMessage(plaintext, keyData.publicKey);

        const dto = {
            senderUsername: myUsername,
            receiverUsername: currentChatUser,
            encryptedMessage: encryptedPayload.encryptedMessage,
            encryptedAesKey: encryptedPayload.encryptedAesKey,
            iv: encryptedPayload.iv,
            messageHash: encryptedPayload.messageHash,
            timestamp: new Date().toISOString(),
            type: 'CHAT'
        };

        stompClient.send('/app/chat.send', {}, JSON.stringify(dto));
        displayMessage({
            text: plaintext,
            sender: myUsername,
            timestamp: new Date().toISOString(),
            isOwn: true,
            isValid: true,
            isError: false,
            encryptedData: dto
        });
        input.value = '';
    } catch (error) {
        showToast(error.message || 'Failed to send encrypted message.', 'error', 4000);
    } finally {
        sendBtn.disabled = false;
        input.focus();
    }
}

/**
 * Processes live WebSocket messages, decrypting messages intended for the current user and
 * displaying sender copies without plaintext because only the recipient has the right private key.
 */
async function handleIncomingMessage(message) {
    if (message.type === 'JOIN' || message.type === 'LEAVE') {
        if (message.senderUsername && message.senderUsername !== myUsername) {
            showToast(message.senderUsername + ' joined the secure channel.', 'info', 2500);
        }
        return;
    }

    if (message.senderUsername !== currentChatUser && message.receiverUsername !== myUsername) {
        showToast('New encrypted message from ' + message.senderUsername, 'info', 3000);
    }

 if (message.senderUsername === myUsername) {
     return; // already displayed locally in sendMessage()
 }

    if (message.receiverUsername === myUsername) {
        try {
            const decrypted = await CryptoUtils.decryptMessage(message, myUsername);
            displayMessage({
                text: decrypted.plaintext,
                sender: message.senderUsername,
                timestamp: message.timestamp,
                isOwn: false,
                isValid: decrypted.isValid,
                isError: !decrypted.isValid,
                encryptedData: message
            });
        } catch (error) {
            displayMessage({
                text: 'Unable to decrypt message: ' + error.message,
                sender: message.senderUsername,
                timestamp: message.timestamp,
                isOwn: false,
                isValid: false,
                isError: true,
                encryptedData: message
            });
        }
    }
}

/**
 * Opens a conversation, updates the UI header, highlights the selected user, and loads encrypted history.
 */
function openChat(element) {
    const username = element.getAttribute("data-username");

    currentChatUser = username;

    document.getElementById('welcomeScreen').style.display = 'none';
    document.getElementById('chatWindow').style.display = 'flex';
    document.getElementById('chatHeaderAvatar').textContent = username.charAt(0).toUpperCase();
    document.getElementById('chatHeaderName').textContent = username;
    document.getElementById('chatHeaderStatus').textContent = 'Secure conversation with ' + username;

    document.querySelectorAll('.user-item').forEach((item) => {
        item.classList.toggle('active', item.dataset.username === username);
    });

    loadHistory(username);
}

/**
 * Closes the current chat panel so the user can return to the welcome state without reloading.
 */
function closeChatWindow() {
    currentChatUser = null;
    document.getElementById('welcomeScreen').style.display = 'flex';
    document.getElementById('chatWindow').style.display = 'none';
    document.getElementById('messagesContainer').innerHTML = '';
    document.querySelectorAll('.user-item').forEach((item) => item.classList.remove('active'));
}

/**
 * Loads encrypted conversation history and decrypts only the messages addressed to the current browser user.
 */
async function loadHistory(username) {
    const messagesContainer = document.getElementById('messagesContainer');
    messagesContainer.innerHTML = '';
    lastEncryptedMessages = [];

    try {
        const response = await fetch('/api/messages/history?with=' + encodeURIComponent(username));
        if (!response.ok) {
            throw new Error('Could not load message history.');
        }

        const messages = await response.json();
        for (const message of messages) {
           // lastEncryptedMessages.push(message);
   if (message.senderUsername === myUsername) {
       displayMessage({
           text: 'You: [Sent securely]',
           sender: message.senderUsername,
           timestamp: message.timestamp,
           isOwn: true,
           isValid: true,
           isError: false,
           encryptedData: message
       });
   }
            else {
                try {
                    const decrypted = await CryptoUtils.decryptMessage(message, myUsername);
                    displayMessage({
                        text: decrypted.plaintext,
                        sender: message.senderUsername,
                        timestamp: message.timestamp,
                        isOwn: false,
                        isValid: decrypted.isValid,
                        isError: !decrypted.isValid,
                        encryptedData: message
                    });
                } catch (error) {
                    displayMessage({
                        text: 'Unable to decrypt historical message: ' + error.message,
                        sender: message.senderUsername,
                        timestamp: message.timestamp,
                        isOwn: false,
                        isValid: false,
                        isError: true,
                        encryptedData: message
                    });
                }
            }
        }

        scrollToBottom();
    } catch (error) {
        showToast(error.message || 'Failed to load history.', 'error', 4000);
    }
}

/**
 * Renders a single chat bubble, escapes plaintext to prevent HTML injection, and stores the
 * encrypted payload so users can inspect the raw academic demo artifacts on click.
 */
function displayMessage({ text, sender, timestamp, isOwn, isValid, isError, encryptedData }) {
    if (!currentChatUser) {
        return;
    }



   const isIncoming =
       sender === currentChatUser &&
       encryptedData.receiverUsername === myUsername;

   const isOutgoing =
       sender === myUsername &&
       encryptedData.receiverUsername === currentChatUser;

   if (!isIncoming && !isOutgoing) return;


    const container = document.getElementById('messagesContainer');
    const messageDiv = document.createElement('div');
    messageDiv.className = 'message ' + (isOwn ? 'message-own' : 'message-other');

    const integrity = isError ? ' Integrity failed' : (isValid ? ' Integrity verified' : '⚠ Integrity unknown');
    const safeText = escapeHtml(text);

    messageDiv.innerHTML = `
        <div class="message-bubble">${safeText}</div>
        <div class="integrity-badge">${integrity}</div>
        <div class="message-time">${escapeHtml(timestamp || '')}</div>
    `;

messageDiv.addEventListener('click', () => {
    window.lastSelectedMessage = { encryptedData, text };
    showEncryptedDetails(encryptedData, text);
});

container.appendChild(messageDiv);
    scrollToBottom();
}

/**
 * Shows the encrypted payload, IV, SHA-256 hash, and the displayed plaintext for academic inspection.
 */
function showEncryptedDetails(encryptedData, plaintext) {
    const encryptedPanel = document.getElementById('encryptedPanel');
    const encryptedDataDiv = document.getElementById('encryptedData');

    encryptedDataDiv.innerHTML = `
        <div class="enc-section">
            <h4>Displayed Plaintext</h4>
            <div class="enc-value">${escapeHtml(plaintext || '')}</div>
        </div>
        <div class="enc-section">
            <h4>Encrypted Message</h4>
            <div class="enc-value ciphertext">${escapeHtml(encryptedData.encryptedMessage || '')}</div>
        </div>
        <div class="enc-section">
            <h4>Encrypted AES Key</h4>
            <div class="enc-value">${escapeHtml(encryptedData.encryptedAesKey || '')}</div>
        </div>
        <div class="enc-section">
            <h4>IV</h4>
            <div class="enc-value">${escapeHtml(encryptedData.iv || '')}</div>
        </div>
        <div class="enc-section">
            <h4>SHA-256 Hash</h4>
            <div class="enc-value hash">${escapeHtml(encryptedData.messageHash || '')}</div>
        </div>
    `;

    encryptedPanel.style.display = 'block';
}

/**
 * Toggles the encrypted payload panel so users can inspect or hide raw ciphertext details.
 */
function toggleEncryptedView() {
    const panel = document.getElementById('encryptedPanel');

    if (panel.style.display === 'block') {
        panel.style.display = 'none';
        return;
    }

    // 🔥 show last selected message
    if (window.lastSelectedMessage) {
        showEncryptedDetails(
            window.lastSelectedMessage.encryptedData,
            window.lastSelectedMessage.text
        );
    } else {
        showToast('Click a message first to view encrypted data.', 'warning', 3000);
    }
}

/**
 * Filters sidebar users by username so it is easier to find a conversation partner quickly.
 */
function filterUsers(query) {
    const normalized = (query || '').toLowerCase();
    document.querySelectorAll('.user-item').forEach((item) => {
        const username = item.dataset.username.toLowerCase();
        item.style.display = username.includes(normalized) ? 'flex' : 'none';
    });
}

/**
 * Displays a temporary toast notification to communicate chat status, errors, and connection events.
 */
function showToast(message, type, duration) {
    const toast = document.getElementById('toast');
    toast.className = 'toast toast-' + type;
    toast.textContent = message;
    toast.style.display = 'block';

    window.clearTimeout(showToast._timeoutId);
    showToast._timeoutId = window.setTimeout(() => {
        toast.style.display = 'none';
    }, duration || 3000);
}

/**
 * Scrolls the messages container to the newest message after rendering or loading history.
 */
function scrollToBottom() {
    const container = document.getElementById('messagesContainer');
    container.scrollTop = container.scrollHeight;
}

/**
 * Escapes user-visible plaintext so decrypted content cannot inject HTML into the chat interface.
 */
function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text ?? '';
    return div.innerHTML;
}

document.addEventListener('DOMContentLoaded', initChat);
