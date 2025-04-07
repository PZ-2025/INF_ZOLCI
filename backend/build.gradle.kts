plugins {
    java
    id("org.springframework.boot") version "3.4.3"
    id("io.spring.dependency-management") version "1.1.7"
    checkstyle
    id("com.github.spotbugs") version "6.1.7"
}

group = "com.example"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    implementation("org.liquibase:liquibase-core")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation ("io.github.cdimascio:dotenv-java:2.3.2")
    implementation ("com.h2database:h2")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

# Ignore Checkstyle output when building app
tasks.withType<Checkstyle> {
    reports {
        html.required.set(true) // Wygeneruj raport HTML
        xml.required.set(false) // Wyłącz raport XML, jeśli nie jest potrzebny
    }
    isIgnoreFailures = true // Opcjonalne: nie przerywaj budowania w przypadku błędów
    showViolations = false // Wyłącz wyświetlanie naruszeń w konsoli
}

spotbugs {
    toolVersion = "4.8.3"
    ignoreFailures = true
}

tasks.withType<com.github.spotbugs.snom.SpotBugsTask> {
    reports {
        create("html") {
            required.set(true)
            // Don't specify stylesheet at all - it will use the default
        }
    }
}

// custom task
tasks.register("codeQuality") {
    description = "Runs both Checkstyle and SpotBugs checks"
    group = "verification"

    // Make this task depend on checkstyle and spotbugs tasks
    dependsOn("checkstyleMain", "checkstyleTest", "spotbugsMain", "spotbugsTest")
}