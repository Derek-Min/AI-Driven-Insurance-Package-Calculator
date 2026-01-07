import axios from "axios";

const api = axios.create({
    baseURL: "/chatbot", // 👈 proxy through Pages
    timeout: 10000
});

export default api;

export async function sendMessage(sessionId, message) {
    try {
        const res = await api.post("", {
            sessionId,
            message
        });

        const data = res.data;

        return {
            sessionId: data.sessionId,
            messages: Array.isArray(data.messages) ? data.messages : [],
            endSession: Boolean(data.endSession)
        };
    } catch (err) {
        console.error("Chatbot API error:", err);
        return {
            messages: ["⚠️ Backend not reachable."],
            endSession: false
        };
    }
}
