module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  testTimeout: 60000,
  roots: ['<rootDir>/src'],
  setupFiles: ['<rootDir>/src/test/setup-env.ts'],
};
