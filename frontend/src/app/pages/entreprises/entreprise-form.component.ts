import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Entreprise } from '../../core/models';

@Component({
  selector: 'app-entreprise-form',
  templateUrl: './entreprise-form.component.html'
})
export class EntrepriseFormComponent implements OnInit {

  id: number | null = null;
  model: Entreprise = { nom: '', tel: '', email: '', adresse: '', secteur: '', siteWeb: '' };
  error = '';
  fieldErrors: { [field: string]: string } = {};
  saving = false;

  constructor(private route: ActivatedRoute, private router: Router, private api: ApiService) { }

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = +id;
      this.api.getEntreprise(this.id).subscribe({
        next: detail => this.model = { ...detail.entreprise },
        error: err => this.error = this.api.toApiError(err).message
      });
    }
  }

  save(): void {
    this.error = '';
    this.fieldErrors = {};
    this.saving = true;
    const request = this.id
      ? this.api.updateEntreprise(this.id, this.model)
      : this.api.createEntreprise(this.model);
    request.subscribe({
      next: () => this.router.navigate(['/entreprises']),
      error: err => {
        this.saving = false;
        const e = this.api.toApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fields || {};
      }
    });
  }
}
