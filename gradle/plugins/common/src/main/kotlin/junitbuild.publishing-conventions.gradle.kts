plugins {
	`maven-publish`
	signing
	id("junitbuild.base-conventions")
	id("junitbuild.build-parameters")
}

val isSnapshot = project.version.toString().contains("SNAPSHOT")

val jupiterProjects: List<Project> by rootProject
val platformProjects: List<Project> by rootProject
val vintageProjects: List<Project> by rootProject

when (project) {
	in jupiterProjects -> {
		group = property("jupiterGroup")!!
	}
	in platformProjects -> {
		group = property("platformGroup")!!
		version = property("platformVersion")!!
	}
	in vintageProjects -> {
		group = property("vintageGroup")!!
		version = property("vintageVersion")!!
	}
}

// ensure project is built successfully before publishing it
tasks.withType<PublishToMavenRepository>().configureEach {
	dependsOn(provider {
		val tempRepoName: String by rootProject
		if (repository.name != tempRepoName) {
			listOf(tasks.build)
		} else {
			emptyList()
		}
	})
}
tasks.withType<PublishToMavenLocal>().configureEach {
	dependsOn(tasks.build)
}

signing {
	useGpgCmd()
	sign(publishing.publications)
	isRequired = !(isSnapshot || buildParameters.ci)
}

tasks.withType<Sign>().configureEach {
	val isSnapshot = project.version.toString().contains("SNAPSHOT")
	onlyIf {
		!isSnapshot // Gradle Module Metadata currently does not support signing snapshots
	}
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			pom {
				name.set(provider {
					project.description ?: "${project.group}:${project.name}"
				})
				url.set("https://junit.org/junit5/")
				scm {
					connection.set("scm:git:git://github.com/junit-team/junit5.git")
					developerConnection.set("scm:git:git://github.com/junit-team/junit5.git")
					url.set("https://github.com/junit-team/junit5")
				}
				licenses {
					license {
						val license: License by rootProject.extra
						name.set(license.name)
						url.set(license.url.toString())
					}
				}
				developers {
					developer {
						id.set("bechte")
						name.set("Stefan Bechtold")
						email.set("stefan.bechtold@me.com")
					}
					developer {
						id.set("jlink")
						name.set("Johannes Link")
						email.set("business@johanneslink.net")
					}
					developer {
						id.set("marcphilipp")
						name.set("Marc Philipp")
						email.set("mail@marcphilipp.de")
					}
					developer {
						id.set("mmerdes")
						name.set("Matthias Merdes")
						email.set("matthias.merdes@heidelpay.com")
					}
					developer {
						id.set("sbrannen")
						name.set("Sam Brannen")
						email.set("sam@sambrannen.com")
					}
					developer {
						id.set("sormuras")
						name.set("Christian Stein")
						email.set("sormuras@gmail.com")
					}
					developer {
						id.set("juliette-derancourt")
						name.set("Juliette de Rancourt")
						email.set("derancourt.juliette@gmail.com")
					}
				}
			}
		}
	}
}
