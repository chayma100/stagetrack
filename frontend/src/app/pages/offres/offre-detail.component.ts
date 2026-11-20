import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { badgeOffre } from '../../core/badges';
import { Offre } from '../../core/models';

@Component({
  selector: 'app-offre-detail',
  templateUrl: './offre-detail.component.html'
})
export class OffreDetailComponent implements OnInit {

  offre: Offre | null = null;
  commentaire = '';
  error = '';
  success = '';
  sending = false;
  badge = badgeOffre;

  constructor(private route: ActivatedRoute, private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id')!;
    this.api.getOffre(id).subscribe({
      next: o => this.offre = o,
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  /** L'offre accepte des candidatures si elle est ouverte et que sa date limite n'est pas passée. */
  get canApply(): boolean {
    if (!this.offre || this.offre.statut !== 'OUVERTE') {
      return false;
    }
    if (!this.offre.dateLimite) {
      return true;
    }
    const today = new Date().toISOString().substring(0, 10);
    return this.offre.dateLimite >= today;
  }

  postuler(): void {
    this.error = '';
    this.success = '';
    this.sending = true;
    this.api.postuler(this.offre!.id!, this.commentaire).subscribe({
      next: () => {
        this.sending = false;
        this.success = 'Votre candidature a bien été envoyée. Suivez-la dans « Mes candidatures ».';
        this.commentaire = '';
      },
      error: err => {
        this.sending = false;
        this.error = this.api.toApiError(err).message;
      }
    });
  }
}
