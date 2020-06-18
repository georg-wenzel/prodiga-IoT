# Prodiga

Made with :beers: in Innsbruck!

## Project Structure

### Prodiga.SQLFileGenerator
Contains a C# solution to generate appropriate system data. For testing purposes, **this does not need to be used**, as the supplied data.sql file is appropriate.

### client
Contains the java code for the Raspberry Pi clients.

### docs
Contains all project documentation, such as timesheets, the concept description and the test script
- `Konzeptbeschreibung.pdf`: Final version of the projects concept
 document.
- Timesheets: Contains the students' timesheets.
- `Testdrehbuch.pdf`: Final version of the test script


### scripts
Contains SQL scripts.
- `create_prodiga_user.sql` is needed to create the `prodiga` user which is the sa account for this project, if you want to set up the database on your local MySQL instance.

### server
Contains the java code for the JSF Web Application.

## Setup - Server

There are two ways to start the server. Either by using `Docker` or `Maven` and `MySql`.  
**We recommend using Docker, as it is simpler, and falling back on manual setup if Docker should not function on your system properly.**

### Setup with Docker
`Dockerfile` and `docker-compose.yml` are located inside the `server` directory. First you need the webapp using `docker build .`. Next you can start everything using `docker-compose up`. It usually takes the server a few tries to start because the DB creates a lot of testdata, which takes a minute or two. However eventually you should see `uibk.ac.at.prodiga.Main : Started Main in xyz seconds` printed to stdout. Now the everything can be accessed from `localhost:8080`.

### Dockerless Server Setup

Make sure you have the following prerequisites installed locally:

- Maven
- JDK11
- MySQL

1. Setup the DB. There are 2 possible ways to do this.
	- Run the script `create_prodiga_user.sql` inside the `scripts` folder on your MySQL instance using the root user.
  This script will create the DB and create a user which will be used by the server to access the DB.
  Verify that you have a `prodiga` DB and a `prodiga` user. You can do this by running `show databases;` and `select * from mysql.user;`
	- Create a DB manually and specify the login data inside the `application.properties` file located in `server/src/main/resources`.
  Here you can specify the user and the password as well as the name of the DB (this can be done by changing the connection string).
  `jdbc:mysql://${MYSQL_HOST:localhost}:3306/<database-name>?useLegacyDatetimeCode=false&serverTimezone=UTC`

2. `cd` into the `server` directory and run `mvn spring-boot:run`. This command installs all dependencies and starts the website.
3. After a while you should see following output on your terminal `uibk.ac.at.prodiga.Main : Started Main in xyz seconds`. The website can now be access on `localhost:8080`.

#### Potential Problems
##### Potential Problems with Dockerless Install
- Database cannot be accessed. This usually menas your connection string is not correct. Check if the MySQL instance is running on port 3306 (which should be default). Also try connect to the DB using the login information inside the `application.properties` file.
- Error while dropping FKxyz. Whenever you restart the website all data will be lost. If there are any changes to the data model the database will be recreated. Hibernate usually tries to drop all tables before recreating them.
  However sometimes the script generated is a bit weird. So try to drop and create the DB manully.

## Setup - Client

For the client setup you should have a Raspberry Pi running raspbian, with a
working internet connection.

### Install Script

#### First Time Install

If you are running the file for the first time, you have to run the
`setup.sh` file found in the directory `client/script`. You have to provide the path to
the root of the client project as a commandline argument.

This script then installs all dependencies and creates a config file under
`$HOME/.config/prodiga/prodigarc` where the path to the project root will be
stored.

Next it compiles the project (provided the right project root was entered) and will start
it.

#### Starting the Client

If you only want to start the client (meaning you have installed all dependencies)
you can run the `start_client.sh` file. It compiles the client if needed and
starts it.

#### Potential Problems
* The dependencies script was interrupted: If this happens try to restart the
  `setup.sh` script. If it won't work, you may have to start from a new
  installation of raspbian.
* Your internet connection broke during downloading: If this happens, try to
  restart the `setup.sh` skript. If it won't work, you may have to start from a
  new installation of raspbian.
* The config file doesn't exist/wasn't created correctly: If this happens, try to
  create it manually under `$HOME/.config/prodiga/prodigarc` and insert a single
  line containing the path to the project root. (Eg `$HOME/Documents/client`).

## Setup - Adding a TimeFlip Cube (Internally: Dice)

Adding a cube to the system requires four steps.

1) Verify the Webapp (Server) is running **first** by following the "Setup - Server" section, starting the Web Application, and navigating 
to the corresponding website, which should show a login prompt.
2) Add a raspberry pi to the system: Run the client setup and start the client.
   If you followed the "Setup - Client" section correctly the script should automatically
   connect to the server. On the web application you should see a new pending raspberry
   pi under "Raspberry Pi/pending". Configure it by confiming the password you
   specified and selecting the room the raspberry pi is in. Now you have added
   the raspberry pi.
3) Add the cube to the system: Put the cube near the running raspberry pi. If
   the cube wasn't added to the system the cube will appear (in max. 2 minutes)
   in the website's "Dices/pending" section. From there we can add it by clicking add
   button. Now the cube is in the "Dices/overview" section where we can assign a
   user to the cube.
4) In order for the cube's placement to be registered as time entries (internally: booking) for the user, 
the user needs to configure the dice first. Log in as the corresponding user the cube
 was assigned to, then navigate to the "Dices/Your Dice" section and follow the **configuration** procedure outlined. 
 By changing the cube's facing while inside the configuration menu, the corresponding table row will light up, allowing you 
 to select a booking category for the cube. After confirming the mapping in the configuration menu, 
 all future changing of the cube's sides will be registered as bookings for the corresponding user.
 
#### Potential Problems
*  **All Table Rows in the Dice Configuration are greyed out:** This is by design. You need to start the **dice configuration** (not editing) procedure once. 
  After doing that, you need to physically change the sides of your TimeFlip Cube while connected to the Raspberry Pi. This allows the client to create a mapping 
  between the real side the cube is laying on, and the side displayed in the web application. When you change the side of your TimeFlip cube, one of the table rows should light up, 
allowing you to configure that side. After saving the configuration, you can then use the **dice editing** procedure to change already mapped sides without physically 
flipping the cube again.