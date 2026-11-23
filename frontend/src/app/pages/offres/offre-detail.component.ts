import { Component, ElementRef, OnInit, ViewChild } from '@angular/core';
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
  cv: File | null = null;
  cvError = '';
  readonly cvTailleMax = 5 * 1024 * 1024;
  @ViewChild('cvInput') cvInput?: ElementRef<HTMLInputElement>;
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

  onCvChange(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files && input.files.length > 0 ? input.files[0] : null;
    this.cv = null;
    this.cvError = '';
    if (!file) {
      return;
    }
    if (!/\.(pdf|docx?|PDF|DOCX?)$/.test(file.name)) {
      this.cvError = 'Le CV doit être un fichier PDF, DOC ou DOCX.';
    } else if (file.size > this.cvTailleMax) {
      this.cvError = 'Le CV ne doit pas dépasser 5 Mo.';
    } else {
      this.cv = file;
    }
    if (this.cvError) {
      input.value = '';
    }
  }

  retirerCv(): void {
    this.cv = null;
    this.cvError = '';
    if (this.cvInput) {
      this.cvInput.nativeElement.value = '';
    }
  }

  postuler(): void {
    this.error = '';
    this.success = '';
    this.sending = true;
    this.api.postuler(this.offre!.id!, this.commentaire, this.cv).subscribe({
      next: () => {
        this.sending = false;
        this.success = 'Votre candidature a bien été envoyée. Suivez-la dans « Mes candidatures ».';
        this.commentaire = '';
        this.retirerCv();
      },
      error: err => {
        this.sending = false;
        this.error = this.api.toApiError(err).message;
      }
    });
  }
}
