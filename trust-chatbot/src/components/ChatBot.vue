<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3>Trust Insurance Assistant</h3>
      <p class="tagline">Secure your future</p>
    </div>

    <div class="chat-box" ref="chatBox">

      <div v-for="(msg, index) in messages" :key="index"
           :class="['chat-message', msg.sender]">

        <div class="bubble">{{ msg.text }}</div>

      </div>

      <div v-if="loading" class="loading">
        <div class="dot"></div>
        <div class="dot"></div>
        <div class="dot"></div>
      </div>

    </div>

    <div class="chat-input">
      <input
          v-model="userInput"
          @keyup.enter="send"
          placeholder="Type your message..."
      />

      <button @click="send">Send</button>
    </div>
  </div>
</template>


<script>
//import { sendMessage, confirmSend } from "../api";
import * as api from "../api";
console.log("API MODULE:", api);


export default {
  data() {
    return {
      sessionId: localStorage.getItem("trust_session_id") || this.generateId(),
      userInput: "",
      messages: [],
      loading: false,
    };
  },

  mounted() {
    this.saveSession();
  },

  methods: {
    generateId() {
      return "session-" + Math.random().toString(36).substring(2, 12);
    },

    saveSession() {
      localStorage.setItem("trust_session_id", this.sessionId);
    },

    async send() {
      const text = this.userInput.trim();
      if (!text) return;

      // Show user message
      this.messages.push({ sender: "user", text });
      this.userInput = "";
      this.scrollToBottom();

      this.loading = true;

      try {
        let response;

        // Detect confirm keywords
        if (/^(yes|send|confirm)$/i.test(text)) {
          response = await api.confirmSend(this.sessionId);
        } else {
          response = await api.sendMessage(this.sessionId, text);
        }

        console.log("Raw chatbot response:", response);

        // ✅ EXPECT NEW NORMALIZED FORMAT
        // { sessionId, reply, shouldEndSession }
        if (!response || !response.reply) {
          this.messages.push({
            sender: "bot",
            text: "❗ I didn't receive any reply from the server."
          });
        } else {
          this.messages.push({
            sender: "bot",
            text: response.reply
          });

          // keep session in sync
          if (response.sessionId) {
            this.sessionId = response.sessionId;
            this.saveSession();
          }
        }

      } catch (err) {
        console.error("Chat error:", err);

        this.messages.push({
          sender: "bot",
          text: "❗ Something went wrong. Please try again."
        });
      }

      this.loading = false;
      this.scrollToBottom();
    }
    ,

    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.chatBox;
        if (box) {
          box.scrollTop = box.scrollHeight;
        }
      });
    }
  }
};

</script>



<style scoped>
.chat-container {
  width: 380px;
  height: 600px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  overflow: hidden;
  font-family: "Inter", sans-serif;
}

/* HEADER */
.chat-header {
  background: #0a5cff;
  color: white;
  padding: 18px;
  text-align: center;
}
.chat-header h3 {
  margin: 0;
}
.tagline {
  font-size: 12px;
  opacity: 0.8;
}

/* CHAT BOX */
.chat-box {
  flex: 1;
  padding: 15px;
  overflow-y: auto;
  background: #f7f9fc;
}

/* MESSAGES */
.chat-message {
  display: flex;
  margin-bottom: 12px;
}

.chat-message.user {
  justify-content: flex-end;
}

.chat-message.bot {
  justify-content: flex-start;
}

.bubble {
  max-width: 70%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 14px;
  line-height: 1.4;
}

.user .bubble {
  background: #0a5cff;
  color: white;
  border-bottom-right-radius: 4px;
}

.bot .bubble {
  background: #e8e8e8;
  color: #000;
  border-bottom-left-radius: 4px;
}

/* LOADING DOTS */
.loading {
  display: flex;
  gap: 6px;
  margin-left: 10px;
}
.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #999;
  animation: bounce 1.2s infinite;
}
.dot:nth-child(2) { animation-delay: 0.2s; }
.dot:nth-child(3) { animation-delay: 0.4s; }

@keyframes bounce {
  0%, 80%, 100% { transform: scale(0.5); opacity: 0.6; }
  40% { transform: scale(1.1); opacity: 1; }
}

/* INPUT BAR */
.chat-input {
  display: flex;
  padding: 12px;
  gap: 10px;
  border-top: 1px solid #ddd;
}
.chat-input input {
  flex: 1;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccc;
}
.chat-input button {
  background: #0a5cff;
  color: white;
  padding: 10px 16px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}
</style>
