# Getting Started

### Project Set UP
We created the project with the following gradle command: `gradle init`

Please, have a look into the following references:
* [Building Java Applications with libraries Sample](https://docs.gradle.org/current/samples/sample_building_java_applications_multi_project.html)
* [Sharing build logic between subprojects Sample](https://docs.gradle.org/current/samples/sample_convention_plugins.html)

### Docker
* [Install Docker Engine on Ubuntu](https://docs.docker.com/engine/install/ubuntu/#uninstall-docker-engine)
* [How To Install and Use Docker Compose on Ubuntu 20.04](https://www.digitalocean.com/community/tutorials/how-to-install-and-use-docker-compose-on-ubuntu-20-04)
* [Linux post-installation steps for Docker Engine](https://docs.docker.com/engine/install/linux-postinstall/)
* [Unlock the Full Potential of Docker: Run Docker-Compose without Sudo](https://sujanrajtuladhar.com.np/unlock-the-full-potential-of-docker-run-docker-compose-without-sudo)
* [Docker Cheat Sheet: All the Most Essential Commands in One Place](https://www.hostinger.com/tutorials/docker-cheat-sheet?ppc_campaign=google_search_generic_hosting_all&bidkw=defaultkeyword&lo=20215&gad_source=1&gclid=EAIaIQobChMIw4a4gIPbhAMVjZZoCR2HvQYzEAAYAiAAEgI_z_D_BwE#Clean_Up_Commands)
* [Docker cheatsheet](https://quickref.me/docker.html)

### Persistence
[The Spring official guide](https://github.com/spring-guides/tut-spring-boot-kotlin?tab=readme-ov-file#persistence-with-jpa) 
says we shouldn't use kotlin data class with spring-data-jpa.
>"Here we don’t use data classes with val properties because JPA is not designed to work with immutable classes or the 
methods generated automatically by data classes. If you are using other Spring Data flavor, most of them are designed 
to support such constructs so you should use classes like data class User(val login: String, …​) when using Spring 
Data MongoDB, Spring Data JDBC, etc."

#### Working with Spring Data Repositories
The goal of the Spring Data repository abstraction is to significantly reduce the amount of boilerplate code required to
implement data access layers for various persistence stores. For more details, please have a look into the following 
reference:
* [Working with Spring Data Repositories](https://docs.spring.io/spring-data/data-commons/docs/current/reference/html/#repositories)

#### No-arg compiler plugin
We added the [No-arg plugin](https://kotlinlang.org/docs/no-arg-plugin.html) for generating an additional zero-argument 
constructor for classes with a specific annotation. This allows the Java Persistence API (JPA) to instantiate a class, 
although it doesn't have the zero-parameter constructor from Kotlin or Java point of view.

#### Enabling DuplicateKeyException in MongoDB
We have to add the following spring data configuration: `spring.data.mongodb.auto-index-creation=true`
Because in Spring Data MongoDB, automatic index creation is turned off by default.

#### Enabling the OptimisticLockingFailureException in MySQL
We have to add the following spring data configuration: `spring.jpa.hibernate.ddl-auto=create`
* [How does spring.jpa.hibernate.ddl-auto property exactly work in Spring?](https://stackoverflow.com/questions/42135114/how-does-spring-jpa-hibernate-ddl-auto-property-exactly-work-in-spring)
* [Locking in Spring Boot](https://aurigait.com/blog/locking-in-spring-boot/)