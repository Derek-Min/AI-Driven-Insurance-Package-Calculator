<template>
  <div class="chat-container">
    <div class="chat-header">
      <h3>Trust Insurance Assistant</h3>
      <p class="tagline">Secure your future</p>
    </div>

    <div class="chat-box" ref="chatBox">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['chat-message', msg.sender]"
      >
        <div class="bubble">{{ msg.text }}</div>
      </div>

      <div v-if="loading" class="loading">
        <span>.</span><span>.</span><span>.</span>
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
import * as api from "../api";

export default {
  name: "ChatBot",

  data() {
    return {
      sessionId: localStorage.getItem("trust_session_id"),
      userInput: "",
      messages: [],
      loading: false
    };
  },

  mounted() {
    // Initial greeting (optional but recommended)
    if (this.messages.length === 0) {
      this.messages.push({
        sender: "bot",
        text: "👋 Welcome! Do you want Motor or Life insurance?"
      });
    }
  },

  methods: {
    async send() {
      const text = this.userInput.trim();
      if (!text) return;

      this.messages.push({ sender: "user", text });
      this.userInput = "";
      this.loading = true;
      this.scrollToBottom();

      try {
        const response = await api.sendMessage(this.sessionId, text);

        if (response.sessionId) {
          this.sessionId = response.sessionId;
          localStorage.setItem("trust_session_id", this.sessionId);
        }

        this.messages.push({
          sender: "bot",
          text: response.reply || "⚠️ No response from server."
        });

      } catch (e) {
        console.error(e);
        this.messages.push({
          sender: "bot",
          text: "❗ Server error. Please try again."
        });
      }

      this.loading = false;
      this.scrollToBottom();
    },

    scrollToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.chatBox;
        if (box) box.scrollTop = box.scrollHeight;
      });
    }
  }
};
</script>

<style scoped>
.chat-container {
  width: 360px;
  height: 540px;
  border-radius: 14px;
  display: flex;
  flex-direction: column;
  background: #ffffff;
  border: 1px solid #e0e0e0;
  font-family: Arial, sans-serif;
}

.chat-header {
  background: #0a5cff;
  color: #ffffff;
  padding: 16px;
  text-align: center;
}

.tagline {
  font-size: 12px;
  opacity: 0.85;
}

.chat-box {
  flex: 1;
  padding: 12px;
  overflow-y: auto;
  background: #f7f9fc;
}

.chat-message {
  display: flex;
  margin-bottom: 10px;
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

.chat-input {
  display: flex;
  padding: 10px;
  gap: 8px;
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
  padding: 10px 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
}

.loading {
  font-size: 20px;
  color: #999;
}
</style>
