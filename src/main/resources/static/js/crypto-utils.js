/**
 * Performs all browser-side end-to-end cryptography using the native Web Crypto API so
 * plaintext, private keys, AES session keys, and integrity verification remain client-side.
 */
const CryptoUtils = {
    /**
     * Generates an RSA-OAEP 2048-bit key pair so the browser can protect per-message AES keys.
     */
    async generateRSAKeyPair() {
        return window.crypto.subtle.generateKey(
            {
                name: "RSA-OAEP",
                modulusLength: 2048,
                publicExponent: new Uint8Array([1, 0, 1]),
                hash: "SHA-256"
            },
            true,
            ["encrypt", "decrypt"]
        );
    },

    /**
     * Exports the RSA public key as Base64 SPKI so it can be stored on the server and shared.
     */
    async exportPublicKey(publicKey) {
        const buffer = await window.crypto.subtle.exportKey("spki", publicKey);
        return this._arrayBufferToBase64(buffer);
    },

    /**
     * Exports the RSA private key as Base64 PKCS8 so it can stay only in browser localStorage.
     */
    async exportPrivateKey(privateKey) {
        const buffer = await window.crypto.subtle.exportKey("pkcs8", privateKey);
        return this._arrayBufferToBase64(buffer);
    },

    /**
     * Imports a recipient's Base64 SPKI public key for RSA-OAEP encryption of the AES key.
     */
    async importPublicKey(base64Key) {
        const buffer = this._base64ToArrayBuffer(base64Key);
        return window.crypto.subtle.importKey(
            "spki",
            buffer,
            { name: "RSA-OAEP", hash: "SHA-256" },
            false,
            ["encrypt"]
        );
    },

    /**
     * Imports the current user's Base64 PKCS8 private key for RSA-OAEP decryption of the AES key.
     */
    async importPrivateKey(base64Key) {
        const buffer = this._base64ToArrayBuffer(base64Key);
        return window.crypto.subtle.importKey(
            "pkcs8",
            buffer,
            { name: "RSA-OAEP", hash: "SHA-256" },
            false,
            ["decrypt"]
        );
    },

    /**
     * Encrypts raw data with RSA-OAEP and returns Base64 so the wrapped AES key can be sent safely.
     */
    async encryptWithRSA(data, publicKey) {
        const encrypted = await window.crypto.subtle.encrypt({ name: "RSA-OAEP" }, publicKey, data);
        return this._arrayBufferToBase64(encrypted);
    },

    /**
     * Decrypts an RSA-OAEP Base64 payload back into raw bytes so the AES session key can be recovered.
     */
    async decryptWithRSA(encryptedBase64, privateKey) {
        const buffer = this._base64ToArrayBuffer(encryptedBase64);
        return window.crypto.subtle.decrypt({ name: "RSA-OAEP" }, privateKey, buffer);
    },

    /**
     * Generates a fresh AES-256-CBC key for one message to preserve confidentiality with key rotation.
     */
    async generateAESKey() {
        return window.crypto.subtle.generateKey(
            { name: "AES-CBC", length: 256 },
            true,
            ["encrypt", "decrypt"]
        );
    },

    /**
     * Exports the AES key to raw bytes so it can be wrapped with the recipient's RSA public key.
     */
    async exportAESKey(aesKey) {
        return window.crypto.subtle.exportKey("raw", aesKey);
    },

    /**
     * Imports raw AES key bytes for decryption of the ciphertext with AES-CBC.
     */
    async importAESKey(keyBytes) {
        return window.crypto.subtle.importKey(
            "raw",
            keyBytes,
            { name: "AES-CBC" },
            false,
            ["decrypt"]
        );
    },

    /**
     * Encrypts plaintext with AES-256-CBC using a fresh random IV for every message.
     */
    async encryptWithAES(plaintext, aesKey) {
        const iv = window.crypto.getRandomValues(new Uint8Array(16));
        const encoded = new TextEncoder().encode(plaintext);
        const encrypted = await window.crypto.subtle.encrypt(
            { name: "AES-CBC", iv },
            aesKey,
            encoded
        );
        return {
            ciphertext: this._arrayBufferToBase64(encrypted),
            iv: this._arrayBufferToBase64(iv.buffer)
        };
    },

    /**
     * Decrypts AES-CBC Base64 ciphertext into plaintext using the recovered AES key and IV.
     */
    async decryptWithAES(ciphertextBase64, aesKey, ivBase64) {
        const iv = new Uint8Array(this._base64ToArrayBuffer(ivBase64));
        const ciphertext = this._base64ToArrayBuffer(ciphertextBase64);
        const decrypted = await window.crypto.subtle.decrypt(
            { name: "AES-CBC", iv },
            aesKey,
            ciphertext
        );
        return new TextDecoder().decode(decrypted);
    },

    /**
     * Computes a lowercase SHA-256 hex digest used to verify message integrity after decryption.
     */
    async computeSHA256(text) {
        const encoded = new TextEncoder().encode(text);
        const digest = await window.crypto.subtle.digest("SHA-256", encoded);
        return Array.from(new Uint8Array(digest))
            .map((byte) => byte.toString(16).padStart(2, "0"))
            .join("");
    },

    /**
     * Recomputes and compares SHA-256 hashes to detect whether a decrypted message was tampered with.
     */
    async verifySHA256(text, expectedHash) {
        const actualHash = await this.computeSHA256(text);
        return actualHash === expectedHash;
    },

    /**
     * Executes the full browser-side encryption flow: hash plaintext, generate AES key, encrypt
     * the message with AES-CBC, then wrap the AES key with the recipient's RSA public key.
     */
    async encryptMessage(plaintext, receiverPublicKeyBase64) {
        const messageHash = await this.computeSHA256(plaintext);
        const aesKey = await this.generateAESKey();
        const { ciphertext, iv } = await this.encryptWithAES(plaintext, aesKey);
        const aesKeyBytes = await this.exportAESKey(aesKey);
        const receiverPublicKey = await this.importPublicKey(receiverPublicKeyBase64);
        const encryptedAesKey = await this.encryptWithRSA(aesKeyBytes, receiverPublicKey);

        return {
            encryptedMessage: ciphertext,
            encryptedAesKey,
            iv,
            messageHash
        };
    },

    /**
     * Executes the full browser-side decryption flow using the current user's private RSA key,
     * then verifies the SHA-256 hash of the recovered plaintext.
     */
    async decryptMessage(encryptedPayload, currentUsername) {
        const privateKeyB64 = localStorage.getItem("privateKey_" + currentUsername);
        if (!privateKeyB64) {
            throw new Error("Private key not found");
        }

        const privateKey = await this.importPrivateKey(privateKeyB64);
        const aesKeyBytes = await this.decryptWithRSA(encryptedPayload.encryptedAesKey, privateKey);
        const aesKey = await this.importAESKey(aesKeyBytes);
        const plaintext = await this.decryptWithAES(
            encryptedPayload.encryptedMessage,
            aesKey,
            encryptedPayload.iv
        );
        const isValid = await this.verifySHA256(plaintext, encryptedPayload.messageHash);
        return { plaintext, isValid };
    },

    /**
     * Converts ArrayBuffer data into Base64 so encrypted values can be stored and sent as JSON.
     */
    _arrayBufferToBase64(buffer) {
        const bytes = new Uint8Array(buffer);
        let binary = "";
        for (let i = 0; i < bytes.byteLength; i += 1) {
            binary += String.fromCharCode(bytes[i]);
        }
        return window.btoa(binary);
    },

    /**
     * Converts a Base64 string back into an ArrayBuffer so Web Crypto can consume the data again.
     */
    _base64ToArrayBuffer(base64) {
        const binary = window.atob(base64);
        const bytes = new Uint8Array(binary.length);
        for (let i = 0; i < binary.length; i += 1) {
            bytes[i] = binary.charCodeAt(i);
        }
        return bytes.buffer;
    }
};
