import { Component, OnInit } from '@angular/core';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { RoleName, User } from '../../core/models';

interface UserRow {
  user: User;
  admin: boolean;
  collaborateur: boolean;
  etudiant: boolean;
}

@Component({
  selector: 'app-users',
  templateUrl: './users.component.html'
})
export class UsersComponent implements OnInit {

  rows: UserRow[] = [];
  pageNumber = 1;
  readonly size = 10;
  total = 0;
  error = '';
  info = '';
  loading = false;

  constructor(private api: ApiService, public auth: AuthService) { }

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading = true;
    this.api.getUsers(this.pageNumber - 1, this.size).subscribe({
      next: page => {
        this.rows = page.content.map(u => this.toRow(u));
        this.total = page.totalElements;
        this.loading = false;
      },
      error: err => { this.loading = false; this.error = this.api.toApiError(err).message; }
    });
  }

  onPage(page: number): void {
    this.pageNumber = page;
    this.load();
  }

  isMe(u: User): boolean {
    return !!this.auth.currentUser && this.auth.currentUser.id === u.id;
  }

  saveRoles(row: UserRow): void {
    this.error = '';
    this.info = '';
    const roles: RoleName[] = [];
    if (row.admin) { roles.push('ADMIN'); }
    if (row.collaborateur) { roles.push('COLLABORATEUR'); }
    if (row.etudiant) { roles.push('ETUDIANT'); }
    if (roles.length === 0) {
      this.error = 'Un utilisateur doit avoir au moins un rôle.';
      return;
    }
    this.api.updateUserRoles(row.user.id, roles).subscribe({
      next: updated => {
        row.user = updated;
        this.info = `Rôles de ${updated.prenom} ${updated.nom} mis à jour.`;
        if (this.isMe(updated)) {
          this.auth.setUser(updated);
        }
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  remove(row: UserRow): void {
    if (!confirm(`Supprimer ${row.user.prenom} ${row.user.nom} et ses candidatures ?`)) {
      return;
    }
    this.api.deleteUser(row.user.id).subscribe({
      next: () => {
        if (this.rows.length === 1 && this.pageNumber > 1) {
          this.pageNumber--;
        }
        this.load();
      },
      error: err => this.error = this.api.toApiError(err).message
    });
  }

  private toRow(u: User): UserRow {
    return { user: u, admin: u.roles.indexOf('ADMIN') >= 0, collaborateur: u.roles.indexOf('COLLABORATEUR') >= 0,
             etudiant: u.roles.indexOf('ETUDIANT') >= 0 };
  }
}
