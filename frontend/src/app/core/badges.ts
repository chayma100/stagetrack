import { StatutCandidature, StatutOffre } from './models';

export const STATUT_CANDIDATURE_LABELS: { [k: string]: string } = {
  ENVOYEE: 'Envoyée', ENTRETIEN: 'Entretien', ACCEPTEE: 'Acceptée', REFUSEE: 'Refusée'
};

export function badgeCandidature(s: StatutCandidature): string {
  switch (s) {
    case 'ACCEPTEE': return 'badge-success';
    case 'REFUSEE': return 'badge-danger';
    case 'ENTRETIEN': return 'badge-warning';
    default: return 'badge-info';
  }
}

export function badgeOffre(s?: StatutOffre): string {
  return s === 'OUVERTE' ? 'badge-success' : 'badge-secondary';
}
