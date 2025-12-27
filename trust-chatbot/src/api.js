import axios from "axios";

const API_BASE = process.env.VUE_APP_API_URL || "http://localhost:8080";

export async function sendMessage(sessionId, message) {
    try {
        const res = await axios.post(`${API_BASE}/chatbot`, { sessionId, message });

        let data = res.data;
        if (data?.body) {
            data = typeof data.body === "string" ? JSON.parse(data.body) : data.body;
        }

        return {
            sessionId: data.sessionId,
            messages: Array.isArray(data.messages) ? data.messages : [],
            endSession: Boolean(data.endSession)
        };

    } catch {
        return {
            messages: ["⚠️ Backend not reachable."],
            endSession: false
        };
    }
}

