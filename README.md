# Web Dynamique Framework

This project demonstrates a custom MVC framework using Java Servlets and annotations.

## Project Structure

- `framework/` - Contains the custom framework code (JAR packaging)
  - `src/main/java/com/framework/annotation/` - Custom `@Controller` annotation
  - `src/main/java/com/framework/util/` - Utility class for scanning controllers (`ClasseUtilitaire`)
  - `src/main/java/com/framework/servlet/` - Front controller servlet (`FrontControllerServlet`)
  - `pom.xml` - Maven configuration for the framework

- `testapp/` - Contains a test web application that uses the framework (WAR packaging)
  - `src/main/java/com/app/controller/` - Sample controller classes annotated with `@Controller`
  - `src/main/webapp/WEB-INF/` - Web application descriptor (`web.xml`)
  - `pom.xml` - Maven configuration for the test application

- `deployFramework.sh` - Script to build the framework JAR and copy to Tomcat webapps
- `deployTestApplication.sh` - Script to build the test application WAR and copy to Tomcat webapps

## Build Steps

### Prerequisites
- Java JDK 17 or higher
- Maven 3.6 or higher
- Tomcat 10+ (for Jakarta Servlet 6.0)

### Building the Framework
```bash
cd framework
mvn clean package
```
This produces `framework/target/framework-1.0-SNAPSHOT.jar`.

### Building the Test Application
```bash
cd testapp
mvn clean package
```
This produces `testapp/target/testapp.war`. The framework JAR is included in the WAR's `WEB-INF/lib` directory.

## Deployment on Tomcat

### Using Deployment Scripts
1. Set the Tomcat directory in the deployment scripts (default: `/opt/tomcat`)
2. Run the deployment scripts:
   ```bash
   ./deployFramework.sh   # Copies framework JAR to $TOMCAT/webapps/
   ./deployTestApplication.sh   # Copies testapp WAR to $TOMCAT/webapps/
   ```

### Manual Deployment
1. Copy `framework/target/framework-1.0-SNAPSHOT.jar` to `$TOMCAT/webapps/` (optional, for reference)
2. Copy `testapp/target/testapp.war` to `$TOMCAT/webapps/`
3. Start or restart Tomcat
4. Access the application at `http://localhost:8080/testapp/`

## How It Works
- The `FrontControllerServlet` is mapped to all requests (`/*`)
- On initialization, it scans the `com.app.controller` package for classes annotated with `@Controller`
- It stores the annotation values (e.g., "home", "user")
- For each request, it prints the requested URI and the list of controller names

## Sample Controllers
- `HomeController` annotated with `@Controller("home")`
- `UserController` annotated with `@Controller("user")`

## Notes
- The framework uses the Reflections library for scanning annotations
- The test application depends on the framework module
- Deployment scripts assume Tomcat is installed at `/opt/tomcat`; modify the scripts if needed