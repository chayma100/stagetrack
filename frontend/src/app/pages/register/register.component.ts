import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { RegisterRequest } from '../../core/models';

@Component({
  selector: 'app-register',
  templateUrl: './register.component.html'
})
export class RegisterComponent implements OnInit {

  model: RegisterRequest = { email: '', password: '', tel: '', nom: '', prenom: '', profil: 'ETUDIANT' };
  error = '';
  fieldErrors: { [field: string]: string } = {};
  loading = false;

  constructor(private auth: AuthService, private api: ApiService, private router: Router,
              private route: ActivatedRoute) { }

  /** Le profil est choisi sur la page précédente (choix-profil). Sans choix, on y retourne. */
  ngOnInit(): void {
    const profil = this.route.snapshot.queryParamMap.get('profil');
    if (profil === 'ETUDIANT' || profil === 'COLLABORATEUR') {
      this.model.profil = profil;
    } else {
      this.router.navigate(['/choix-profil']);
    }
  }

  get profilLabel(): string {
    return this.model.profil === 'COLLABORATEUR' ? 'Recruteur' : 'Étudiant';
  }

  submit(): void {
    this.error = '';
    this.fieldErrors = {};
    this.loading = true;
    this.auth.register(this.model).subscribe({
      next: () => this.router.navigate(['/login']),
      error: err => {
        this.loading = false;
        const e = this.api.toApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fields || {};
      }
    });
  }
}
