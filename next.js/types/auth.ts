export interface User {
  id: number;
  email: string;
  name: string;
  profile: string | null;
  roles: string[];
}

export interface AuthResult {
  accessToken: string;
  refreshToken: string;
  user: User;
}

export interface LoginRequest {
  email: string;
  password: string;
}