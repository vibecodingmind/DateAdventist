process.env.NODE_ENV = 'test';
process.env.DATABASE_URL = process.env.DATABASE_URL || 'file:./test.db';
process.env.JWT_SECRET = 'test_jwt_secret_adventhearts_32chars';
process.env.JWT_REFRESH_SECRET = 'test_refresh_secret_adventhearts_32';
process.env.JWT_EXPIRES_IN = '1h';
process.env.ARGON_MEMORY_COST = '4096';
process.env.ARGON_TIME_COST = '2';
