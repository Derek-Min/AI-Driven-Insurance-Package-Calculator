import axios from "axios";

/**
 * Axios instance for backend API
 * Uses Cloudflare Pages environment variable
 */
const api = axios.create({
    baseURL: process.env.VUE_APP_API_BASE_URL,
    timeout: 10000
});

export default api;

/**
 * Send message to chatbot backend
 */
export async function sendMessage(sessionId, message) {
    try {
        const res = await api.post("/chatbot", {
            sessionId,
            message
        });

        let data = res.data;

        // Lambda / API Gateway compatibility
        if (data?.body) {
            data = typeof data.body === "string"
                ? JSON.parse(data.body)
                : data.body;
        }

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
