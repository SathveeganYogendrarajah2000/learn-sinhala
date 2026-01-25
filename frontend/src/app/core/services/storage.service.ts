import { Injectable } from '@angular/core';

const TOKEN_KEY = 'auth_token';
const USER_KEY = 'auth_user';

/**
 * Service for browser storage operations.
 * Uses localStorage for persistence across sessions.
 */
@Injectable({
  providedIn: 'root'
})
export class StorageService {

  // ========================
  // Token Management
  // ========================

  getToken(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  setToken(token: string): void {
    localStorage.setItem(TOKEN_KEY, token);
  }

  removeToken(): void {
    localStorage.removeItem(TOKEN_KEY);
  }

  hasToken(): boolean {
    return !!this.getToken();
  }

  // ========================
  // User Management
  // ========================

  getUser<T>(): T | null {
    const user = localStorage.getItem(USER_KEY);
    return user ? JSON.parse(user) : null;
  }

  setUser<T>(user: T): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
  }

  removeUser(): void {
    localStorage.removeItem(USER_KEY);
  }

  // ========================
  // Clear All
  // ========================

  clear(): void {
    this.removeToken();
    this.removeUser();
  }

  // ========================
  // Generic Storage
  // ========================

  get<T>(key: string): T | null {
    const item = localStorage.getItem(key);
    if (!item) return null;

    try {
      return JSON.parse(item);
    } catch {
      return item as unknown as T;
    }
  }

  set<T>(key: string, value: T): void {
    const item = typeof value === 'string' ? value : JSON.stringify(value);
    localStorage.setItem(key, item);
  }

  remove(key: string): void {
    localStorage.removeItem(key);
  }
}
