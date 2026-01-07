<template>
  <div class="landing">
    <!-- TOP NAV -->
    <header class="nav">
      <div class="nav-left">
        <img :src="logo" alt="Trust Insurance" class="logo" />
        <div class="brand-block">
          <div class="brand-name">Trust Insurance</div>
          <div class="brand-tag">Secure • Simple • Fast Quotes</div>
        </div>
      </div>

      <nav class="nav-links">
        <a href="#products">Products</a>
        <a href="#why">Why Us</a>
        <a href="#reviews">Reviews</a>
        <a href="#contact">Contact</a>
      </nav>

      <div class="nav-right">
        <button class="btn btn-ghost" @click="$emit('openChat')">Talk to TrustBot</button>
        <button class="btn btn-primary" @click="$emit('openChat')">Get a Quote</button>
      </div>
    </header>

    <!-- HERO -->
    <section class="hero">
      <div class="hero-grid">
        <div class="hero-left">
          <div class="pill">
            <span class="dot"></span>
            AI-assisted Quotation • Motor & Life
          </div>

          <h1>Insurance that feels modern.</h1>
          <p class="hero-sub">
            Get quick, transparent quotes for <b>Motor</b> and <b>Life</b>.
            TrustBot helps you compare options, understand premiums, and move faster.
          </p>

          <div class="hero-cta">
            <button class="btn btn-primary btn-lg" @click="$emit('openChat')">
              Start with TrustBot
            </button>
            <button class="btn btn-ghost btn-lg" @click="scrollTo('products')">
              Explore products
            </button>
          </div>

          <div class="trust-row">
            <div class="trust-badge">
              <div class="trust-icon">🛡️</div>
              <div>
                <div class="trust-title">Secure by design</div>
                <div class="trust-text">Privacy-first data handling</div>
              </div>
            </div>

            <div class="trust-badge">
              <div class="trust-icon">⏱️</div>
              <div>
                <div class="trust-title">Fast quotes</div>
                <div class="trust-text">Fewer steps, less waiting</div>
              </div>
            </div>

            <div class="trust-badge">
              <div class="trust-icon">💬</div>
              <div>
                <div class="trust-title">Clear explanations</div>
                <div class="trust-text">Know why your premium changes</div>
              </div>
            </div>
          </div>
        </div>

        <!-- HERO CARD -->
        <div class="hero-right">
          <div class="quote-card">
            <div class="card-top">
              <div>
                <div class="card-kicker">Instant Quotation</div>
                <div class="card-title">Try a quick start</div>
              </div>
              <div class="chip">Live</div>
            </div>

            <div class="form">
              <label>
                Full Name
                <input v-model="demo.name" placeholder="e.g., Zay Yar Naing" />
              </label>

              <label>
                Product
                <select v-model="demo.product">
                  <option value="Motor">Motor Insurance</option>
                  <option value="Life">Life Insurance</option>
                </select>
              </label>

              <label v-if="demo.product === 'Motor'">
                Vehicle Type
                <select v-model="demo.vehicle">
                  <option>Sedan</option>
                  <option>SUV</option>
                  <option>Hatchback</option>
                  <option>Pickup</option>
                </select>
              </label>

              <label v-else>
                Age
                <input v-model.number="demo.age" type="number" min="18" max="80" placeholder="e.g., 30" />
              </label>

              <button class="btn btn-primary btn-block" @click="$emit('openChat')">
                Continue in TrustBot
              </button>

              <p class="note">
                No payment required. You can request a PDF quotation after chatting.
              </p>
            </div>
          </div>

          <div class="mini-stats">
            <div class="stat">
              <div class="stat-num">24/7</div>
              <div class="stat-label">Availability</div>
            </div>
            <div class="stat">
              <div class="stat-num">&lt; 1 min</div>
              <div class="stat-label">Start quote</div>
            </div>
            <div class="stat">
              <div class="stat-num">2</div>
              <div class="stat-label">Products</div>
            </div>
          </div>
        </div>
      </div>
    </section>

    <!-- PRODUCTS -->
    <section id="products" class="section">
      <div class="section-head">
        <h2>Our Products</h2>
        <p>Choose coverage that matches your needs — with transparent pricing guidance.</p>
      </div>

      <div class="product-grid">
        <div class="product-card">
          <div class="pc-top">
            <div class="pc-icon">🚗</div>
            <div>
              <h3>Comprehensive Motor Insurance</h3>
              <p class="muted">Vehicle protection with add-ons you can understand.</p>
            </div>
          </div>

          <ul class="list">
            <li>Own damage & third-party coverage</li>
            <li>NCD guidance and premium breakdown</li>
            <li>Optional add-ons: flood, theft, windshield</li>
          </ul>

          <div class="pc-actions">
            <button class="btn btn-primary" @click="$emit('openChat')">Get Motor Quote</button>
            <button class="btn btn-ghost" @click="$emit('openChat')">Ask TrustBot</button>
          </div>
        </div>

        <div class="product-card">
          <div class="pc-top">
            <div class="pc-icon">❤️</div>
            <div>
              <h3>Short-Term Life Insurance</h3>
              <p class="muted">Flexible protection for your loved ones, made simple.</p>
            </div>
          </div>

          <ul class="list">
            <li>Fast eligibility checks and clear explanations</li>
            <li>Flexible coverage choices</li>
            <li>Premium transparency: what impacts your rate</li>
          </ul>

          <div class="pc-actions">
            <button class="btn btn-primary" @click="$emit('openChat')">Get Life Quote</button>
            <button class="btn btn-ghost" @click="$emit('openChat')">Ask TrustBot</button>
          </div>
        </div>
      </div>
    </section>

    <!-- WHY US -->
    <section id="why" class="section section-alt">
      <div class="section-head">
        <h2>Why Trust Insurance</h2>
        <p>Built for speed, clarity, and a professional insurance experience.</p>
      </div>

      <div class="feature-grid">
        <div class="feature">
          <div class="f-icon">🔎</div>
          <h4>Transparent breakdown</h4>
          <p>See key factors that affect pricing (age, claims, coverage, add-ons).</p>
        </div>
        <div class="feature">
          <div class="f-icon">🧠</div>
          <h4>AI-guided flow</h4>
          <p>TrustBot asks only what’s needed and guides you step-by-step.</p>
        </div>
        <div class="feature">
          <div class="f-icon">📄</div>
          <h4>Professional PDF output</h4>
          <p>Generate quotation PDFs suitable for email and documentation.</p>
        </div>
        <div class="feature">
          <div class="f-icon">🔐</div>
          <h4>Secure handling</h4>
          <p>Designed with privacy and secure communications in mind.</p>
        </div>
      </div>

      <div class="cta-strip">
        <div>
          <div class="cta-title">Ready to get your quote?</div>
          <div class="cta-sub">Start a chat and finish in minutes.</div>
        </div>
        <button class="btn btn-primary btn-lg" @click="$emit('openChat')">Chat with TrustBot</button>
      </div>
    </section>

    <!-- REVIEWS -->
    <section id="reviews" class="section">
      <div class="section-head">
        <h2>What users like</h2>
        <p>Clear steps. Faster quotes. Less confusion.</p>
      </div>

      <div class="review-grid">
        <div class="review">
          <div class="stars">★★★★★</div>
          <p>“The premium explanation made sense. I understood what increased the price.”</p>
          <div class="who">— Customer</div>
        </div>
        <div class="review">
          <div class="stars">★★★★★</div>
          <p>“Quick and clean. I got the quote without calling anyone.”</p>
          <div class="who">— Customer</div>
        </div>
        <div class="review">
          <div class="stars">★★★★★</div>
          <p>“It feels like a real insurance website — not a basic form.”</p>
          <div class="who">— Customer</div>
        </div>
      </div>
    </section>

    <!-- FOOTER -->
    <footer id="contact" class="footer">
      <div class="footer-inner">
        <div class="foot-left">
          <img :src="logo" alt="Trust Insurance" class="foot-logo" />
          <div class="foot-title">Trust Insurance</div>
          <div class="foot-text">Motor & Life • AI-driven quotation experience</div>
        </div>

        <div class="foot-links">
          <a href="#products">Products</a>
          <a href="#why">Why Us</a>
          <a href="#reviews">Reviews</a>
        </div>

        <div class="foot-cta">
          <button class="btn btn-primary" @click="$emit('openChat')">Get Quote</button>
          <div class="foot-small">© {{ new Date().getFullYear() }} Trust Insurance</div>
        </div>
      </div>
    </footer>
  </div>
</template>

<script>
import logo from "../assets/logo.png";

export default {
  name: "LandingPage",
  data() {
    return {
      logo,
      demo: {
        name: "",
        product: "Motor",
        vehicle: "Sedan",
        age: 30
      }
    };
  },
  methods: {
    scrollTo(id) {
      const el = document.getElementById(id);
      if (el) el.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  }
};
</script>

<style scoped>
/* Base */
.landing {
  font-family: "Inter", Arial, sans-serif;
  color: #0b1b3a;
  background: #ffffff;
}
a { color: inherit; text-decoration: none; }
.muted { color: #5a6b88; }
.section {
  padding: 70px 22px;
  max-width: 1140px;
  margin: 0 auto;
}
.section-alt {
  background: #f6f9ff;
  max-width: 100%;
}
.section-alt > * {
  max-width: 1140px;
  margin-left: auto;
  margin-right: auto;
}
.section-head {
  text-align: center;
  margin-bottom: 28px;
}
.section-head h2 {
  font-size: 34px;
  margin: 0 0 8px;
}
.section-head p {
  margin: 0;
  color: #5a6b88;
  font-size: 16px;
}

/* Buttons */
.btn {
  border: 1px solid transparent;
  padding: 10px 14px;
  border-radius: 10px;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.08s ease, background 0.2s ease, border-color 0.2s ease;
}
.btn:active { transform: scale(0.98); }
.btn-primary {
  background: linear-gradient(135deg, #0a5cff, #00a6c8);
  color: white;
}
.btn-ghost {
  background: transparent;
  border-color: rgba(11, 27, 58, 0.15);
  color: #0b1b3a;
}
.btn-lg { padding: 12px 18px; border-radius: 12px; }
.btn-block { width: 100%; padding: 12px 14px; border-radius: 12px; }

/* Navbar */
.nav {
  position: sticky;
  top: 0;
  z-index: 20;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
  padding: 14px 20px;
  background: rgba(255,255,255,0.86);
  backdrop-filter: blur(10px);
  border-bottom: 1px solid rgba(10, 92, 255, 0.08);
}
.nav-left {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 240px;
}
.logo {
  height: 44px;
  width: auto;
}
.brand-block { line-height: 1.1; }
.brand-name { font-size: 16px; font-weight: 800; }
.brand-tag { font-size: 12px; color: #5a6b88; margin-top: 2px; }

.nav-links {
  display: flex;
  gap: 18px;
  color: #1f2a44;
  font-weight: 600;
  font-size: 14px;
}
.nav-links a {
  opacity: 0.9;
  padding: 8px 10px;
  border-radius: 10px;
}
.nav-links a:hover {
  background: rgba(10, 92, 255, 0.06);
}

.nav-right {
  display: flex;
  gap: 10px;
}

/* Hero */
.hero {
  padding: 60px 22px 30px;
  background:
      radial-gradient(800px 400px at 20% 10%, rgba(10,92,255,0.18), transparent 60%),
      radial-gradient(700px 380px at 80% 30%, rgba(0,166,200,0.18), transparent 60%),
      linear-gradient(180deg, #ffffff 0%, #f6f9ff 100%);
}
.hero-grid {
  max-width: 1140px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr 0.8fr;
  gap: 26px;
  align-items: start;
}
.pill {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 999px;
  background: rgba(10, 92, 255, 0.08);
  border: 1px solid rgba(10, 92, 255, 0.14);
  font-weight: 700;
  color: #123a94;
  font-size: 13px;
}
.dot {
  width: 10px; height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #0a5cff, #00a6c8);
}
.hero-left h1 {
  font-size: 48px;
  line-height: 1.05;
  margin: 16px 0 12px;
}
.hero-sub {
  font-size: 18px;
  color: #34455f;
  margin: 0 0 18px;
  max-width: 560px;
}
.hero-cta {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin: 18px 0 22px;
}

.trust-row {
  display: grid;
  grid-template-columns: 1fr;
  gap: 10px;
  max-width: 560px;
}
.trust-badge {
  display: flex;
  gap: 12px;
  align-items: center;
  padding: 12px 14px;
  border-radius: 14px;
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(11, 27, 58, 0.08);
}
.trust-icon {
  width: 40px;
  height: 40px;
  display: grid;
  place-items: center;
  border-radius: 12px;
  background: rgba(10, 92, 255, 0.08);
}
.trust-title { font-weight: 800; }
.trust-text { color: #5a6b88; font-size: 13px; margin-top: 2px; }

/* Quote card */
.quote-card {
  background: rgba(255,255,255,0.9);
  border: 1px solid rgba(11, 27, 58, 0.10);
  border-radius: 18px;
  box-shadow: 0 14px 35px rgba(10, 22, 50, 0.08);
  padding: 16px;
}
.card-top {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 10px;
  margin-bottom: 12px;
}
.card-kicker { color: #0a5cff; font-weight: 900; font-size: 12px; letter-spacing: 0.4px; }
.card-title { font-size: 18px; font-weight: 900; margin-top: 2px; }
.chip {
  font-size: 12px;
  font-weight: 800;
  padding: 6px 10px;
  border-radius: 999px;
  color: #0b1b3a;
  background: rgba(0, 166, 200, 0.12);
  border: 1px solid rgba(0, 166, 200, 0.18);
}
.form label {
  display: grid;
  gap: 6px;
  font-size: 13px;
  font-weight: 800;
  color: #1f2a44;
  margin-bottom: 10px;
}
.form input, .form select {
  border: 1px solid rgba(11, 27, 58, 0.14);
  border-radius: 12px;
  padding: 11px 12px;
  font-size: 14px;
  outline: none;
  background: white;
}
.form input:focus, .form select:focus {
  border-color: rgba(10, 92, 255, 0.45);
  box-shadow: 0 0 0 4px rgba(10, 92, 255, 0.10);
}
.note {
  margin: 10px 0 0;
  color: #5a6b88;
  font-size: 12px;
  line-height: 1.4;
}
.mini-stats {
  margin-top: 12px;
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 10px;
}
.stat {
  background: rgba(255,255,255,0.7);
  border: 1px solid rgba(11, 27, 58, 0.08);
  border-radius: 14px;
  padding: 12px;
  text-align: center;
}
.stat-num { font-weight: 950; font-size: 18px; }
.stat-label { color: #5a6b88; font-size: 12px; margin-top: 2px; }

/* Products */
.product-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 18px;
  margin-top: 10px;
}
.product-card {
  background: white;
  border: 1px solid rgba(11, 27, 58, 0.10);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 10px 24px rgba(10, 22, 50, 0.05);
}
.pc-top {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}
.pc-icon {
  width: 44px; height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: rgba(10, 92, 255, 0.08);
  font-size: 20px;
}
.product-card h3 { margin: 0; font-size: 18px; }
.list {
  margin: 14px 0 0;
  padding-left: 18px;
  color: #34455f;
}
.list li { margin: 8px 0; }
.pc-actions {
  margin-top: 16px;
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

/* Features */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 14px;
  margin-top: 18px;
  padding: 0 22px 30px;
  max-width: 1140px;
}
.feature {
  background: rgba(255,255,255,0.8);
  border: 1px solid rgba(11, 27, 58, 0.10);
  border-radius: 18px;
  padding: 16px;
}
.f-icon {
  width: 44px; height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: rgba(0, 166, 200, 0.12);
  margin-bottom: 10px;
  font-size: 18px;
}
.feature h4 { margin: 0 0 6px; font-size: 15px; }
.feature p { margin: 0; color: #5a6b88; font-size: 13px; line-height: 1.45; }

.cta-strip {
  max-width: 1140px;
  margin: 0 auto 40px;
  background: linear-gradient(135deg, rgba(10,92,255,0.12), rgba(0,166,200,0.12));
  border: 1px solid rgba(11, 27, 58, 0.10);
  border-radius: 20px;
  padding: 18px 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 14px;
}
.cta-title { font-size: 18px; font-weight: 950; }
.cta-sub { color: #5a6b88; margin-top: 3px; }

/* Reviews */
.review-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
  margin-top: 14px;
}
.review {
  background: white;
  border: 1px solid rgba(11, 27, 58, 0.10);
  border-radius: 18px;
  padding: 16px;
}
.stars { font-weight: 950; letter-spacing: 1px; }
.review p { margin: 10px 0 12px; color: #34455f; }
.who { color: #5a6b88; font-size: 13px; }

/* Footer */
.footer {
  border-top: 1px solid rgba(11, 27, 58, 0.10);
  padding: 28px 22px;
  background: #ffffff;
}
.footer-inner {
  max-width: 1140px;
  margin: 0 auto;
  display: grid;
  grid-template-columns: 1.2fr 0.6fr 0.7fr;
  gap: 16px;
  align-items: center;
}
.foot-logo { height: 38px; width: auto; }
.foot-title { font-weight: 950; margin-top: 6px; }
.foot-text { color: #5a6b88; font-size: 13px; margin-top: 4px; }
.foot-links {
  display: flex;
  gap: 14px;
  justify-content: center;
  font-weight: 700;
  color: #1f2a44;
}
.foot-cta { justify-self: end; text-align: right; }
.foot-small { margin-top: 8px; color: #5a6b88; font-size: 12px; }

/* Responsive */
@media (max-width: 980px) {
  .nav-links { display: none; }
  .hero-grid { grid-template-columns: 1fr; }
  .hero-left h1 { font-size: 40px; }
  .product-grid { grid-template-columns: 1fr; }
  .feature-grid { grid-template-columns: repeat(2, minmax(0, 1fr)); }
  .review-grid { grid-template-columns: 1fr; }
  .footer-inner { grid-template-columns: 1fr; text-align: center; }
  .foot-cta { justify-self: center; text-align: center; }
  .foot-links { justify-content: center; flex-wrap: wrap; }
}
</style>
