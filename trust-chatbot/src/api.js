import axios from "axios";

export async function sendMessage(sessionId, message) {
    try {
        const res = await axios.post("/api/chatbot", {
            sessionId,
            message
        });

        return res.data;

    } catch (err) {
        console.error("Chatbot API error:", err);
        return {
            messages: ["⚠️ Backend not reachable."],
            endSession: false
        };
    }
}
