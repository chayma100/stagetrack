import { Routes } from '@angular/router';

import { LoginComponent } from '../../pages/login/login.component';
import { ChoixProfilComponent } from '../../pages/choix-profil/choix-profil.component';
import { RegisterComponent } from '../../pages/register/register.component';

export const AuthLayoutRoutes: Routes = [
    { path: 'login',          component: LoginComponent },
    { path: 'choix-profil',   component: ChoixProfilComponent },
    { path: 'register',       component: RegisterComponent }
];
