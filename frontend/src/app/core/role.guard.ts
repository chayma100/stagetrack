import { Injectable } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';
import { RoleName } from './models';

/**
 * Vérifie que l'utilisateur a l'un des rôles déclarés dans la route :
 *   { path: 'users', component: UsersComponent, data: { roles: ['ADMIN'] } }
 */
@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {

  constructor(private auth: AuthService, private router: Router) { }

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const roles: RoleName[] = route.data['roles'] || [];
    if (roles.length === 0 || this.auth.hasAnyRole(roles)) {
      return true;
    }
    return this.router.parseUrl('/access-denied');
  }
}
