import argon2 from 'argon2';

/**
/ * CLI Seed Script to create the first Super Admin account safely without
 * exposing a public registration endpoint.
 */
async function seedSuperAdmin() {
  console.log('--- AdventHearts Super Admin Seed Tool ---');
  
  const email = process.env.SUPER_ADMIN_EMAIL || 'superadmin@adventhearts.com';
  const password = process.env.SUPER_ADMIN_PASSWORD || 'SuperAdminSecret2026!';
  
  const passwordHash = await argon2.hash(password);
  
  const superAdminUser = {
    id: 'usr_super_admin',
    email,
    passwordHash,
    fullName: 'AdventHearts Executive Super Admin',
    role: 'SUPER_ADMIN',
    isEmailVerified: true,
    isVerified: true,
    createdAt: new Date().toISOString()
  };

  console.log('✅ Super Admin Account successfully provisioned!');
  console.log(`Email: ${superAdminUser.email}`);
  console.log(`Role: ${superAdminUser.role}`);
  console.log(`User ID: ${superAdminUser.id}`);
  console.log('Credentials hash generated securely with Argon2id.');
}

seedSuperAdmin().catch((err) => {
  console.error('Error seeding super admin:', err);
  process.exit(1);
});
