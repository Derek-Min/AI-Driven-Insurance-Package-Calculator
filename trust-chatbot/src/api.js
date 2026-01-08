import axios from "axios";

const API_BASE_URL =
    import.meta.env.MODE === "production"
        ? "https://api.trust-insurancexyz.xyz/api"
        : "http://localhost:8080/api";

const api = axios.create({
    baseURL: API_BASE_URL,
    timeout: 10000
});

export async function sendMessage(sessionId, message) {
    try {
        const res = await api.post("/chatbot", {
            sessionId,
            message
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
