import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { badgeOffre } from '../../core/badges';
import { Entreprise, Offre } from '../../core/models';

@Component({
  selector: 'app-offres',
  templateUrl: './offres.component.html'
})
export class OffresComponent implements OnInit {

  offres: Offre[] = [];
  entreprises: Entreprise[] = [];
  filters: { search: string; entrepriseId: number | null; type: string; statut: string; dureeMax: number | null; mine: boolean } =
    { search: '', entrepriseId: null, type: '', statut: '', dureeMax: null, mine: false };
  pageNumber = 1;
  readonly size = 10;
  total = 0;
  error = '';
  loading = false;
  badge = badgeOffre;

  constructor(private api: ApiService, public auth: AuthService, private route: ActivatedRoute) { }

  ngOnInit(): void {
    // liste déroulante des entreprises pour le filtre
    this.api.getEntreprises('', 0, 100).subscribe({ next: p => this.entreprises = p.content });
    // « Mes offres publiées » (menu du recruteur) arrive avec ?mine=true
    this.route.queryParamMap.subscribe(params => {
      this.filters.mine = params.get('mine') === 'true';
      this.pageNumber = 1;
      this.load();
    });
  }

  load(): void {
    this.loading = true;
    this.api.getOffres(this.filters, this.pageNumber - 1, this.size).subscribe({
      next: page => {
        this.offres = page.content;
        this.total = page.totalElements;
        this.loading = false;
        this.error = '';
      },
      error: err => {
        this.loading = false;
        this.error = this.api.toApiError(err).message;
      }
    });
  }

  onSearch(): void {
    this.pageNumber = 1;
    this.load();
  }

  reset(): void {
    this.filters = { search: '', entrepriseId: null, type: '', statut: '', dureeMax: null, mine: false };
    this.onSearch();
  }

  onPage(page: number): void {
    this.pageNumber = page;
    this.load();
  }

  remove(o: Offre): void {
    if (!confirm(`Supprimer l'offre « ${o.libelle} » et ses candidatures ?`)) {
      return;
    }
    this.api.deleteOffre(o.id!).subscribe({
      next: () => {
        if (this.offres.length === 1 && this.pageNumber > 1) {
          this.pageNumber--;
        }
        this.load();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  exportCsv(): void {
    this.api.exportOffres().subscribe({
      next: blob => this.api.download(blob, 'offres.csv'),
      error: err => this.error = this.api.toApiError(err).message
    });
  }
}
