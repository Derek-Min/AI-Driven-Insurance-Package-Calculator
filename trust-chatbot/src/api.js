import axios from "axios";

const api = axios.create({
    baseURL: "http://api.trust-insurancexyz.xyz:8080",
    timeout: 10000
});

export async function sendMessage(sessionId, message) {
    try {
        const res = await api.post("/chatbot", {
            sessionId,
            message
        });

        let data = res.data;

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
