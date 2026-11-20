import { Component, ElementRef, OnDestroy, OnInit, ViewChild } from '@angular/core';
import { forkJoin } from 'rxjs';
import Chart from 'chart.js';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { badgeCandidature, STATUT_CANDIDATURE_LABELS } from '../../core/badges';
import { Candidature, Offre, Stats } from '../../core/models';

type Mode = 'ADMIN' | 'RECRUTEUR' | 'ETUDIANT';

interface Carte { titre: string; valeur: number; sous: string; icone: string; couleur: string; }
interface Ligne { titre: string; sous: string; badge?: string; badgeClasse?: string; lien?: any[]; }

/** Un seul composant, trois tableaux de bord : le contenu dépend du rôle de l'utilisateur connecté. */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html'
})
export class DashboardComponent implements OnInit, OnDestroy {

  mode: Mode = 'ETUDIANT';
  prenom = '';
  titre = '';
  sousTitre = '';
  actionLabel = '';
  actionLien: any[] = ['/offres'];

  cartes: Carte[] = [];
  counts: { [statut: string]: number } = {};
  chartTitre = '';
  recentTitre = '';
  recent: Ligne[] = [];
  basTitre = '';
  bas: Offre[] = [];
  loaded = false;
  error = '';
  private chart: any;

  @ViewChild('statutChart') canvas: ElementRef<HTMLCanvasElement>;

  constructor(private api: ApiService, private auth: AuthService) { }

  ngOnInit(): void {
    const user = this.auth.currentUser;
    this.prenom = user ? user.prenom : '';
    if (this.auth.isAdmin()) {
      this.mode = 'ADMIN';
      this.loadAdmin();
    } else if (this.auth.hasRole('COLLABORATEUR')) {
      this.mode = 'RECRUTEUR';
      this.loadRecruteur();
    } else {
      this.mode = 'ETUDIANT';
      this.loadEtudiant();
    }
  }

  ngOnDestroy(): void {
    if (this.chart) {
      this.chart.destroy();
    }
  }

  // ---------- Admin : vue globale ----------
  private loadAdmin(): void {
    this.titre = 'Vue d\'ensemble de la plateforme';
    this.sousTitre = 'Toutes les entreprises, offres et candidatures.';
    this.actionLabel = 'Gérer les utilisateurs';
    this.actionLien = ['/users'];
    this.api.getStats().subscribe({
      next: (s: Stats) => {
        this.cartes = [
          { titre: 'Offres ouvertes', valeur: s.offresOuvertes, sous: 'sur ' + s.totalOffres + ' offres au total', icone: 'ni-briefcase-24', couleur: 'bg-success' },
          { titre: 'Entreprises', valeur: s.totalEntreprises, sous: 'partenaires enregistrés', icone: 'ni-building', couleur: 'bg-info' },
          { titre: 'Candidatures', valeur: s.totalCandidatures, sous: (s.candidaturesParStatut['ACCEPTEE'] || 0) + ' acceptée(s)', icone: 'ni-send', couleur: 'bg-warning' },
          { titre: 'Entretiens', valeur: s.candidaturesParStatut['ENTRETIEN'] || 0, sous: 'candidatures en entretien', icone: 'ni-chat-round', couleur: 'bg-danger' }
        ];
        this.counts = s.candidaturesParStatut;
        this.chartTitre = 'Candidatures par statut';
        this.recentTitre = 'Entreprises avec le plus d\'offres';
        this.recent = s.topEntreprises.map(t => ({ titre: t.nom, sous: t.nbOffres + ' offre(s)' }));
        this.basTitre = 'Offres qui expirent dans 14 jours';
        this.bas = s.offresExpirantBientot;
        this.done();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  // ---------- Recruteur : ses offres et les candidatures reçues ----------
  private loadRecruteur(): void {
    this.titre = 'Mon espace recruteur';
    this.sousTitre = 'Suivez vos offres publiées et les candidatures reçues.';
    this.actionLabel = 'Publier une offre';
    this.actionLien = ['/offres/new'];
    forkJoin({
      offres: this.api.getOffres({ mine: true }, 0, 100),
      recues: this.api.getCandidatures('', 0, 100)
    }).subscribe({
      next: ({ offres, recues }) => {
        const ouvertes = offres.content.filter(o => o.statut === 'OUVERTE').length;
        this.counts = this.compter(recues.content);
        this.cartes = [
          { titre: 'Mes offres', valeur: offres.totalElements, sous: ouvertes + ' ouverte(s)', icone: 'ni-briefcase-24', couleur: 'bg-success' },
          { titre: 'Candidatures reçues', valeur: recues.totalElements, sous: 'sur vos offres', icone: 'ni-send', couleur: 'bg-warning' },
          { titre: 'À traiter', valeur: this.counts['ENVOYEE'] || 0, sous: 'nouvelles candidatures', icone: 'ni-bell-55', couleur: 'bg-info' },
          { titre: 'Entretiens', valeur: this.counts['ENTRETIEN'] || 0, sous: 'en cours', icone: 'ni-chat-round', couleur: 'bg-danger' }
        ];
        this.chartTitre = 'Candidatures reçues par statut';
        this.recentTitre = 'Dernières candidatures reçues';
        this.recent = recues.content.slice(0, 6).map(c => this.ligneCandidature(c, true));
        this.basTitre = 'Mes dernières offres';
        this.bas = offres.content.slice(0, 5);
        this.done();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  // ---------- Etudiant : ses candidatures et les offres à venir ----------
  private loadEtudiant(): void {
    this.titre = 'Mon espace étudiant';
    this.sousTitre = 'Trouvez un stage et suivez vos candidatures.';
    this.actionLabel = 'Voir les offres';
    this.actionLien = ['/offres'];
    forkJoin({
      stats: this.api.getStats(),
      mes: this.api.getMesCandidatures()
    }).subscribe({
      next: ({ stats, mes }) => {
        this.counts = this.compter(mes);
        this.cartes = [
          { titre: 'Offres ouvertes', valeur: stats.offresOuvertes, sous: 'disponibles maintenant', icone: 'ni-briefcase-24', couleur: 'bg-success' },
          { titre: 'Mes candidatures', valeur: mes.length, sous: 'envoyées au total', icone: 'ni-send', couleur: 'bg-info' },
          { titre: 'Entretiens', valeur: this.counts['ENTRETIEN'] || 0, sous: 'à préparer', icone: 'ni-chat-round', couleur: 'bg-warning' },
          { titre: 'Acceptées', valeur: this.counts['ACCEPTEE'] || 0, sous: 'félicitations', icone: 'ni-check-bold', couleur: 'bg-danger' }
        ];
        this.chartTitre = 'Mes candidatures par statut';
        this.recentTitre = 'Mes dernières candidatures';
        this.recent = mes.slice(0, 6).map(c => this.ligneCandidature(c, false));
        this.basTitre = 'Offres qui expirent dans 14 jours';
        this.bas = stats.offresExpirantBientot;
        this.done();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  private compter(list: Candidature[]): { [statut: string]: number } {
    const res: { [statut: string]: number } = {};
    list.forEach(c => res[c.statut] = (res[c.statut] || 0) + 1);
    return res;
  }

  private ligneCandidature(c: Candidature, avecCandidat: boolean): Ligne {
    return {
      titre: avecCandidat ? c.userNom : c.offreLibelle,
      sous: avecCandidat ? c.offreLibelle : c.entrepriseNom,
      badge: STATUT_CANDIDATURE_LABELS[c.statut],
      badgeClasse: badgeCandidature(c.statut)
    };
  }

  private done(): void {
    this.loaded = true;
    // le canvas n'existe qu'après l'affichage du bloc *ngIf
    setTimeout(() => this.drawChart(), 0);
  }

  private drawChart(): void {
    if (!this.canvas) {
      return;
    }
    const keys = ['ENVOYEE', 'ENTRETIEN', 'ACCEPTEE', 'REFUSEE'];
    this.chart = new Chart(this.canvas.nativeElement, {
      type: 'bar',
      data: {
        labels: keys.map(k => STATUT_CANDIDATURE_LABELS[k]),
        datasets: [{
          label: 'Candidatures',
          data: keys.map(k => this.counts[k] || 0),
          backgroundColor: ['#11cdef', '#fb6340', '#2dce89', '#f5365c']
        }]
      },
      options: {
        legend: { display: false },
        scales: { yAxes: [{ ticks: { beginAtZero: true, precision: 0 } }] }
      }
    });
  }
}
