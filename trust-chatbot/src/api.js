import axios from "axios";

export async function sendMessage(sessionId, message) {
    try {
        const res = await axios.post("/chatbot", {
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
