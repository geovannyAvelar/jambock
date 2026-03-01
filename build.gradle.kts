plugins {
    id("java")
    id("maven-publish")
    id("java-library")
    id("signing")
}

group = "dev.avelar"
version = "1.0.1"

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

        // Maven Central Repository (Sonatype OSSRH - proven stable endpoint)
        maven {
            name = "MavenCentral"
            url = uri(
                if ((version as String).endsWith("SNAPSHOT"))
                    "https://s01.oss.sonatype.org/content/repositories/snapshots/"
                else
                    "https://s01.oss.sonatype.org/service/local/staging/deploy/maven2/"
            )
            credentials {
                username = System.getenv("SONATYPE_USERNAME") ?: findProperty("sonatypeUsername") as String?
                password = System.getenv("SONATYPE_PASSWORD") ?: findProperty("sonatypePassword") as String?
            }
        }

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

