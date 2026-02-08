/**
 * Production environment configuration.
 */
export const environment = {
  production: true,
  // Absolute URL for separate deployment (Cloudflare Pages + Google Cloud Run)
  // NOTE: CORS must be configured on backend to allow Cloudflare domain
  apiUrl: 'https://learn-sinhala-backend-809391430909.asia-south1.run.app/api',

  // API settings
  api: {
    timeout: 30000,
    retryAttempts: 3,
    retryDelay: 2000
  },

  // Feature flags
  features: {
    audioPlayback: true,
    sentenceBuilder: true,
    offlineMode: false
  },

  // Debug settings
  debug: {
    logApiCalls: false,
    logErrors: true
  }
};
