import { Component, OnInit } from '@angular/core';
import { Router } from '@angular/router';
import { AuthService } from '../../core/auth.service';

declare interface RouteInfo {
    path: string;
    title: string;
    icon: string;
    class: string;
    queryParams?: { [key: string]: string };
}
const DASHBOARD: RouteInfo = { path: '/dashboard', title: 'Tableau de bord', icon: 'ni-tv-2 text-primary', class: '' };
const ENTREPRISES: RouteInfo = { path: '/entreprises', title: 'Entreprises', icon: 'ni-building text-blue', class: '' };
const PROFIL: RouteInfo = { path: '/profile', title: 'Mon profil', icon: 'ni-circle-08 text-pink', class: '' };

/** Un menu par profil : chacun ne voit que ce qui lui sert. */
export const MENU_ETUDIANT: RouteInfo[] = [
    DASHBOARD,
    { path: '/offres', title: 'Offres de stage', icon: 'ni-briefcase-24 text-orange', class: '' },
    ENTREPRISES,
    { path: '/mes-candidatures', title: 'Mes candidatures', icon: 'ni-send text-info', class: '' },
    PROFIL
];

export const MENU_RECRUTEUR: RouteInfo[] = [
    DASHBOARD,
    { path: '/offres', title: 'Mes offres', icon: 'ni-briefcase-24 text-orange', class: '', queryParams: { mine: 'true' } },
    { path: '/candidatures', title: 'Candidatures reçues', icon: 'ni-bullet-list-67 text-red', class: '' },
    ENTREPRISES,
    PROFIL
];

export const MENU_ADMIN: RouteInfo[] = [
    DASHBOARD,
    ENTREPRISES,
    { path: '/offres', title: 'Offres de stage', icon: 'ni-briefcase-24 text-orange', class: '' },
    { path: '/candidatures', title: 'Candidatures', icon: 'ni-bullet-list-67 text-red', class: '' },
    { path: '/users', title: 'Utilisateurs', icon: 'ni-single-02 text-yellow', class: '' },
    PROFIL
];

@Component({
  selector: 'app-sidebar',
  templateUrl: './sidebar.component.html',
  styleUrls: ['./sidebar.component.scss']
})
export class SidebarComponent implements OnInit {

  public menuItems: any[] = [];
  public isCollapsed = true;
  public espace = '';

  constructor(private router: Router, public auth: AuthService) { }

  ngOnInit() {
    // le menu dépend du profil de l'utilisateur connecté
    this.auth.user$.subscribe(() => {
      if (this.auth.isAdmin()) {
        this.menuItems = MENU_ADMIN;
        this.espace = 'Espace administrateur';
      } else if (this.auth.hasRole('COLLABORATEUR')) {
        this.menuItems = MENU_RECRUTEUR;
        this.espace = 'Espace recruteur';
      } else if (this.auth.hasRole('ETUDIANT')) {
        this.menuItems = MENU_ETUDIANT;
        this.espace = 'Espace étudiant';
      } else {
        this.menuItems = [];
        this.espace = '';
      }
    });
    this.router.events.subscribe((event) => {
      this.isCollapsed = true;
   });
  }

  logout(): void {
    this.auth.logout();
  }
}
