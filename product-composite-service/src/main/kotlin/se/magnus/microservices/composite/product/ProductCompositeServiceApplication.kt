package se.magnus.microservices.composite.product

import io.swagger.v3.oas.models.ExternalDocumentation
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Contact
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.info.License
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.ComponentScan
import org.springframework.web.client.RestTemplate

@SpringBootApplication
@ComponentScan("se.magnus")
class ProductCompositeServiceApplication {

	@Value("\${api.common.version}") private lateinit var apiVersion: String
	@Value("\${api.common.title}") private lateinit var apiTitle: String
	@Value("\${api.common.description}") private lateinit var apiDescription: String
	@Value("\${api.common.termsOfService}") private lateinit var apiTermsOfService: String
	@Value("\${api.common.license}") private lateinit var apiLicense: String
	@Value("\${api.common.licenseUrl}") private lateinit var apiLicenseUrl: String
	@Value("\${api.common.externalDocDesc}") private lateinit var apiExternalDocDesc: String
	@Value("\${api.common.externalDocUrl}") private lateinit var apiExternalDocUrl: String
	@Value("\${api.common.contact.name}") private lateinit var apiContactName: String
	@Value("\${api.common.contact.url}") private lateinit var apiContactUrl: String
	@Value("\${api.common.contact.email}")private lateinit var apiContactEmail: String

	/**
	 * Will exposed on $HOST:$PORT/swagger-ui.html
	 *
	 * @return the common OpenAPI documentation
	 */
	@Bean
	fun getOpenApiDocumentation(): OpenAPI = OpenAPI().info(createInfo()).externalDocs(createExternalDoc())

	private fun createExternalDoc(): ExternalDocumentation? = ExternalDocumentation()
		.description(apiExternalDocDesc)
		.url(apiExternalDocUrl)

	private fun createInfo(): Info? = Info().title(apiTitle)
		.description(apiDescription)
		.version(apiVersion)
		.contact(createContact())
		.termsOfService(apiTermsOfService)
		.license(createLicense())

	private fun createContact(): Contact = Contact()
		.name(apiContactName)
		.url(apiContactUrl)
		.email(apiContactEmail)

	private fun createLicense(): License = License()
		.name(apiLicense)
		.url(apiLicenseUrl)

	@Bean
	fun restTemplate(): RestTemplate = RestTemplate()
}

fun main(args: Array<String>) {
	SpringApplication.run(ProductCompositeServiceApplication::class.java, *args)
}