function fn() {
  // ============================================================================
  // KARATE CONFIGURATION FILE (karate-config.js)
  // ============================================================================
  //
  // This file is automatically loaded by Karate before running tests.
  // It provides global configuration and variables for all .feature files.
  //
  // ============================================================================

  var config = {
    // Base URL for the API - can be overridden by environment
    baseUrl: 'http://localhost:8080'
  };

  // Get environment from system property (e.g., -Dkarate.env=dev)
  var env = karate.env;

  if (!env) {
    env = 'local';  // Default environment
  }

  karate.log('Running tests in environment:', env);

  // Environment-specific configuration
  if (env === 'local') {
    config.baseUrl = 'http://localhost:8080';
  } else if (env === 'dev') {
    config.baseUrl = 'http://dev-server:8080';
  } else if (env === 'staging') {
    config.baseUrl = 'http://staging-server:8080';
  }

  // Set connection timeout
  karate.configure('connectTimeout', 5000);
  karate.configure('readTimeout', 5000);

  return config;
}

