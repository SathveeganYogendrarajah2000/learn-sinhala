/**
 * Production environment configuration.
 */
export const environment = {
  production: true,
  apiUrl: '/api',  // Relative URL for production (same origin)

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
