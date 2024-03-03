package se.magnus.microservices.utilities.http

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.net.InetAddress
import java.net.UnknownHostException

@Component
class ServiceUtil @Autowired constructor(@param:Value("\${server.port}") private val port: String) {

    final var serviceAddress: String = String()
        get() {
            if (field.isBlank()) {
                field = findMyHostname() + "/" + findMyIpAddress() + ":" + port
            }
            return field
        }
        private set

    private fun findMyHostname(): String {
        return try {
            InetAddress.getLocalHost().hostName
        } catch (e: UnknownHostException) {
            "unknown host name"
        }
    }

    private fun findMyIpAddress(): String {
        return try {
            InetAddress.getLocalHost().hostAddress
        } catch (e: UnknownHostException) {
            "unknown IP address"
        }
    }
}