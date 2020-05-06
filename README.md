
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

### Setup with Docker
### #Docker-Compose
A docker-compose file is provided for you. Run `docker-compose up` to start the mysql client and webapp server. Alternatively, you can follow the steps below to start individiual pieces of the web application.

#### Alternative: Manual setup
##### Setting up a network
Create a network to connect MySQL Database and Server App by using the docker network command, like so:  
`docker network create prodiga`
##### MySQL Dockerfile
You can pull the MySQL dockerfile using `docker pull mysql:latest`

Here is an example command of how to run the MySQL Dockerfile:
```
docker run -p 3306:3306 --env MYSQL_USER=prodiga --env 'MYSQL_PASSWORD=SuperGeheimesPassword123NacktNiemand!!' --env MYSQL_DATABASE=prodiga --env MYSQL_ROOT_PASSWORD=root --network prodiga --name db mysql:latest
```
This will create the correct user and database for the server to run. Make sure all needed environment variables are present and the password is properly escaped.

##### Prodiga Server Dockerfile
To start only the server, build the Dockerfile provided, e.g. with  
 `docker image build --tag prodiga_server:1.0 .`   
 while in the root directory. 

You can then start the image. Make sure the database is running before starting the server container.

Here is an example command of how to run the server Dockerfile:
```
docker run -p 8080:8080 -v [Your M2 Directory]:/home/prodiga_user/.m2 -v [Prodiga Server Directory]:/home/prodiga_user/app --name server --network prodiga --env MYSQL_HOST=db prodiga_server:1.0
```
Of course, you have to adapt the mounted volumes accordingly. The first mount is optional, as to not redownload the maven dependencies at every execution. The secound mount, which should point to the maven application of the root directory, must be mounted to the docker directory /home/prodiga_user/app.

The MYSQL_HOST environment variable is used by spring and must point to the name of your database container.

The database must be running before running the server.

##### Potential Problems:

On Windows Home, directories can not be simply mounted using Docker Toolbox.  The following [StackOverflow question](https://stackoverflow.com/questions/57756835/docker-toolbox-volume-mounting-not-working-on-windows-10) explains in detail how to add a directory to the VirtualBox shared directories, in order to properly mount the directory. 
> 1.  In Virtual Box under 'Settings' -> 'Shared Folders' added 'projects' and pointed it to the location I want to mount. In my case
> this is 'D:\projects' (Auto-mount and Make Permanent enabled)
> 2.  Start Docker Quickstart Terminal
> 3.  Type 'docker-machine ssh default' (the VirtualBox VM that Docker uses is called 'default')
> 4.  Go to the root of the VM filesystem, command 'cd /'
> 5.  Switch to the user root by typing 'sudo su'
> 6.  Create the directory you want to use as a mount point. Which in my case is the same as the name of the shared folder in VirtualBox:
> 'mkdir projects'
> 7.  Mount the VirtualBox shared folder by typing 'mount -t vboxsf -o uid=1000,gid=50 projects /projects' (the first 'projects' is the
> VirtualBox shared folder name, the second '/projects' is the directory
> I just created and want to use as the mount point).
> 8.  Now I can add a volume to my Docker file like this: '- /projects/test/www/build/:/var/www/html/' (left side is the /projects
> mount in my VM, the right side is the directory to mount in my docker
> container)
> 9.  Run the command 'docker-compose up' to start using the mount (to be clear: run this command via the Docker Quickstart Terminal outside
> of your SSH session on your local file system where your
> docker-compose.yml file is located).

  
## Setup - Client