plugins {
    id("java")
    id("maven-publish")
    id("java-library")
    id("signing")
    id("com.gradleup.nmcp") version "0.0.9"
}

group = "dev.avelar"
version = "1.1.0"

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.freemarker:freemarker:2.3.32")
    implementation("org.thymeleaf:thymeleaf:3.1.2.RELEASE")
    implementation("org.xhtmlrenderer:flying-saucer-pdf:9.5.1")
    implementation("org.apache.poi:poi-ooxml:5.4.0")
    implementation("org.jsoup:jsoup:1.18.1")

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

// ============================================================================
// Maven Publication Configuration
// ============================================================================

val projectUrl = providers.gradleProperty("project.url").orNull
val projectName = providers.gradleProperty("project.name").orNull
val projectDescription = providers.gradleProperty("project.description").orNull
val developerId = providers.gradleProperty("developer.id").orNull
val developerName = providers.gradleProperty("developer.name").orNull
val developerEmail = providers.gradleProperty("developer.email").orNull

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])

            pom {
                name.set(projectName ?: "Jambock Reports Engine")
                description.set(projectDescription ?: "A powerful PDF report generation engine using Freemarker templates and Flying Saucer (HTML to PDF)")
                url.set(projectUrl ?: "https://github.com/geovannyAvelar/jambock")

                inceptionYear.set("2024")

                licenses {
                    license {
                        name.set("MIT License")
                        url.set("https://opensource.org/licenses/MIT")
                        distribution.set("repo")
                    }
                }

                developers {
                    developer {
                        id.set(developerId ?: "avelar")
                        name.set(developerName ?: "Giovani Avelar")
                        email.set(developerEmail ?: "github@avelar.dev")
                    }
                }

                scm {
                    val scmUrl = projectUrl ?: "https://github.com/geovannyAvelar/jambock"
                    connection.set("scm:git:${scmUrl}.git")
                    developerConnection.set("scm:git:${scmUrl}.git")
                    url.set(scmUrl)
                }

                issueManagement {
                    system.set("GitHub Issues")
                    url.set("https://github.com/geovannyAvelar/jambock/issues")
                }
            }
        }
    }

    repositories {
        // Local Maven Repository (for testing and local use)
        maven {
            name = "Local"
            url = uri("${layout.buildDirectory}/repo")
        }

        // GitHub Packages
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/geovannyAvelar/jambock")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: findProperty("githubUsername") as String?
                password = System.getenv("GITHUB_TOKEN") ?: findProperty("githubToken") as String?
            }
        }

        // Maven Central publishing is handled by the nmcp plugin below

        // Private Maven Repository (Artifactory, Nexus, etc)
        // Uncomment and configure for your private repository
        /*
        maven {
            name = "PrivateRepo"
            url = uri("https://your-repo.example.com/maven")
            credentials {
                username = findProperty("repoUsername") as String?
                password = findProperty("repoPassword") as String?
            }
        }
        */
    }
}

// ============================================================================
// Signing Configuration
// ============================================================================
signing {
    val gpgKey = System.getenv("GPG_PRIVATE_KEY") ?: findProperty("signing.key") as String?
    val gpgPassphrase = System.getenv("GPG_PASSPHRASE") ?: findProperty("signing.password") as String?
    if (gpgKey != null && gpgPassphrase != null) {
        useInMemoryPgpKeys(gpgKey, gpgPassphrase)
        sign(publishing.publications["mavenJava"])
    }
}

// ============================================================================
// Sonatype Central Portal Publishing (nmcp)
// Uses tokens generated at: central.sonatype.org → Account → Generate User Token
// ============================================================================
nmcp {
    publish("mavenJava") {
        username = System.getenv("SONATYPE_USERNAME") ?: ""
        password = System.getenv("SONATYPE_PASSWORD") ?: ""
        // AUTOMATIC: releases immediately after validation
        // MANUAL: stays in staging for manual review at central.sonatype.com
        publicationType = "AUTOMATIC"
    }
}

