/**
 * Production environment configuration.
 */
export const environment = {
  production: true,
  // Absolute URL for Cloud Run deployment
  // Update this to your backend Cloud Run service URL
  apiUrl: 'https://YOUR-BACKEND-SERVICE.run.app/api',

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
