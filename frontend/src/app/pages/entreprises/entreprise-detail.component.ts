import { Component, OnInit } from '@angular/core';
import { ActivatedRoute } from '@angular/router';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { badgeOffre } from '../../core/badges';
import { EntrepriseDetail } from '../../core/models';

@Component({
  selector: 'app-entreprise-detail',
  templateUrl: './entreprise-detail.component.html'
})
export class EntrepriseDetailComponent implements OnInit {

  detail: EntrepriseDetail | null = null;
  error = '';
  badge = badgeOffre;

  constructor(private route: ActivatedRoute, private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    const id = +this.route.snapshot.paramMap.get('id')!;
    this.api.getEntreprise(id).subscribe({
      next: d => this.detail = d,
      error: err => this.error = this.api.toApiError(err).message
    });
  }
}
