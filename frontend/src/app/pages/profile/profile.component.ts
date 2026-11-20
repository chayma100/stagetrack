import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { User } from '../../core/models';

@Component({
  selector: 'app-profile',
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {

  user: User | null = null;
  nom = '';
  prenom = '';
  tel = '';
  newPassword = '';
  error = '';
  fieldErrors: { [field: string]: string } = {};
  success = '';
  saving = false;

  constructor(private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    this.api.getMe().subscribe({
      next: u => this.fill(u),
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  save(): void {
    this.error = '';
    this.success = '';
    this.fieldErrors = {};
    this.saving = true;
    this.api.updateMe({
      nom: this.nom, prenom: this.prenom, tel: this.tel,
      newPassword: this.newPassword || undefined
    }).subscribe({
      next: u => {
        this.saving = false;
        this.fill(u);
        this.auth.setUser(u);
        this.success = 'Profil mis à jour.';
      },
      error: err => {
        this.saving = false;
        const e = this.api.toApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fields || {};
      }
    });
  }

  private fill(u: User): void {
    this.user = u;
    this.nom = u.nom;
    this.prenom = u.prenom;
    this.tel = u.tel || '';
    this.newPassword = '';
  }
}
