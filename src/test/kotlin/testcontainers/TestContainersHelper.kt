package testcontainers

import org.testcontainers.containers.Network

class TestContainersHelper {
    companion object {
        private val network = Network.newNetwork()

        val postgresContainer = PostgrestContainerHelper(network = network)
    }
}