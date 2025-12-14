import axios from "axios";

const API_URL =
    "https://k72m5zhm28.execute-api.us-east-1.amazonaws.com/chatbot";

/**
 * Send a normal chat message
 */
export async function sendMessage(sessionId, message) {
    try {
        const res = await axios.post(
            API_URL,
            {
                sessionId: sessionId,
                message: message
            },
            {
                headers: {
                    "Content-Type": "application/json"
                }
            }
        );

        // ✅ NORMALIZED RESPONSE
        return {
            sessionId: res.data.sessionId,
            reply: res.data.reply,
            shouldEndSession: res.data.shouldEndSession || false
        };

    } catch (error) {
        console.error("Chatbot API error:", error);

        return {
            error: true,
            reply: "I didn't receive any reply from the server."
        };
    }
}

/**
 * Confirm sending quotation (optional future use)
 */
export async function confirmSend(sessionId) {
    try {
        const res = await axios.post(
            API_URL,
            {
                sessionId: sessionId,
                message: "confirm"
            },
            {
                headers: {
                    "Content-Type": "application/json"
                }
            }
        );

        // ✅ SAME NORMALIZED RESPONSE
        return {
            sessionId: res.data.sessionId,
            reply: res.data.reply,
            shouldEndSession: res.data.shouldEndSession || false
        };

    } catch (error) {
        console.error("Confirm send error:", error);

        return {
            error: true,
            reply: "Unable to confirm quotation at the moment."
        };
    }
}
