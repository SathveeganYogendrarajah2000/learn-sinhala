/**
 * Development environment configuration.
 */
export const environment = {
  production: false,
  apiUrl: 'http://localhost:8080/api',

  // API settings
  api: {
    timeout: 30000,         // 30 seconds
    retryAttempts: 2,       // Retry failed requests
    retryDelay: 1000        // 1 second between retries
  },

  // Feature flags
  features: {
    audioPlayback: true,
    sentenceBuilder: true,
    offlineMode: false
  },

  // Debug settings
  debug: {
    logApiCalls: true,
    logErrors: true
  }
};
