import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { badgeCandidature, STATUT_CANDIDATURE_LABELS } from '../../core/badges';
import { Candidature } from '../../core/models';

@Component({
  selector: 'app-mes-candidatures',
  templateUrl: './mes-candidatures.component.html'
})
export class MesCandidaturesComponent implements OnInit {

  candidatures: Candidature[] = [];
  loading = true;
  error = '';
  badge = badgeCandidature;
  labels = STATUT_CANDIDATURE_LABELS;

  constructor(private api: ApiService) { }

  ngOnInit(): void {
    this.api.getMesCandidatures().subscribe({
      next: list => { this.candidatures = list; this.loading = false; },
      error: err => { this.loading = false; this.error = this.api.toApiError(err).message; }
    });
  }
}
