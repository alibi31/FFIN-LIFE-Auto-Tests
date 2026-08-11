import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("java")
    id("io.qameta.allure") version "3.0.0"
    id("io.freefair.lombok") version "8.11"
}

group = "life.ffin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

// Настройка кодировки
java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

tasks.withType<Test> {
    systemProperty("file.encoding", "UTF-8")
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")

    testImplementation("io.rest-assured:rest-assured:5.4.0")
    testImplementation("io.rest-assured:json-path:5.4.0")
    testImplementation("io.rest-assured:json-schema-validator:5.4.0")

    testImplementation("org.projectlombok:lombok:1.18.42")

    testImplementation("io.qameta.allure:allure-junit5:2.29.1")
    testImplementation("io.qameta.allure:allure-rest-assured:2.29.1")

    testImplementation("com.microsoft.playwright:playwright:1.50.0")

    testImplementation("org.aeonbits.owner:owner:1.0.12")

    testImplementation("com.deque.html.axe-core:playwright:4.10.2")

    testImplementation("com.squareup.retrofit2:retrofit:2.11.0")
    testImplementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    testImplementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    testImplementation("com.squareup.okhttp3:okhttp-urlconnection:4.11.0")
    testImplementation("org.springframework.data:spring-data-commons:3.3.3")

    testImplementation("io.qameta.allure:allure-okhttp3:2.29.1")

    testImplementation("org.junit.platform:junit-platform-suite-api:1.10.0")
    testRuntimeOnly("org.junit.platform:junit-platform-suite-engine:1.10.0")

    testImplementation("org.junit.platform:junit-platform-launcher:1.10.1")

    // Logging dependencies
    testImplementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")
    // Для отображения логов в консоли Gradle
    testImplementation("org.slf4j:slf4j-simple:2.0.13")

    // Wiremock и Test containers
    testImplementation("org.wiremock:wiremock-standalone:3.0.1")
    testImplementation("org.testcontainers:testcontainers:1.19.0")
    testImplementation("org.testcontainers:postgresql:1.19.0")
    testImplementation("org.testcontainers:junit-jupiter:1.19.0")
    testImplementation("org.awaitility:awaitility:4.2.0")
}

allure {
    version.set("2.34.1")
    adapter {
        frameworks { junit5 {} }
        autoconfigure.set(true)
        autoconfigureListeners.set(true)
        aspectjWeaver.set(true)
        allureJavaVersion.set("2.29.1")
        aspectjVersion.set("1.9.22.1")
    }

    report {
        reportDir.set(project.reporting.baseDirectory.dir("allure-report"))


        singleFile.set(true)
    }
}

tasks.test {
    useJUnitPlatform {
        includeEngines("junit-jupiter", "junit-platform-suite")

        // ./gradlew test -PincludeTags=ffin-api,PA-UI
        if (project.hasProperty("includeTags")) {
            val include = project.property("includeTags")
                .toString()
                .split(',')
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (include.isNotEmpty()) {
                includeTags(*include.toTypedArray())
            }
        }

        // ./gradlew test -PexcludeTags=ffin-api,PA-UI
        if (project.hasProperty("excludeTags")) {
            val exclude = project.property("excludeTags")
                .toString()
                .split(',')
                .map { it.trim() }
                .filter { it.isNotBlank() }

            if (exclude.isNotEmpty()) {
                excludeTags(*exclude.toTypedArray())
            }
        }

    }

//    maxParallelForks = maxOf(1, Runtime.getRuntime().availableProcessors() / 2)

    systemProperty("env", System.getProperty("env", "test"))

    ignoreFailures = true

    finalizedBy("allureReport")
}

tasks.register("serveAfterTest") {
    dependsOn("test")
    finalizedBy("allureServe")
}

// Открыть Inspector и генерить Java-код (сохранит файл куда укажешь, по дефолту в src/test/java/life/ffin/test/generated)
tasks.register<JavaExec>("pwCodegen") {
    group = "playwright"
    description = "Playwright Inspector codegen (Java)"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath

    val url  = project.findProperty("url")  ?: "https://ffin.life"
    val out  = project.findProperty("out")  ?: "src/test/java/life/ffin/test/generated/GeneratedTest.java"

    args("codegen", url, "--target=java", "--test-id-attribute=data-testid", "--output=$out")
}

// Установить браузеры
tasks.register<JavaExec>("pwInstall") {
    group = "playwright"
    description = "Install Playwright browsers (Java CLI)"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath

    args("install")
}

// Сохранить storage state после логина
tasks.register<JavaExec>("pwSaveStorage") {
    group = "playwright"
    description = "Open Inspector and save storage state"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath
    val url = project.findProperty("url") ?: "https://example.com/login"
    val out = project.findProperty("out") ?: "state.json"
    args("codegen", url, "--save-storage=$out")
}

// Дебаг существующих тестов с Inspector
tasks.register<Test>("uiDebug") {
    group = "verification"
    description = "Run JUnit tests with Playwright Inspector"
    useJUnitPlatform()
    environment("PWDEBUG", "1")
    systemProperty("headless", "false")
    testLogging {
        events("FAILED", "SKIPPED", "PASSED")
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}