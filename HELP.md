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