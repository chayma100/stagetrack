export type RoleName = 'ADMIN' | 'COLLABORATEUR' | 'ETUDIANT';
export type Profil = 'ETUDIANT' | 'COLLABORATEUR';
export type TypeStage = 'PFE' | 'ETE';
export type StatutOffre = 'OUVERTE' | 'FERMEE';
export type StatutCandidature = 'ENVOYEE' | 'ENTRETIEN' | 'ACCEPTEE' | 'REFUSEE';

export interface User {
  id: number;
  email: string;
  tel?: string;
  nom: string;
  prenom: string;
  roles: RoleName[];
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface RegisterRequest {
  email: string;
  password: string;
  tel?: string;
  nom: string;
  prenom: string;
  profil?: Profil;
}

export interface Entreprise {
  id?: number;
  nom: string;
  tel?: string;
  email?: string;
  adresse?: string;
  secteur?: string;
  siteWeb?: string;
  nbOffres?: number;
}

export interface EntrepriseDetail {
  entreprise: Entreprise;
  offres: Offre[];
}

export interface Offre {
  id?: number;
  libelle: string;
  gratification?: number;
  duree: number;
  description?: string;
  type: TypeStage;
  dateLimite?: string;
  statut?: StatutOffre;
  entrepriseId: number;
  entrepriseNom?: string;
}

export interface Candidature {
  id: number;
  statut: StatutCandidature;
  dateEnvoi: string;
  commentaire?: string;
  userId: number;
  userNom: string;
  userEmail: string;
  offreId: number;
  offreLibelle: string;
  entrepriseNom: string;
  cvNom?: string | null;
}

export interface Page<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
}

export interface Stats {
  offresOuvertes: number;
  totalOffres: number;
  totalEntreprises: number;
  totalCandidatures: number;
  candidaturesParStatut: { [statut: string]: number };
  topEntreprises: { nom: string; nbOffres: number }[];
  offresExpirantBientot: Offre[];
}

export interface ApiError {
  status: number;
  message: string;
  fields?: { [field: string]: string };
}
