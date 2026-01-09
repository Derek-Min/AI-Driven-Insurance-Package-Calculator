import axios from "axios";

const api = axios.create({
    baseURL: '/api',
    timeout: 10000,
    headers: {
        "Content-Type": "application/json"
    }
});

export async function sendMessage(sessionId, message) {
    try {
        const res = await api.request({
            url: "/chatbot",
            method: "POST",
            data: { sessionId, message }
        });
        return res.data;
    } catch (err) {
        console.error("Chatbot API error:", err);
        return {
            messages: ["⚠️ Backend not reachable"],
            endSession: false
        };
    }
}

