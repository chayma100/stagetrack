import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { badgeCandidature, STATUT_CANDIDATURE_LABELS } from '../../core/badges';
import { Candidature, StatutCandidature } from '../../core/models';

@Component({
  selector: 'app-candidatures',
  templateUrl: './candidatures.component.html'
})
export class CandidaturesComponent implements OnInit {

  candidatures: Candidature[] = [];
  statutFilter = '';
  pageNumber = 1;
  readonly size = 10;
  total = 0;
  error = '';
  info = '';
  loading = false;
  badge = badgeCandidature;
  labels = STATUT_CANDIDATURE_LABELS;
  statuts: StatutCandidature[] = ['ENVOYEE', 'ENTRETIEN', 'ACCEPTEE', 'REFUSEE'];

  constructor(private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getCandidatures(this.statutFilter, this.pageNumber - 1, this.size).subscribe({
      next: page => {
        this.candidatures = page.content;
        this.total = page.totalElements;
        this.loading = false;
        this.error = '';
      },
      error: err => { this.loading = false; this.error = this.api.toApiError(err).message; }
    });
  }

  onFilter(): void {
    this.pageNumber = 1;
    this.load();
  }

  onPage(page: number): void {
    this.pageNumber = page;
    this.load();
  }

  changeStatut(c: Candidature, statut: StatutCandidature): void {
    this.error = '';
    this.info = '';
    this.api.updateStatutCandidature(c.id, statut).subscribe({
      next: updated => {
        c.statut = updated.statut;
        this.info = `Statut de ${c.userNom} mis à jour : ${this.labels[statut]}. Un email lui est envoyé.`;
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  telechargerCv(c: Candidature): void {
    this.api.downloadCv(c.id).subscribe({
      next: blob => this.api.download(blob, c.cvNom || 'cv'),
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  exportCsv(): void {
    this.api.exportCandidatures().subscribe({
      next: blob => this.api.download(blob, 'candidatures.csv'),
      error: err => this.error = this.api.toApiError(err).message
    });
  }
}
