import { NgModule } from '@angular/core';
import { RouterModule } from '@angular/router';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { NgbModule } from '@ng-bootstrap/ng-bootstrap';

import { AdminLayoutRoutes } from './admin-layout.routing';
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

@NgModule({
  imports: [
    CommonModule,
    RouterModule.forChild(AdminLayoutRoutes),
    FormsModule,
    NgbModule
  ],
  declarations: [
    AccessDeniedComponent,
    CandidaturesComponent,
    MesCandidaturesComponent,
    DashboardComponent,
    EntrepriseDetailComponent,
    EntrepriseFormComponent,
    EntreprisesComponent,
    OffreDetailComponent,
    OffreFormComponent,
    OffresComponent,
    ProfileComponent,
    UsersComponent
  ]
})
export class AdminLayoutModule {}
