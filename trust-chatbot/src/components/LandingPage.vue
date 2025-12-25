<template>
  <div class="landing">

    <!-- NAVBAR -->
    <header class="nav">
      <div class="logo">
        <!-- Trust Insurance SVG -->
        <svg width="160" height="48" viewBox="0 0 260 64"
             xmlns="http://www.w3.org/2000/svg">
          <defs>
            <linearGradient id="trustGrad" x1="0%" y1="0%" x2="100%" y2="100%">
              <stop offset="0%" stop-color="#0A5CFF"/>
              <stop offset="100%" stop-color="#00A6C8"/>
            </linearGradient>
          </defs>

          <path d="M32 6 L52 14 V30
             C52 42 42 52 32 56
             C22 52 12 42 12 30
             V14 Z"
                fill="url(#trustGrad)"/>

          <path d="M24 30 L30 36 L40 24"
                fill="none"
                stroke="#FFFFFF"
                stroke-width="3.5"
                stroke-linecap="round"
                stroke-linejoin="round"/>

          <text x="70" y="34"
                font-family="Arial"
                font-size="22"
                font-weight="700"
                fill="#0B1B3A">
            Trust Insurance
          </text>

          <text x="70" y="52"
                font-family="Arial"
                font-size="11"
                letter-spacing="1.4"
                fill="#0A5CFF">
            SECURE YOUR FUTURE
          </text>
        </svg>
      </div>

      <h1 class="brand">Trust Insurance</h1>
    </header>

    <!-- HERO SECTION -->
    <section class="hero">
      <div class="hero-content">
        <h2>Secure Your Future</h2>
        <p>Your trusted partner for Motor & Life protection.</p>

        <button class="cta" @click="$emit('openChat')">
          Talk to TrustBot
        </button>
      </div>
    </section>

    <!-- PRODUCTS SECTION -->
    <section class="products">
      <h3>Our Products</h3>

      <div class="cards">
        <div class="card">
          <h4>Comprehensive Motor Insurance</h4>
          <p>Protect your vehicle with affordable coverage, NCD benefits, and options for theft, flood, and more.</p>
        </div>

        <div class="card">
          <h4>ShortTerm Life Insurance</h4>
          <p>Coverage that protects your loved ones with flexible premiums and fast approval.</p>
        </div>
      </div>
    </section>

  </div>
</template>


<style scoped>
.landing {
  font-family: "Inter", Arial, sans-serif;
}

/* NAVBAR */
.nav {
  display: flex;
  align-items: center;
  padding: 14px 25px;
  background: white;
  border-bottom: 1px solid #eee;
}
.logo {
  height: 48px;
  margin-right: 14px;
}
.brand {
  font-size: 20px;
  font-weight: 700;
}

/* HERO SECTION */
.hero {
  background: linear-gradient(to bottom right, #164bff, #4ca5ff);
  color: white;
  padding: 80px 25px;
  text-align: center;
}
.hero-content h2 {
  font-size: 42px;
  margin-bottom: 12px;
}
.hero-content p {
  font-size: 18px;
  opacity: 0.9;
}
.cta {
  margin-top: 25px;
  padding: 14px 26px;
  font-size: 16px;
  border: none;
  background: #ffffff;
  color: #164bff;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
}
.cta:hover {
  background: #f3f3f3;
}

/* PRODUCTS SECTION */
.products {
  padding: 40px 25px;
  text-align: center;
}
.cards {
  display: flex;
  gap: 20px;
  justify-content: center;
  margin-top: 25px;
}
.card {
  background: #ffffff;
  padding: 22px;
  width: 300px;
  border-radius: 8px;
  box-shadow: 0 2px 10px rgba(0,0,0,0.1);
}
.card h4 {
  margin-bottom: 8px;
  font-size: 18px;
}
</style>
