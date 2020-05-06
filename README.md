# Prodiga

Made with :beers: in Innsbruck!

## Structure

### scripts
Contains usefull SQL scripts. 
- `create_prodiga_user.sql` is needed to create the `prodiga` user which is
 the sa account for this project.
 
### docs
Contains all project documentation.
- `Konzeptbeschreibung - Final.pdf`: Final version of the projects concept
 document.
- Timesheets: Contains the students timesheets.
 
 ### src
Actual source code.

## Setup - Server

There are two ways to start the server. Either by using `Docker` or `Maven` and `MySql`.

### Maven + My Sql

Make sure you have the following installed:

- Maven
- JDK11
- MySql

1. Setup the DB. There are 2 possible ways to do this.
	- Run the script `create_prodiga_user.sql` inside the `scripts` folder on your My Sql instance using the root user.
  This script will create the DB and create a user which will be used by the server to access the DB.
  Verify that you have a `prodiga` DB and a `prodiga` user. You can do this by running `show databases;` and `select * from mysql.user;`
	- Create a DB manually and specify the login data inside the `application.properties` file located in `server/src/main/respurces`.
  Here you can specify the user and the password as well as the name of the DB (this can be done by changing the connection string).
  `jdbc:mysql://${MYSQL_HOST:localhost}:3306/<database-name>?useLegacyDatetimeCode=false&serverTimezone=UTC`

2. `cd` into the `server` directory and run `mvn spring-boot:run`. This command installs all dependcies and starts the website.
3. After a while you should see following output on your terminal `uibk.ac.at.prodiga.Main : Started Main in xyz seconds`. The website can now be access on `localhost:8080`.

Follwoing Problems might occur:
- Database cannot be accessed. This usually menas your connection string is not correct. Check if the My Sql instance is running on port 3306 (which should be default). Also try connect to the DB using the login information inside the `application.properties` file.
- Error while dropping FKxyz. Whenever you restart the website all data will be lost. If there are any changes to the data model the database will be recreated. Hibernate usually tries to drop all tables before recreating them.
  However sometimes the script generated is a bit weird. So try to drop and create the DB manully.

## Setup - Client