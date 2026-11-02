import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    java
    id("io.qameta.allure") version "3.0.0"
    id("io.freefair.lombok") version "8.11"
}

group = "life.ffin"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

// Плагин freefair сам подключает Lombok и annotationProcessor — версию задаём здесь
lombok {
    version.set("1.18.42")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

dependencies {
    // BOM-ы выравнивают версии внутри семейств артефактов
    testImplementation(platform("org.junit:junit-bom:5.11.4"))
    testImplementation(platform("io.qameta.allure:allure-bom:2.29.1"))
    testImplementation(platform("org.testcontainers:testcontainers-bom:1.19.0"))

    // JUnit
    testImplementation("org.junit.jupiter:junit-jupiter")
    // launcher нужен при компиляции: CleanupAfterAllTestsListener реализует TestExecutionListener.
    // Версию берёт из BOM, отдельную (1.10.1) не указывать.
    testImplementation("org.junit.platform:junit-platform-launcher")

    // API: REST Assured (json-path приходит транзитивно)
    testImplementation("io.rest-assured:rest-assured:5.5.0")
    testImplementation("io.rest-assured:json-schema-validator:5.5.0")

    // API: Retrofit / OkHttp
    testImplementation("com.squareup.retrofit2:retrofit:2.11.0")
    testImplementation("com.squareup.retrofit2:converter-jackson:2.11.0")
    testImplementation("com.squareup.retrofit2:converter-scalars:2.11.0")
    testImplementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.17.2")
    testImplementation("com.squareup.okhttp3:okhttp:4.11.0")
    testImplementation("com.squareup.okhttp3:logging-interceptor:4.11.0")
    testImplementation("com.squareup.okhttp3:okhttp-urlconnection:4.11.0")
    testImplementation("org.springframework.data:spring-data-commons:3.3.3")

    // UI
    testImplementation("com.microsoft.playwright:playwright:1.50.0")
    testImplementation("com.deque.html.axe-core:playwright:4.10.2")

    // Конфигурация
    testImplementation("org.aeonbits.owner:owner:1.0.12")

    // Allure (версии из allure-bom)
    testImplementation("io.qameta.allure:allure-junit5")
    testImplementation("io.qameta.allure:allure-rest-assured")
    testImplementation("io.qameta.allure:allure-okhttp3")

    // Логирование: ровно один SLF4J-провайдер — logback
    testImplementation("org.slf4j:slf4j-api:2.0.13")
    testImplementation("ch.qos.logback:logback-classic:1.5.6")

    // WireMock и Testcontainers (версии из testcontainers-bom)
    testImplementation("org.wiremock:wiremock-standalone:3.0.1")
    testImplementation("org.testcontainers:testcontainers")
    testImplementation("org.testcontainers:postgresql")
    testImplementation("org.testcontainers:junit-jupiter")

    // Гигиена тестов
    testImplementation("org.awaitility:awaitility:4.2.2")
    testImplementation("com.tngtech.archunit:archunit-junit5:1.3.0")
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

// "-PincludeTags=ffin-api,PA-UI" -> [ffin-api, PA-UI]
fun csvProperty(name: String): List<String> =
    providers.gradleProperty(name).orNull
        ?.split(',')
        ?.map(String::trim)
        ?.filter(String::isNotBlank)
        ?: emptyList()

// Общие настройки для всех Test-задач (test, uiDebug)
tasks.withType<Test>().configureEach {
    // Java 17 на Windows по умолчанию cp1251 — принудительно UTF-8 для JVM с тестами
    defaultCharacterEncoding = "UTF-8"
    jvmArgs("-Dsun.stdout.encoding=UTF-8", "-Dsun.stderr.encoding=UTF-8")
    systemProperty("env", providers.systemProperty("env").getOrElse("test"))

    useJUnitPlatform {
        includeEngines("junit-jupiter")

        // Основной способ: ./gradlew test -Ptags="smoke & !flaky"
        providers.gradleProperty("tags").orNull?.let { includeTags(it) }

        // Обратная совместимость с CI: -PincludeTags=ffin-api,PA-UI / -PexcludeTags=...
        csvProperty("includeTags").takeIf { it.isNotEmpty() }?.let { includeTags(*it.toTypedArray()) }
        csvProperty("excludeTags").takeIf { it.isNotEmpty() }?.let { excludeTags(*it.toTypedArray()) }
    }

    ignoreFailures = false
}

tasks.test {
    useJUnitPlatform {
        excludeTags("manual", "quarantine")
    }

    finalizedBy("allureReport") // отчёт строится и при упавших тестах

    testLogging {
        events("failed", "skipped")
        exceptionFormat = TestExceptionFormat.FULL
        showStackTraces = true
    }
}

tasks.register("serveAfterTest") {
    dependsOn("test")
    finalizedBy("allureServe")
}

// Дебаг тестов с Playwright Inspector: ./gradlew uiDebug -Ptags=PA-UI --tests "*LoginTest"
tasks.register<Test>("uiDebug") {
    group = "verification"
    description = "Run JUnit tests with Playwright Inspector"

    testClassesDirs = sourceSets.test.get().output.classesDirs
    classpath = sourceSets.test.get().runtimeClasspath

    environment("PWDEBUG", "1")
    systemProperty("headless", "false")

    testLogging {
        events("FAILED", "SKIPPED", "PASSED")
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
}

// --- Playwright CLI ---

// Codegen: сгенерированный код — черновик, кладём в build/, а не в src/
tasks.register<JavaExec>("pwCodegen") {
    group = "playwright"
    description = "Playwright Inspector codegen (Java)"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath

    val url = providers.gradleProperty("url").getOrElse("https://ffin.life")
    val out = providers.gradleProperty("out")
        .getOrElse(layout.buildDirectory.file("playwright/GeneratedTest.java").get().asFile.path)

    args("codegen", url, "--target=java", "--test-id-attribute=data-testid", "--output=$out")
}

tasks.register<JavaExec>("pwInstall") {
    group = "playwright"
    description = "Install Playwright browsers (Java CLI)"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath
    args("install")
}

// storage state содержит cookies/токены сессии — только в build/ и никогда в git
tasks.register<JavaExec>("pwSaveStorage") {
    group = "playwright"
    description = "Open Inspector and save storage state"
    mainClass.set("com.microsoft.playwright.CLI")
    classpath = sourceSets.test.get().runtimeClasspath

    val url = providers.gradleProperty("url").getOrElse("https://example.com/login")
    val out = providers.gradleProperty("out")
        .getOrElse(layout.buildDirectory.file("playwright/state.json").get().asFile.path)

    args("codegen", url, "--save-storage=$out")
}