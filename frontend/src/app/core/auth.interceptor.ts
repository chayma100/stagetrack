import { Injectable } from '@angular/core';
import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, throwError } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { environment } from '../../environments/environment';
import { AuthService } from './auth.service';

/**
 * 1) Ajoute "Authorization: Bearer <token>" à chaque requête vers l'API.
 * 2) 401 sur une route protégée : session expirée, retour à la page de connexion.
 * 3) 403 : droits insuffisants, page "accès refusé".
 */
@Injectable()
export class AuthInterceptor implements HttpInterceptor {

  constructor(private auth: AuthService, private router: Router) { }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const isApi = req.url.startsWith(environment.apiUrl);
    const isAuthCall = req.url.indexOf('/auth/') >= 0;
    const token = this.auth.token;

    if (isApi && token && !isAuthCall) {
      req = req.clone({ setHeaders: { Authorization: `Bearer ${token}` } });
    }

    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {
        if (isApi && !isAuthCall) {
          if (error.status === 401) {
            this.auth.logout();
          } else if (error.status === 403) {
            this.router.navigate(['/access-denied']);
          }
        }
        return throwError(() => error);
      })
    );
  }
}
