# Spring MVC-like Framework and Test Web Application

This repository contains two separate Maven projects:

1. **framework** - A minimal framework equivalent to Spring MVC that produces a JAR containing `FrontControllerServlet`.
2. **test-app** - A simple web application (WAR) that depends on the framework JAR and deploys the servlet in Tomcat.

## Structure

```
framework/
├── framework/                 # Framework project (produces spring-mvc-framework.jar)
│   ├── pom.xml
│   └── src/main/java/com/example/framework/FrontControllerServlet.java
└── test-app/                  # Test web application (produces test-webapp.war)
    ├── pom.xml
    └── src/main/webapp/
        ├── WEB-INF/web.xml
        └── index.jsp
```

## Framework (`FrontControllerServlet`)

- **doGet** → delegates to `processRequest` (default implementation returns a simple HTML page).
- **doPost** → writes the request URL to the response as plain text.

You can extend `FrontControllerServlet` and override `processRequest` to add custom logic (e.g., routing to controllers).

## Building

### 1. Build the framework JAR

```bash
cd framework
mvn clean install
```

This will install `spring-mvc-framework-1.0-SNAPSHOT.jar` into your local Maven repository.

### 2. Build the test webapp WAR

```bash
cd test-app
mvn clean package
```

The resulting `test-webapp.war` will be placed in `target/`.

## Deployment

Deploy the generated `test-webapp.war` to an Apache Tomcat server (version 9.0+ recommended, as it implements Servlet 4.0).

After deployment, access the application at:

```
http://localhost:8080/test-webapp/
```

- Click the link to test a GET request.
- Use the form to test a POST request.

## Notes

- The framework and test app are in separate workspaces as requested.
- The test app does **not** contain the framework source; it only depends on the compiled JAR.
- Ensure your Tomcat is configured to use JDK 11 or higher (matching the Maven compiler settings).
