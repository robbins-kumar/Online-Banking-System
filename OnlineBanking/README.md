# Online Banking System (Java Swing + JDBC + MySQL)

## Features
- Customer registration (auto-creates a bank account) and login with salted SHA-256 password hashes
- Dashboard: balance, account info, deposit, withdraw, fund transfer, transaction history, change password
- Admin panel: view all accounts, freeze/unfreeze accounts, view all transactions, total deposits
- All SQL uses `PreparedStatement` (no SQL injection)
- Deposits/withdrawals/transfers run inside DB transactions with row locks (`SELECT ... FOR UPDATE`);
  a transfer debits and credits atomically (commit or rollback)

## Setup
1. Install JDK 11+ and MySQL 8.
2. Create the schema:  `mysql -u root -p < sql/schema.sql`
3. Download **MySQL Connector/J** (the "Platform Independent" ZIP from
   https://dev.mysql.com/downloads/connector/j/) and put `mysql-connector-j-x.x.x.jar` in the `lib/` folder.
4. Edit `db.properties` with your MySQL username/password.
5. Build and run:
   - Linux/macOS:  `./run.sh`
   - Windows:      `run.bat`

Default admin (created on first start): **admin@bank.com / admin123**
Customers register themselves from the login screen.

## Using an IDE (IntelliJ / Eclipse / NetBeans)
Create a new Java project, set `src/` as the source folder, add the Connector/J jar to the
classpath/libraries, and run `bank.Main`. Keep `db.properties` in the project's working directory.

## Structure
```
sql/schema.sql         database schema
db.properties          DB url / user / password
src/bank/Main.java     entry point
src/bank/db            DBConnection
src/bank/util          PasswordUtil (salted hashing)
src/bank/model         User, Account, Transaction
src/bank/dao           UserDAO, AccountDAO, TransactionDAO (all SQL)
src/bank/ui            LoginFrame, RegisterDialog, DashboardFrame, AdminFrame, UIUtil
```
