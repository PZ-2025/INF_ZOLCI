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
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.mariadb.jdbc:mariadb-java-client:3.3.2")
    implementation("org.liquibase:liquibase-core")
    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")

    implementation ("io.github.cdimascio:dotenv-java:2.3.2")
    implementation ("com.h2database:h2")

    implementation("org.springframework.security:spring-security-crypto")
    testImplementation("org.springframework.security:spring-security-test")

    implementation("jakarta.validation:jakarta.validation-api:3.1.1")
    implementation("javax.validation:validation-api:2.0.1.Final")
    implementation("org.hibernate.validator:hibernate-validator:8.0.1.Final")

    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-logging")

    // Własna biblioteka do raportowania w PDF + itext
    implementation(files("libs/reporting-library-1.0-SNAPSHOT.jar"))
    implementation("com.itextpdf:itextpdf:5.5.13") // zależność do jara lokalnego z libs/
}

tasks.withType<Test> {
    useJUnitPlatform()
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

checkstyle {
    isShowViolations = false
}

tasks.withType<Checkstyle>().configureEach {
    reports {
        xml.required.set(true)
        html.required.set(false)
    }

    logging.captureStandardOutput(org.gradle.api.logging.LogLevel.QUIET)
}

// Custom Tasks
tasks.register("codeQuality") {
    description = "Runs both Checkstyle and SpotBugs checks"
    group = "verification"

    // Make this task depend on checkstyle and spotbugs tasks
    dependsOn("checkstyleMain", "checkstyleTest", "spotbugsMain", "spotbugsTest")
}

tasks.register<JavaExec>("performanceTests") {
    description = "Runs API performance tests for all controllers"
    group = "verification"

    dependsOn("compileTestJava")

    classpath = sourceSets.test.get().runtimeClasspath
    mainClass.set("com.example.backend.performance.PerformanceTestRunner")

    // Ustawienia JVM dla testów wydajności
    jvmArgs = listOf(
        "-Xmx2g",
        "-Xms1g",
        "-XX:+UseG1GC",
        "-Dspring.profiles.active=performance",
        "-Dspring.main.web-application-type=servlet"
    )

    // Argumenty aplikacji
    args = emptyList<String>()

    // Konfiguracja środowiska
    environment(
        "SPRING_PROFILES_ACTIVE" to "performance",
        "REPORTS_STORAGE_PATH" to "./test-reports"
    )

    doFirst {
        println("🚀 Starting BuildTask API Performance Tests...")
        println("📝 Using profile: performance")
        println("🗄️ Database: H2 in-memory")
        println("⏰ This may take several minutes...")
        println("")
    }

    doLast {
        println("")
        println("✅ Performance tests completed!")
        println("📊 Check the console output above for detailed results")
    }
}

// Task do szybkiego uruchamiania testów wydajności
tasks.register("perfTest") {
    description = "Quick alias for performance tests"
    group = "verification"
    dependsOn("performanceTests")
}

// Task do czyszczenia raportów testowych
tasks.register<Delete>("cleanTestReports") {
    description = "Cleans test reports directory"
    group = "verification"
    delete("./test-reports")
}