# Railway Ticketing & Fraud Detection System

A Java desktop app that lets passengers search Canadian rail routes, book and cancel seats, and flags suspicious booking behaviour in real time — modeled on the anomaly-detection workflow I used professionally in railway revenue control.

## Why I built this

Before switching into software development, I spent 12 years at Kazakhstan Railways, most of it in revenue control, reviewing ticketing and revenue data to catch anomalies (duplicate charges, irregular cancellation patterns, mismatched passenger loads) and quantify the financial losses they caused. That work was done in Excel and SQL, one report at a time.

This project rebuilds that analytical instinct as software: instead of a human reviewer flagging irregular patterns after the fact, the application flags them the moment they happen. I built it against a fictional Canadian railway network (station names and long-haul routes inspired by real services like VIA Rail's *The Canadian* and *The Ocean*) so the domain would be relevant to a Canadian employer, but all schedules, fares, and passenger records in this repository are synthetic, see [Data](#data) below.

## Features

- **Passenger self-service** : register, log in, search trains by origin/destination/date, pick a seat from a live seat map, and view or cancel bookings.
- **Admin dashboard** : booking and passenger totals, a fraud-alert queue, and filters by severity and review status.
- **Rule-based fraud detection engine**, applied automatically around every booking and cancellation:
  - **R01 - Duplicate booking**: same passenger books the same schedule twice within an hour.
  - **R02 - Rapid booking**: more than 3 confirmed bookings by one passenger within 10 minutes.
  - **R03 - Excessive cancellations**: more than 2 cancellations by one passenger within 24 hours.
- Every triggered rule writes an auditable `FraudAlert` record an admin can review and mark resolved.

## Tech stack

| Layer       | Technology                                   |
|-------------|-----------------------------------------------|
| Language    | Java 17+ (records, `switch` expressions)      |
| UI          | JavaFX                                        |
| Database    | SQLite via the [sqlite-jdbc](https://github.com/xerial/sqlite-jdbc) driver |
| Persistence | Hand-written DAO classes using JDBC `PreparedStatement`s |
| Build       | Plain Java project (no Maven/Gradle) -> see [How to Run](#how-to-run) |

## Data

This repository ships with an empty schema (`schema.sql`) and an optional demo dataset (`seed.sql`), no real database file is committed. `seed.sql` populates six fictional passenger accounts, one demo admin account, and six fictional Canadian train routes with future-dated schedules, so the app has something to search and book immediately after setup. No real passenger, booking, timetable, or fare data from any railway (Canadian or otherwise) is included, and no data from my previous employer is used anywhere in this project. Station and route names are inspired by real Canadian long-haul rail corridors for realism only.

## Project structure

```
RailwayTicketingSystem/
|--- src/com/railway/
|----------- Main.java         # application entry point
|   |-- config/                # DatabaseConnection (JDBC setup)
|   |-- models/                # Booking, Passenger, Schedule, Train, User, FraudAlert (records)
|   |-- dao/                   # one DAO per table: all SQL lives here
|   |-- fraud/                 # FraudDetectionEngine: the R01/R02/R03 rules
|   |-- ui/                    # JavaFX screens (login, dashboards, booking flow)
|-- resources/styles.css       # JavaFX stylesheet
|-- database/
|   |-- schema.sql             # creates all tables (run this first)
|   |-- seed.sql               # optional demo data (run this second)
|   |-- railway.sqlite         # created locally, NOT committed (see .gitignore)
|-- lib/                       # sqlite-jdbc-3.46.1.3.jar
```

## How to run

**Prerequisites:** JDK 17+, and the [JavaFX SDK](https://openjfx.io/) matching your JDK.

1. Clone the repo and open it in your IDE (Eclipse, IntelliJ, or VS Code with the Java extensions).
2. Add `lib/sqlite-jdbc-3.46.1.3.jar` to the project's build path / classpath.
3. Configure the JavaFX SDK as a library and add these VM arguments (in Eclipse: Run Configurations -> Arguments -> VM arguments):
   ```
   --enable-native-access=javafx.graphics --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls --enable-native-access=ALL-UNNAMED
   ```
   Replace `/path/to/javafx-sdk/lib` with your own JavaFX SDK's `lib` folder. The `--enable-native-access` flags suppress native-access warnings JavaFX triggers on newer JDKs; if your JDK doesn't recognize them, they can be dropped.
4. Create the database (from the project root, with the SQLite CLI, or via DB Browser for SQLite):
   ```
   sqlite3 database/railway.sqlite < database/schema.sql
   ```
   This matches the path `DatabaseConnection.java` expects (`database/railway.sqlite`, relative to wherever you run the app from - normally your project root) and creates all six tables, empty.

   ! **Re-running this command drops and recreates every table** (see the `DROP TABLE IF EXISTS` statements at the top of `schema.sql`), which erases any trains, accounts, or bookings you've already added. Back up `railway.sqlite` first if you want to keep what's in it.
5. Add some data, two options:
   - **Quick start:** load the demo dataset:
     ```
     sqlite3 database/railway.sqlite < database/seed.sql
     ```
    This adds two demo passenger accounts, one demo admin account, five fictional Canadian train routes, and a booking/fraud-alert history that reproduces the exact scenario shown in [SCREENSHOTS.md](SCREENSHOTS.md) - including all three fraud rules (R01, R02, R03) firing against the same demo passenger. Every demo account uses the password **`password123`**:

     | Username     | Password      | Role      |
     |--------------|---------------|-----------|
     | john_snow    | password123   | Passenger |
     | jane_smith   | password123   | Passenger |
     | admin1       | password123   | Admin     |

     Note: the seeded schedules are dated July 2026, matching the screenshots exactly - since the app doesn't validate past dates (see Known Limitations), they'll still load and display normally even after that date has passed, but booking a "new" ticket on them from today's date won't reflect a realistic future trip.

   - **Or, build your own data:** add trains and schedules directly in DB Browser for SQLite (or your own `INSERT` statements), create passenger accounts by running the app and using **Create New Account** (passwords are hashed automatically on registration), and for an admin account, insert a row into `Users` with `role = 'admin'` and a SHA-256 password hash, since there's no registration flow for admin accounts. Generate the hash with a one-off Java snippet (no extra tools needed, since you already have a JDK):
     ```java
     import java.security.MessageDigest;

     public class HashGen {
         public static void main(String[] args) throws Exception {
             MessageDigest md = MessageDigest.getInstance("SHA-256");
             byte[] hash = md.digest("yourpassword".getBytes());
             StringBuilder sb = new StringBuilder();
             for (byte b : hash) sb.append(String.format("%02x", b));
             System.out.println(sb);
         }
     }
     ```
     Run it once, copy the printed hash into the `Users` insert, then delete the file. (If you prefer Python instead: `hashlib.sha256("yourpassword".encode()).hexdigest()`.)
6. Run `Main.java`.

## What I learned

- Translating an informal, spreadsheet-based fraud-review process into explicit, testable rules (`R01`-`R03`) that run automatically instead of after the fact.
- Practical JDBC: parameterized queries end-to-end, `try-with-resources` for connection/statement cleanup, and designing a schema with foreign keys and `CHECK` constraints instead of enforcing rules only in application code.
- JavaFX UI composition - reusable table-column and card builders, shared navbars across screens, and CSS-driven styling instead of inline styles.
- I learned the SQLite/JDBC connection fundamentals from Murach's *Java Programming* and adapted them to this project's schema and DAO structure.

## Known limitations / what I'd improve next

**Fraud & booking rules**
- No past-date validation: a schedule with a departure date in the past can still be booked.
- No overlap detection: a passenger can currently hold confirmed tickets on two different trains whose travel windows overlap.
- No per-schedule ticket cap: nothing stops one passenger from holding multiple seats on the same train/schedule.
- No minimum lead time before departure: a booking can be made seconds before a train leaves.
- No cross-account correlation: the fraud engine only evaluates one passenger's own history; a more realistic version would flag the same phone/email appearing across multiple accounts.
- Alerts don't escalate to account action: repeated high-severity alerts are visible to an admin but don't automatically restrict the passenger.
- No export/reporting: an admin can browse alerts on-screen but can't export bookings or fraud alerts to CSV for offline analysis.

**Technical**
- Passwords are hashed with  SHA-256 for demo simplicity; 
- DAOs use static methods with a new connection per call, which is simple but not easily testable or mockable; an interface-based DAO layer with dependency injection would be a natural next step.
- Fraud rule thresholds are hard-coded constants; making them configurable (per-route or admin-adjustable) would be more realistic.
- No automated tests yet: unit tests around `FraudDetectionEngine` and the DAOs are next on my list.

## Testing

This project was tested manually across all core flows - authentication, booking, cancellation, and all three fraud rules - before publishing. See [TESTING.md](TESTING.md) for the full test matrix.

## Screenshots

**Login**
![Login screen](screenshots/01-login.png)

**Fraud detection - duplicate booking blocked**
![R01 duplicate booking blocked dialog](screenshots/11-r01-blocked.png)

See [SCREENSHOTS.md](SCREENSHOTS.md) for the full walkthrough of every screen.

## License

MIT - see [LICENSE](LICENSE).
