import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { Entreprise, Offre } from '../../core/models';

@Component({
  selector: 'app-offre-form',
  templateUrl: './offre-form.component.html'
})
export class OffreFormComponent implements OnInit {

  id: number | null = null;
  entreprises: Entreprise[] = [];
  model: Offre = {
    libelle: '', gratification: 0, duree: 6, description: '', type: 'PFE', dateLimite: '',
    statut: 'OUVERTE', entrepriseId: null as any
  };
  /** Nom de l'entreprise saisi ou choisi dans les suggestions. */
  entrepriseSaisie = '';
  error = '';
  fieldErrors: { [field: string]: string } = {};
  saving = false;

  constructor(private route: ActivatedRoute, private router: Router, private api: ApiService) { }

  ngOnInit(): void {
    this.api.getEntreprises('', 0, 100).subscribe({
      next: p => {
        this.entreprises = p.content;
        // arrivée depuis la fiche d'une entreprise : afficher son nom dans le champ
        if (!this.entrepriseSaisie && this.model.entrepriseId) {
          const e = this.entreprises.find(x => x.id === this.model.entrepriseId);
          this.entrepriseSaisie = e ? e.nom : '';
        }
      }
    });

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.id = +id;
      this.api.getOffre(this.id).subscribe({
        next: o => {
          this.model = { ...o, dateLimite: o.dateLimite || '' };
          this.entrepriseSaisie = o.entrepriseNom || '';
        },
        error: err => this.error = this.api.toApiError(err).message
      });
    } else {
      // arrivée depuis la fiche d'une entreprise : /offres/new?entrepriseId=3
      const preset = this.route.snapshot.queryParamMap.get('entrepriseId');
      if (preset) {
        this.model.entrepriseId = +preset;
      }
    }
  }

  save(): void {
    this.error = '';
    this.fieldErrors = {};
    this.saving = true;
    // une date vide doit partir en null, pas en chaîne vide
    const nom = this.entrepriseSaisie.trim();
    const connue = this.entreprises.find(e => e.nom.trim().toLowerCase() === nom.toLowerCase());
    // entreprise connue : on envoie son identifiant ; sinon le nom, et le serveur crée l'entreprise
    const body: Offre = {
      ...this.model,
      dateLimite: this.model.dateLimite || undefined,
      entrepriseId: connue ? connue.id : (null as any),
      entrepriseNom: nom
    };
    const request = this.id ? this.api.updateOffre(this.id, body) : this.api.createOffre(body);
    request.subscribe({
      next: () => this.router.navigate(['/offres']),
      error: err => {
        this.saving = false;
        const e = this.api.toApiError(err);
        this.error = e.message;
        this.fieldErrors = e.fields || {};
      }
    });
  }
}
