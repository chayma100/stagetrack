# StageTrack

Application de gestion des **entreprises**, des **offres de stage** (PFE, été) et des **candidatures**.
Projet de la formation FullStack Spring Boot et Angular (cahier des charges « Projects Manager »), avec un autre domaine métier.

| CDC (Projects Manager) | StageTrack |
| --- | --- |
| User, Role (Many-to-Many) | User, Role : Admin, Collaborateur (recruteur) et Etudiant (rôle ajouté) |
| Client | Entreprise |
| Projet (idClient) | Offre (entreprise_id) |
| Admin : tout le CRUD / Collaborateur : consulter et ajouter | Identique, sur les entreprises et les offres |
| (ajout) | Candidature, dashboard, recherche et filtres, pagination, export CSV, profil, Swagger |

## Contenu du dossier

```
stagetrack/
├── backend/                  Spring Boot 3 (Java 17), package com.ensi.stagetrack
│   ├── pom.xml               produit psm.jar
│   └── src/main/...          model, repository, service, controller, security (JWT), dto, exception, config
├── frontend/                 Angular 14 basé sur le template Argon Dashboard (MIT)
│   └── src/app/core          services, AuthGuard, RoleGuard, AuthInterceptor
├── postman/                  collection Postman (29 requêtes, avec tests)
├── Dockerfile                build complet pour Render
├── docker-compose.yml        MySQL + Mailpit pour le développement (facultatif)
├── build-psm.ps1 / .sh       construit psm.jar (Angular dans static/)
└── README.md
```

## 1. Mettre le projet en local

1. Décompresser `stagetrack.zip` dans un dossier **sans espace ni accent**, par exemple `C:\dev\stagetrack`.
2. Installer ce qui manque :
   - **JDK 17 ou plus récent** (`java -version`) ;
   - **Maven** : inutile si tu utilises Spring Tool Suite (il embarque le sien) ;
   - **MySQL** (par exemple XAMPP, onglet MySQL, bouton Start) ;
   - **Node.js 18 ou plus récent** (`node -v`) pour Angular.
3. (Facultatif) `git init` dans le dossier pour versionner ; le `.gitignore` est déjà prêt.

## 2. Ouvrir et lancer le backend (Spring Tool Suite)

1. **File > Import… > Maven > Existing Maven Projects**, puis *Root Directory* = le dossier `backend`. Terminer, puis attendre le téléchargement des dépendances (barre de progression en bas à droite, internet nécessaire).
2. Vérifier que STS utilise un JDK : *Window > Preferences > Java > Installed JREs*.
3. Démarrer MySQL. La base `stagetrack` est créée toute seule au premier lancement.
4. Clic droit sur `StageTrackApplication.java` > **Run As > Spring Boot App**.
5. Si ton utilisateur `root` MySQL a un mot de passe (XAMPP : aucun par défaut) : *Run > Run Configurations > Spring Boot App > StageTrackApplication > onglet Environment*, ajouter `DB_PASSWORD` = ton mot de passe. Autre solution : modifier `src/main/resources/application-dev.properties`.
6. Quand la console affiche `Started StageTrackApplication`, tester :
   - Swagger : <http://localhost:8080/swagger-ui.html>
   - Compte admin créé au premier démarrage : `admin@stagetrack.local` / `Admin123!`
   - Des entreprises et offres de démonstration sont créées (profil `dev`).

Autres IDE : IntelliJ (ouvrir `backend/pom.xml`) ou en terminal `cd backend` puis `mvn spring-boot:run`.

## 3. Lancer le frontend (Angular)

Dans un terminal, dans le dossier `frontend` (ou ouvrir ce dossier dans VS Code) :

```
npm install --legacy-peer-deps
npm start
```

Ouvrir <http://localhost:4200>. En développement, Angular appelle l'API sur `http://localhost:8080/api` (fichier `src/environments/environment.ts`).

- Se connecter avec un compte de démonstration (voir section 6), ou créer un compte via **Inscription** (Étudiant ou Recruteur).
- Les emails d'inscription partent vers `ADMIN_EMAIL`. Sans serveur SMTP, l'inscription fonctionne quand même et un avertissement apparaît dans la console du backend. Pour voir les emails en local : `docker compose up -d` lance **Mailpit** (<http://localhost:8025>) et MySQL.

## 4. Tester l'API avec Postman (étapes 3, 6 et 7 du CDC)

Importer `postman/StageTrack.postman_collection.json`, puis exécuter les dossiers dans l'ordre (ou le *Collection Runner*). Le dossier 6 vérifie les codes 401 (sans token) et 403 (Collaborateur sur une route Admin).

## 5. Construire psm.jar (Angular dans le jar)

```
.\build-psm.ps1          (PowerShell)     ou     ./build-psm.sh
java -jar backend\target\psm.jar
```

Puis ouvrir <http://localhost:8080> : Spring Boot sert Angular et l'API sur le même port. Le routage Angular est en mode hash (`#/offres`), donc aucune redirection serveur n'est nécessaire.

## 6. Rôles et droits

- **Admin** : tout le CRUD, gestion des utilisateurs.
- **Collaborateur (recruteur)** : consulter et **ajouter** des entreprises et des offres (règle du CDC), voir et traiter les candidatures reçues **sur ses propres offres**.
- **Etudiant** : consulter les entreprises et les offres, **postuler**, suivre ses candidatures.

| Action | Admin | Collaborateur | Etudiant |
| --- | --- | --- | --- |
| Consulter entreprises et offres | oui | oui | oui |
| Ajouter une entreprise ou une offre | oui | oui | non |
| Modifier / supprimer | oui | non | non |
| Postuler, voir « Mes candidatures » | oui | non | oui |
| Candidatures reçues et changement de statut | toutes | celles de ses offres | non |
| Export CSV, gestion des utilisateurs | oui | non | non |

À l'inscription, le choix « Étudiant » ou « Recruteur » donne le rôle correspondant ; le rôle Admin ne peut pas être choisi, seul un Admin peut l'attribuer (page Utilisateurs).

Comptes de démonstration créés au démarrage en profil `dev` :

| Rôle | Email | Mot de passe |
| --- | --- | --- |
| Admin | `admin@stagetrack.local` | `Admin123!` |
| Collaborateur | `recruteur@stagetrack.local` | `Recruteur123!` |
| Etudiant | `etudiant@stagetrack.local` | `Etudiant123!` |

Les droits sont appliqués côté Spring (`@PreAuthorize`) **et** côté Angular (menus, boutons, `RoleGuard`). Seul le côté Spring est une vraie protection.

## CV joint à la candidature

Lors d'une candidature, l'étudiant peut joindre un CV (PDF, DOC ou DOCX, 5 Mo maximum). Le fichier est stocké en base (table `candidature_cv`). Il peut être téléchargé par l'étudiant auteur, par le recruteur propriétaire de l'offre et par l'admin (`GET /api/candidatures/{id}/cv`). L'envoi se fait en `multipart/form-data` sur `POST /api/offres/{id}/candidatures` (champs `commentaire` et `cv`).

## 7. Déploiement sur Render

Voir **DEPLOIEMENT.md** (guide pas à pas, `render.yaml` prêt à l'emploi, limites de l'offre gratuite).

## Port MySQL

En développement, le backend se connecte à MySQL sur le port **3307** par défaut (variable `DB_PORT`). Si ton MySQL utilise 3306 (cas courant), ajoute la variable `DB_PORT=3306` (STS : *VM arguments* : `-DDB_PORT=3306`) ou modifie `application-dev.properties`.

## Dépannage

- **`Access denied for user 'root'`** : renseigner `DB_PASSWORD` (voir plus haut).
- **`Communications link failure`** : MySQL n'est pas démarré.
- **Angular affiche « Serveur injoignable »** : le backend n'est pas lancé sur le port 8080.
- **Erreurs `npm install`** : ajouter `--legacy-peer-deps` ; avec Node très récent, des avertissements sont normaux.
- **Port 8080 occupé** (par exemple Oracle) : lancer avec la variable `PORT=8081` et adapter `apiUrl` dans `environment.ts`.
