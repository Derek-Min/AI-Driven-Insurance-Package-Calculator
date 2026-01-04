<template>
  <div>
    <LandingPage v-if="!showChat" @openChat="showChat = true" />

    <div v-if="showChat" class="chat-screen">
      <ChatBot @exitChat="showChat = false" />
    </div>
  </div>
</template>

<script>
import LandingPage from "./components/LandingPage.vue";
import ChatBot from "./components/ChatBot.vue";

export default {
  components: { LandingPage, ChatBot },
  data() {
    return {
      showChat: false
    };
  }
};
</script>

<style>
.chat-screen {
  max-width: 420px;
  margin: 40px auto;
}
</style>
