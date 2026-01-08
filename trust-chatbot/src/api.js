import axios from "axios";

const api = axios.create({
    baseURL:
        import.meta.env.MODE === "production"
            ? "https://api.trust-insurancexyz.xyz/api"
            : "http://localhost:8080/api",
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
