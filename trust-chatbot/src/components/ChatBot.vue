<template>
  <div class="chat-wrapper">
    <!-- HEADER -->
    <div class="chat-header">
      <div class="brand">
        <img src="/logo.png" alt="Trust Insurance" class="logo" />
        <div>
          <h3>Trust Insurance Assistant</h3>
          <p>Secure your future</p>
        </div>
      </div>
      <button class="exit" @click="$emit('exitChat')">✕</button>
    </div>

    <!-- BODY -->
    <div class="chat-body" ref="chatBox">
      <div
          v-for="(msg, index) in messages"
          :key="index"
          :class="['msg', msg.sender]"
      >
        <div class="bubble">{{ msg.text }}</div>
      </div>

      <div v-if="loading" class="typing">
        TrustBot is typing<span>.</span><span>.</span><span>.</span>
      </div>
    </div>

    <!-- INPUT -->
    <div class="chat-input">
      <input
          v-model="userInput"
          placeholder="Type your message..."
          @keyup.enter="send"
          :disabled="loading || ended"
      />
      <button @click="send" :disabled="loading || ended">Send</button>
      <button class="restart" @click="restart" :disabled="loading">
        Restart
      </button>
    </div>
  </div>
</template>

<script>
import * as api from "../api";

export default {
  name: "ChatBot",

  data() {
    return {
      sessionId: localStorage.getItem("trust_session_id") || null,
      userInput: "",
      messages: [
        {
          sender: "bot",
          text: "👋 Welcome! Do you want Motor or Life insurance?"
        }
      ],
      loading: false,
      ended: false
    };
  },

  methods: {
    async send() {
      const text = this.userInput.trim();
      if (!text || this.loading || this.ended) return;

      this.messages.push({ sender: "user", text });
      this.userInput = "";
      this.loading = true;
      this.scrollChatToBottom();

      try {
        const response = await api.sendMessage(this.sessionId, text);

        if (response?.sessionId) {
          this.sessionId = response.sessionId;
          localStorage.setItem("trust_session_id", this.sessionId);
        }

        response?.messages?.forEach(m =>
            this.messages.push({ sender: "bot", text: m })
        );

        if (response?.endSession) {
          this.ended = true;
          this.messages.push({
            sender: "bot",
            text: "✅ Session completed. Click Restart to start again."
          });
        }

      } catch {
        this.messages.push({
          sender: "bot",
          text: "❗ Something went wrong. Please try again."
        });
      } finally {
        this.loading = false;
        this.scrollChatToBottom();
      }
    },

    async restart() {
      try {
        await api.sendMessage(this.sessionId, "restart");
      } catch (err) {
        console.warn("Restart request failed:", err);
      }

      localStorage.removeItem("trust_session_id");
      this.sessionId = null;
      this.ended = false;
      this.messages = [
        {
          sender: "bot",
          text: "👋 Welcome! Do you want Motor or Life insurance?"
        }
      ];
    },

    scrollChatToBottom() {
      this.$nextTick(() => {
        const box = this.$refs.chatBox;
        if (box) box.scrollTop = box.scrollHeight;
      });
    }
  }
};
</script>

<style scoped>
.chat-wrapper {
  max-width: 420px;
  margin: 40px auto;
  background: #ffffff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.12);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  font-family: "Inter", Arial, sans-serif;
}

/* HEADER */
.chat-header {
  background: linear-gradient(90deg, #0a5cff, #00a6c8);
  color: #ffffff;
  padding: 14px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
}
.logo {
  height: 38px;
}
.exit {
  background: transparent;
  border: none;
  color: white;
  font-size: 18px;
  cursor: pointer;
}

/* BODY */
.chat-body {
  flex: 1;
  padding: 14px;
  background: #f7f9fc;
  overflow-y: auto;
}
.msg {
  margin-bottom: 10px;
  display: flex;
}
.msg.user {
  justify-content: flex-end;
}
.msg.bot {
  justify-content: flex-start;
}
.bubble {
  max-width: 75%;
  padding: 10px 14px;
  border-radius: 16px;
  font-size: 13px;
  line-height: 1.45;
}
.msg.user .bubble {
  background: #0a5cff;
  color: white;
  border-bottom-right-radius: 4px;
}
.msg.bot .bubble {
  background: #e9edf5;
  color: #0b1b3a;
  border-bottom-left-radius: 4px;
}
.typing {
  font-size: 12px;
  color: #6b7a90;
}

/* INPUT */
.chat-input {
  padding: 10px;
  border-top: 1px solid #e0e6ef;
  display: flex;
  gap: 6px;
}
.chat-input input {
  flex: 1;
  padding: 10px;
  border-radius: 6px;
  border: 1px solid #ccd6e0;
}
.chat-input button {
  padding: 10px 12px;
  border-radius: 6px;
  border: none;
  background: #0a5cff;
  color: white;
  cursor: pointer;
}
.restart {
  background: #6c757d;
}
</style>
