import { Router } from 'express';
import { AuthController } from '../auth/auth.controller';

const router = Router();

router.post('/register', AuthController.register);
router.post('/login', AuthController.login);
router.post('/verify-email', AuthController.verifyEmail);
router.post('/refresh-token', AuthController.refreshToken);
router.get('/me', AuthController.getMe);

export default router;
