plugins {
    id("java")
}

group = "dev.avelar"
version = "1.0-SNAPSHOT"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

repositories {
    mavenCentral()
}

dependencies {
    // Freemarker template engine
    implementation("org.freemarker:freemarker:2.3.32")

    // Flying Saucer for HTML to PDF conversion
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.5.1")

    // SLF4J for logging (Java 8 compatible version)
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("org.slf4j:slf4j-simple:1.7.36")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
}

