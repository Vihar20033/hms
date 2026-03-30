import { Injectable } from '@angular/core';

@Injectable({
  providedIn: 'root',
})
export class CookieService {
  set(name: string, value: string, expiresDays: number = 7, path: string = '/'): void {
    const d = new Date();
    d.setTime(d.getTime() + expiresDays * 24 * 60 * 60 * 1000);
    const expires = `expires=${d.toUTCString()}`;
    document.cookie = `${name}=${encodeURIComponent(value)};${expires};path=${path};SameSite=Lax`;
  }

  get(name: string): string | null {
    const nameEQ = `${name}=`;
    const ca = document.cookie.split(';');
    for (let i = 0; i < ca.length; i++) {
      let c = ca[i];
      while (c.charAt(0) === ' ') c = c.substring(1, c.length);
      if (c.indexOf(nameEQ) === 0) {
        return decodeURIComponent(c.substring(nameEQ.length, c.length));
      }
    }
    return null;
  }

  delete(name: string, path: string = '/'): void {
    this.set(name, '', -1, path);
  }

  exists(name: string): boolean {
    return this.get(name) !== null;
  }
}
