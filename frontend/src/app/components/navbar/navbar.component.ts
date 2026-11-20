import { Component, OnInit } from '@angular/core';
import { MENU_ADMIN, MENU_ETUDIANT, MENU_RECRUTEUR } from '../sidebar/sidebar.component';
import { Location } from '@angular/common';
import { AuthService } from '../../core/auth.service';
import { User } from '../../core/models';

@Component({
  selector: 'app-navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss']
})
export class NavbarComponent implements OnInit {
  public listTitles: any[] = [];
  public location: Location;
  public user: User | null = null;

  constructor(location: Location, public auth: AuthService) {
    this.location = location;
  }

  ngOnInit() {
    this.auth.user$.subscribe(u => {
      this.user = u;
      // le titre de la page vient du menu du profil connecté
      this.listTitles = this.auth.isAdmin() ? MENU_ADMIN
        : this.auth.hasRole('COLLABORATEUR') ? MENU_RECRUTEUR : MENU_ETUDIANT;
    });
  }

  get initials(): string {
    return this.user ? (this.user.prenom.charAt(0) + this.user.nom.charAt(0)).toUpperCase() : '';
  }

  getTitle() {
    var titlee = this.location.prepareExternalUrl(this.location.path());
    if (titlee.charAt(0) === '#') {
      titlee = titlee.slice(1);
    }
    titlee = titlee.split('?')[0];
    // /offres/12/edit correspond à l'entrée /offres du menu
    for (var item = 0; item < this.listTitles.length; item++) {
      const path = this.listTitles[item].path;
      if (titlee === path || titlee.startsWith(path + '/')) {
        return this.listTitles[item].title;
      }
    }
    return 'StageTrack';
  }

  logout(): void {
    this.auth.logout();
  }
}
