# Voting App

A monochrome Spring Boot voting application with Thymeleaf, Spring Security, Spring Data JPA, and MySQL.

## Requirements

- Java 25
- MySQL 8.0 or later
- A reachable MySQL server and database account

Maven is downloaded automatically the first time you run `mvnw.cmd`.

## Quick start (PowerShell)

1. Start MySQL and make sure a database account can create the `voting_app` database. Set the database username and password to the credentials configured on your own MySQL installation; MySQL does not always use `root` / `root`.
2. Copy `.env.example` to `.env` and update `DB_USERNAME`, `DB_PASSWORD`, and `ADMIN_PASSWORD` for your machine. Spring Boot loads `.env` automatically when the app starts. Keep the quotes off property values. Example:

   ```properties
   DB_URL=jdbc:mysql://127.0.0.1:3306/voting_app?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
   DB_USERNAME=root
   DB_PASSWORD=the-password-you-set-when-installing-mysql
   ADMIN_NAME=Election Administrator
   ADMIN_EMAIL=admin@vote.local
   ADMIN_PASSWORD=your-own-development-admin-password
   ```

   The `.env` file is excluded from Git. If you prefer not to use a file, set these variables in the PowerShell session instead.
3. Run the app from this directory:

   ```powershell
   .\mvnw.cmd spring-boot:run
   ```

   The configured admin password must be at least eight characters. Use your own secret in real environments.
4. Open [http://localhost:8080](http://localhost:8080).

Override the database URL or account with `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD`. Override the seeded administrator with `ADMIN_NAME`, `ADMIN_EMAIL`, and `ADMIN_PASSWORD`. Values in the process environment take precedence over `.env` properties. See `.env.example` for names and example formats.

The first successful start creates the schema and administrator. Campaigns and candidates are added from the admin dashboard. Existing data from the earlier single-election schema is migrated into a `Community Election` campaign.

## Build and run

```powershell
.\mvnw.cmd -B -DskipTests package
java -jar target/voting-app-0.0.1-SNAPSHOT.jar
```

## Project layout

```text
src/main/java/com/voting/
├── config/        Security configuration and initial seed data
├── controller/    Page routes and form submissions
├── entity/        User, campaign, candidate, vote, and role entities
├── repository/    Spring Data JPA repositories
├── service/       Registration, candidate, and transactional vote logic
└── VotingApplication.java

src/main/resources/
├── static/css/    Monochrome application styles
├── static/js/     Small progressive-enhancement helpers
├── templates/     Thymeleaf pages
└── application.properties
```

## Notes

- Voter accounts receive `ROLE_NORMAL`; the configured administrator receives `ROLE_ADMIN`.
- Only the administrator can access `/admin`. Voters can access `/user` and cast one vote per campaign.
- Passwords are BCrypt-encoded. CSRF protection is enabled for all forms, including logout.
- A vote is recorded in one database transaction and a unique database constraint prevents a second ballot from the same voter. The stored `Vote` links a candidate to a voter, so individual choices can be inspected by someone with direct database access; do not describe this implementation as an anonymous or cryptographically secret ballot.
- Campaigns have a name, short description, start date, and end date. Each account can cast one vote per campaign.
- This is an educational demonstration, not a certified public-election system.

## Security configuration

The sample seed password defaults to `admin` for local setup to match the original demonstration workflow. The bootstrap refuses to start until `ADMIN_PASSWORD` is set to at least eight characters. **Use a unique password and change it before exposing the application anywhere outside your local machine.** When a seeded account already exists, its password is not reset on later startups, so change it through an account-management process or reset it manually in the database using a BCrypt hash.

For a local demo using the requested `admin` / `admin` credentials, edit `BootstrapData.java` to remove the eight-character check, set `ADMIN_EMAIL=admin`, and set `ADMIN_PASSWORD=admin`. This intentionally insecure configuration is only suitable for an isolated, disposable demo.
