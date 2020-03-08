SEPM-SKEL: Skeleton Project for SEPM

This project provides a starting point for development of projects during the
courses "Software Architecture" and "Softwareentwicklung Engineering". It is a
simple web application offering nearly no "real" functionality. The main purpose
is to help you getting started quickly by providing a suitable starting point.

It utilizes Spring Boot and is configured as maven web application project with:
 - all relevant Spring Framework features enabled
 - embedded Tomcat with support for JSF2
 - embedded H2 in-memory database (including H2 console)
 - support for PrimeFaces
 - basic functionality for user management and web security

Just execute  "mvn spring-boot:run" to start the skeleton project and connect to
http://localhost:8080/ to access the skeleton web application. You may login
with "admin" and "passwd".

Feel free to play around and reuse the whole or parts of this skeleton project
for your own work.