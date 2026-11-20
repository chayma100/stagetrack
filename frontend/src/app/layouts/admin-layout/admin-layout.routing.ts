import { Routes } from '@angular/router';

import { AccessDeniedComponent } from '../../pages/access-denied/access-denied.component';
import { CandidaturesComponent } from '../../pages/candidatures/candidatures.component';
import { MesCandidaturesComponent } from '../../pages/candidatures/mes-candidatures.component';
import { DashboardComponent } from '../../pages/dashboard/dashboard.component';
import { EntrepriseDetailComponent } from '../../pages/entreprises/entreprise-detail.component';
import { EntrepriseFormComponent } from '../../pages/entreprises/entreprise-form.component';
import { EntreprisesComponent } from '../../pages/entreprises/entreprises.component';
import { OffreDetailComponent } from '../../pages/offres/offre-detail.component';
import { OffreFormComponent } from '../../pages/offres/offre-form.component';
import { OffresComponent } from '../../pages/offres/offres.component';
import { ProfileComponent } from '../../pages/profile/profile.component';
import { UsersComponent } from '../../pages/users/users.component';
import { RoleGuard } from '../../core/role.guard';

const ADMIN = { roles: ['ADMIN'] };
const PUBLIER = { roles: ['ADMIN', 'COLLABORATEUR'] };
const POSTULER = { roles: ['ETUDIANT'] };

// L'AuthGuard est appliqué sur la route parente (app.routing.ts).
// Le RoleGuard limite certaines pages selon le rôle, d'après data.roles.
// Attention à l'ordre : 'new' doit précéder ':id'.
export const AdminLayoutRoutes: Routes = [
    { path: 'dashboard',            component: DashboardComponent },

    { path: 'entreprises',          component: EntreprisesComponent },
    { path: 'entreprises/new',      component: EntrepriseFormComponent, canActivate: [RoleGuard], data: PUBLIER },
    { path: 'entreprises/:id/edit', component: EntrepriseFormComponent, canActivate: [RoleGuard], data: ADMIN },
    { path: 'entreprises/:id',      component: EntrepriseDetailComponent },

    { path: 'offres',               component: OffresComponent },
    { path: 'offres/new',           component: OffreFormComponent, canActivate: [RoleGuard], data: PUBLIER },
    { path: 'offres/:id/edit',      component: OffreFormComponent, canActivate: [RoleGuard], data: ADMIN },
    { path: 'offres/:id',           component: OffreDetailComponent },

    { path: 'mes-candidatures',     component: MesCandidaturesComponent, canActivate: [RoleGuard], data: POSTULER },
    { path: 'candidatures',         component: CandidaturesComponent, canActivate: [RoleGuard], data: PUBLIER },

    { path: 'users',                component: UsersComponent, canActivate: [RoleGuard], data: ADMIN },
    { path: 'profile',              component: ProfileComponent },
    { path: 'access-denied',        component: AccessDeniedComponent }
];
