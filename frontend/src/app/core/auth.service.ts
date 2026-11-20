import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable } from 'rxjs';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthResponse, RegisterRequest, RoleName, User } from './models';

const TOKEN_KEY = 'stagetrack_token';
const USER_KEY = 'stagetrack_user';

@Injectable({ providedIn: 'root' })
export class AuthService {

  private userSubject = new BehaviorSubject<User | null>(this.readUser());
  public user$ = this.userSubject.asObservable();

  constructor(private http: HttpClient, private router: Router) { }

  login(email: string, password: string): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/login`, { email, password })
      .pipe(tap(res => this.store(res)));
  }

  register(request: RegisterRequest): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${environment.apiUrl}/auth/register`, request);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    this.userSubject.next(null);
    this.router.navigate(['/login']);
  }

  get token(): string | null {
    return localStorage.getItem(TOKEN_KEY);
  }

  get currentUser(): User | null {
    return this.userSubject.value;
  }

  /** Met à jour l'utilisateur courant (après modification du profil). */
  setUser(user: User): void {
    localStorage.setItem(USER_KEY, JSON.stringify(user));
    this.userSubject.next(user);
  }

  isLoggedIn(): boolean {
    const token = this.token;
    return !!token && !this.isExpired(token);
  }

  hasRole(role: RoleName): boolean {
    const user = this.currentUser;
    return !!user && user.roles.indexOf(role) >= 0;
  }

  hasAnyRole(roles: RoleName[]): boolean {
    return roles.some(r => this.hasRole(r));
  }

  /** Noms affichés pour les rôles (ex. COLLABORATEUR devient Recruteur). */
  roleLabels(roles: RoleName[]): string {
    const noms: { [r: string]: string } = { ADMIN: 'Administrateur', COLLABORATEUR: 'Recruteur', ETUDIANT: 'Étudiant' };
    return roles.map(r => noms[r] || r).join(', ');
  }

  isAdmin(): boolean {
    return this.hasRole('ADMIN');
  }

  /** Peut publier des offres et des entreprises : Admin ou Collaborateur (recruteur). */
  canPublish(): boolean {
    return this.hasAnyRole(['ADMIN', 'COLLABORATEUR']);
  }

  /** Peut postuler à une offre : Etudiant. */
  canPostuler(): boolean {
    return this.hasRole('ETUDIANT');
  }

  private store(res: AuthResponse): void {
    localStorage.setItem(TOKEN_KEY, res.token);
    localStorage.setItem(USER_KEY, JSON.stringify(res.user));
    this.userSubject.next(res.user);
  }

  private readUser(): User | null {
    try {
      const raw = localStorage.getItem(USER_KEY);
      return raw ? JSON.parse(raw) : null;
    } catch {
      return null;
    }
  }

  /** Lit la date d'expiration (claim exp) du JWT, sans vérifier la signature (le serveur s'en charge). */
  private isExpired(token: string): boolean {
    try {
      const payload = JSON.parse(atob(token.split('.')[1].replace(/-/g, '+').replace(/_/g, '/')));
      return !!payload.exp && payload.exp * 1000 < Date.now();
    } catch {
      return true;
    }
  }
}
