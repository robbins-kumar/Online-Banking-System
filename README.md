# Online Banking System

## About The Project 

The Online Banking System is a Java-based application that allows
customers to manage their bank accounts and perform basic banking operations.

## Features

- User Registration
- User Login
- Customer Dashboard
- Deposit Money
- Withdraw Money
- Money Transfer
- Check Account Balance
- Transaction History
- Admin Dashboard

## Technologies Used

- Java
- Java Swing
- JDBC
- MySQL

## Project Structure

- `src` - Contains the Java source code
- `database` - Contains database-related files
- `lib` - Contains external libraries/JAR files
- `README.md` - Contains project documentation
- `.gitignore` - contains files and folders that should NOT upload to your repository.

## Database

The project uses MySQL to store:

- Customer information
- Account details
- Transactions
- Login information



## How to Run

### 1. Clone the repository

git clone https://github.com/yourusername/Online-Banking-System.git

### 2. Install requirements

- Java JDK
- MySQL
- VS Code / IntelliJ IDEA

### 3. Setup Database

1. Open MySQL.
2. Open `database/banking.sql`.
3. Execute the SQL file.
4. This will create the required database and tables.

### 4. Configure Database Connection

Open the database connection file and update:

- MySQL username
- MySQL password
- Database name

### 5. Run the Application

Run `Main.java`.

The Banking Management System will start.