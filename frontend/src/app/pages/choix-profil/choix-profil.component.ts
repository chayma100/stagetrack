import { Component } from '@angular/core';
import { Router } from '@angular/router';

@Component({
  selector: 'app-choix-profil',
  templateUrl: './choix-profil.component.html'
})
export class ChoixProfilComponent {

  constructor(private router: Router) { }

  choisir(profil: 'ETUDIANT' | 'COLLABORATEUR'): void {
    this.router.navigate(['/register'], { queryParams: { profil } });
  }
}
