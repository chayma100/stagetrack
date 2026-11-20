import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';
import {
  ApiError, Candidature, Entreprise, EntrepriseDetail, Offre, Page, RoleName,
  Stats, StatutCandidature, User
} from './models';

@Injectable({ providedIn: 'root' })
export class ApiService {

  private base = environment.apiUrl;

  constructor(private http: HttpClient) { }

  // ---------- Entreprises ----------
  getEntreprises(search = '', page = 0, size = 10): Observable<Page<Entreprise>> {
    return this.http.get<Page<Entreprise>>(`${this.base}/entreprises`,
      { params: this.params({ search, page, size }) });
  }
  getEntreprise(id: number): Observable<EntrepriseDetail> {
    return this.http.get<EntrepriseDetail>(`${this.base}/entreprises/${id}`);
  }
  createEntreprise(e: Entreprise): Observable<Entreprise> {
    return this.http.post<Entreprise>(`${this.base}/entreprises`, e);
  }
  updateEntreprise(id: number, e: Entreprise): Observable<Entreprise> {
    return this.http.put<Entreprise>(`${this.base}/entreprises/${id}`, e);
  }
  deleteEntreprise(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/entreprises/${id}`);
  }

  // ---------- Offres ----------
  getOffres(filters: { search?: string; entrepriseId?: number | null; type?: string; statut?: string;
    dureeMax?: number | null; mine?: boolean }, page = 0, size = 10): Observable<Page<Offre>> {
    return this.http.get<Page<Offre>>(`${this.base}/offres`,
      { params: this.params({ ...filters, page, size }) });
  }
  getOffre(id: number): Observable<Offre> {
    return this.http.get<Offre>(`${this.base}/offres/${id}`);
  }
  createOffre(o: Offre): Observable<Offre> {
    return this.http.post<Offre>(`${this.base}/offres`, o);
  }
  updateOffre(id: number, o: Offre): Observable<Offre> {
    return this.http.put<Offre>(`${this.base}/offres/${id}`, o);
  }
  deleteOffre(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/offres/${id}`);
  }
  postuler(offreId: number, commentaire: string): Observable<Candidature> {
    return this.http.post<Candidature>(`${this.base}/offres/${offreId}/candidatures`, { commentaire });
  }
  exportOffres(): Observable<Blob> {
    return this.http.get(`${this.base}/offres/export`, { responseType: 'blob' });
  }

  // ---------- Candidatures ----------
  getMesCandidatures(): Observable<Candidature[]> {
    return this.http.get<Candidature[]>(`${this.base}/candidatures/me`);
  }
  getCandidatures(statut = '', page = 0, size = 10): Observable<Page<Candidature>> {
    return this.http.get<Page<Candidature>>(`${this.base}/candidatures/recues`,
      { params: this.params({ statut, page, size }) });
  }
  updateStatutCandidature(id: number, statut: StatutCandidature): Observable<Candidature> {
    return this.http.put<Candidature>(`${this.base}/candidatures/${id}/statut`, { statut });
  }
  exportCandidatures(): Observable<Blob> {
    return this.http.get(`${this.base}/candidatures/export`, { responseType: 'blob' });
  }

  // ---------- Utilisateurs ----------
  getUsers(page = 0, size = 10): Observable<Page<User>> {
    return this.http.get<Page<User>>(`${this.base}/users`, { params: this.params({ page, size }) });
  }
  updateUserRoles(id: number, roles: RoleName[]): Observable<User> {
    return this.http.put<User>(`${this.base}/users/${id}/roles`, { roles });
  }
  deleteUser(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/users/${id}`);
  }
  getMe(): Observable<User> {
    return this.http.get<User>(`${this.base}/users/me`);
  }
  updateMe(body: { nom: string; prenom: string; tel?: string; newPassword?: string }): Observable<User> {
    return this.http.put<User>(`${this.base}/users/me`, body);
  }

  // ---------- Statistiques ----------
  getStats(): Observable<Stats> {
    return this.http.get<Stats>(`${this.base}/stats`);
  }

  // ---------- Utilitaires ----------
  /** Télécharge un Blob dans le navigateur (export CSV). */
  download(blob: Blob, filename: string): void {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = filename;
    a.click();
    window.URL.revokeObjectURL(url);
  }

  /** Transforme une erreur HTTP en message affichable. */
  toApiError(err: HttpErrorResponse): ApiError {
    if (err.status === 0) {
      return { status: 0, message: 'Serveur injoignable. Vérifiez que le backend est démarré.' };
    }
    const body = err.error || {};
    return { status: err.status, message: body.message || 'Une erreur est survenue', fields: body.fields };
  }

  private params(values: { [key: string]: any }): HttpParams {
    let p = new HttpParams();
    Object.keys(values).forEach(k => {
      const v = values[k];
      if (v !== null && v !== undefined && v !== '') {
        p = p.set(k, String(v));
      }
    });
    return p;
  }
}
