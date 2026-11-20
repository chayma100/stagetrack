import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Entreprise } from '../../core/models';

@Component({
  selector: 'app-entreprises',
  templateUrl: './entreprises.component.html'
})
export class EntreprisesComponent implements OnInit {

  entreprises: Entreprise[] = [];
  search = '';
  pageNumber = 1;      // ng-bootstrap compte les pages à partir de 1, l'API à partir de 0
  readonly size = 10;
  total = 0;
  error = '';
  loading = false;

  constructor(private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getEntreprises(this.search, this.pageNumber - 1, this.size).subscribe({
      next: page => {
        this.entreprises = page.content;
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

  onPage(page: number): void {
    this.pageNumber = page;
    this.load();
  }

  remove(e: Entreprise): void {
    const msg = `Supprimer « ${e.nom} » ? Ses ${e.nbOffres || 0} offre(s) et leurs candidatures seront aussi supprimées.`;
    if (!confirm(msg)) {
      return;
    }
    this.api.deleteEntreprise(e.id!).subscribe({
      next: () => {
        if (this.entreprises.length === 1 && this.pageNumber > 1) {
          this.pageNumber--;
        }
        this.load();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }
}
