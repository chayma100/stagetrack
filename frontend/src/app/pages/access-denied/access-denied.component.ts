import { Component } from '@angular/core';

@Component({
  selector: 'app-access-denied',
  template: `
    <div class="header bg-gradient-primary pb-8 pt-5 pt-md-8"></div>
    <div class="container-fluid mt--7">
      <div class="card shadow">
        <div class="card-body text-center py-6">
          <h2>Accès refusé</h2>
          <p class="text-muted">Votre rôle ne vous permet pas d'accéder à cette page ou à cette action.</p>
          <a class="btn btn-primary" [routerLink]="['/dashboard']">Retour au tableau de bord</a>
        </div>
      </div>
    </div>`
})
export class AccessDeniedComponent { }
