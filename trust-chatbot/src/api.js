import axios from "axios";

const client = axios.create({
    baseURL: "/",   // important
    timeout: 10000,
});

export function sendMessage(sessionId, text) {
    return client.post("/chatbot", {
        sessionId: sessionId,
        text: text,
        type: "message"
    });
}

export function confirmSend(sessionId) {
    return client.post("/chatbot", {
        sessionId: sessionId,
        type: "confirm"
    });
}
