# Déployer StageTrack sur Render

Un seul service Docker sert Angular et l'API Spring Boot, avec une base PostgreSQL Render.

## À savoir avant de commencer (offre gratuite)

- **La base PostgreSQL gratuite expire 30 jours après sa création** (14 jours de grâce, puis suppression des données). Pour une démonstration c'est suffisant ; sinon il faut passer à une offre payante ou recréer la base.
- **Le service gratuit s'endort après environ 15 minutes sans visite** : le premier chargement suivant prend 30 à 60 secondes.
- **L'envoi d'emails par SMTP est bloqué** (ports 25, 465 et 587) sur les services gratuits. L'application fonctionne quand même : l'inscription réussit et l'erreur d'envoi est seulement écrite dans les journaux. Pour de vrais emails, il faut une offre payante ou un fournisseur d'email par API HTTP.
- Ces règles évoluent : vérifie-les sur <https://render.com/docs/free>.

## 1. Mettre le projet sur GitHub

Dans un terminal, dans le dossier du projet (celui qui contient `Dockerfile`, `backend` et `frontend`) :

```
git init
git add .
git commit -m "StageTrack"
git branch -M main
```

Crée un dépôt vide sur GitHub (sans README), puis :

```
git remote add origin https://github.com/TON_COMPTE/stagetrack.git
git push -u origin main
```

Le fichier `.gitignore` exclut déjà `node_modules`, `target` et `dist`.

## 2. Déployer avec le Blueprint

1. Sur <https://dashboard.render.com>, clique sur **New > Blueprint** et choisis ton dépôt.
2. Render lit `render.yaml` : il prépare la base **stagetrack-db** et le service **stagetrack**.
3. Il te demande la valeur de **ADMIN_PASSWORD** : choisis un mot de passe solide (c'est celui de ton compte administrateur).
4. Clique sur **Apply**. Le premier build dure 5 à 10 minutes (Angular, puis Maven).

## 3. Vérifier

- L'adresse du service est du type `https://stagetrack-xxxx.onrender.com`.
- Ouvre-la : tu arrives sur la page de connexion.
- Connecte-toi avec `admin@stagetrack.local` et le mot de passe que tu as choisi.
- Les comptes de démonstration (`recruteur@stagetrack.local`, `etudiant@stagetrack.local`) existent car `SEED_DEMO=true`. **Pour une mise en ligne publique, passe `SEED_DEMO` à `false`**, car leurs mots de passe sont écrits dans le README.
- Swagger : `https://TON-SERVICE.onrender.com/swagger-ui.html`.

## Si le déploiement échoue

- Onglet **Logs** du service : cherche la première ligne `ERROR`.
- `Connection refused` ou `password authentication failed` : la base n'est pas liée. Vérifie que les variables `DB_HOST`, `DB_NAME`, `DB_USER` et `DB_PASSWORD` existent dans l'onglet *Environment*.
- Build Angular qui échoue : consulte les logs du build. Teste d'abord en local avec `docker build -t stagetrack .`.
- Service « Out of memory » : l'offre gratuite a 512 Mo. Réessaie le déploiement ou passe à une offre supérieure.

## Sans Blueprint (manuel)

1. **New > PostgreSQL** (plan Free). Note *Hostname*, *Port*, *Database*, *Username*, *Password* (adresse interne).
2. **New > Web Service**, dépôt GitHub, **Runtime = Docker**, plan Free, même région que la base.
3. Variables d'environnement : `SPRING_PROFILES_ACTIVE=prod`, `DB_HOST`, `DB_PORT=5432`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (32 caractères aléatoires minimum), `ADMIN_EMAIL`, `ADMIN_PASSWORD`, `SEED_DEMO`.
