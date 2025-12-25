import axios from "axios";

// Base backend URL only
const API_BASE = process.env.VUE_APP_API_URL || "http://localhost:8080";

export async function sendMessage(sessionId, message) {
    try {
        const res = await axios.post(`${API_BASE}/chatbot`, {
            sessionId,
            message
        });

        // API Gateway style (Lambda proxy)
        if (res.data?.body) {
            const parsed = typeof res.data.body === "string"
                ? JSON.parse(res.data.body)
                : res.data.body;

            return {
                sessionId: parsed.sessionId,
                reply: parsed.reply,
                shouldEndSession: parsed.shouldEndSession || false
            };
        }

        // Direct Spring Boot response
        return {
            sessionId: res.data.sessionId,
            reply: res.data.reply,
            shouldEndSession: res.data.shouldEndSession || false
        };

    } catch (err) {
        console.error("API ERROR:", err);
        return {
            reply: "⚠️ Backend not reachable."
        };
    }
}
