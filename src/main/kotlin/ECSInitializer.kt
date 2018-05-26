import com.amazonaws.services.ecs.AmazonECS
import com.amazonaws.services.ecs.model.*
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject

fun main(args: Array<String>) {
    startKoin(listOf(mainModule))

    ECSInitializer()
}

class ECSInitializer : KoinComponent {

    val ecs by inject<AmazonECS>()

    val cpuConfigs = mapOf("0.25 vCPU" to "0.5GB",
            "0.5 vCPU" to "1GB",
            "1 vCPU" to "2GB",
            "2 vCPU" to "4GB",
            "4 vCPU" to "8GB")


    init {
        setupEcs()
    }

    fun setupEcs() {

        cpuConfigs.forEach { key, value ->
            val image =
                    ContainerDefinition()
                            .withName("benchmark")
                            .withImage("roykachouh/benchmark")
                            .withLogConfiguration(
                                    LogConfiguration()
                                            .withLogDriver(LogDriver.Awslogs)
                                            .withOptions(mapOf(
                                                    "awslogs-region" to "us-east-1",
                                                    "awslogs-group" to "ecs/benchmark",
                                                    "awslogs-stream-prefix" to "ecs"))
                            )
                            .withEnvironment(KeyValuePair().withName("cpu-spec").withValue(key))
                            .withEnvironment(KeyValuePair().withName("AWS_ACCESS_KEY").withValue(System.getenv("AWS_ACCESS_KEY")))
                            .withEnvironment(KeyValuePair().withName("AWS_SECRET_KEY").withValue(System.getenv("AWS_SECRET_KEY")))

            val family = "benchmark${key.replace(" ", "").replace(".", "_")}"

            val registerTaskDefinitionRequest =
                    RegisterTaskDefinitionRequest()
                            .withFamily(family)
                            .withExecutionRoleArn("ecsExecutionRole")
                            .withCpu(key)
                            .withMemory(value)
                            .withRequiresCompatibilities("FARGATE")
                            .withNetworkMode(NetworkMode.Awsvpc)
                            .withContainerDefinitions(image)

            ecs.registerTaskDefinition(registerTaskDefinitionRequest)

            val networkConfig = NetworkConfiguration()
                    .withAwsvpcConfiguration(AwsVpcConfiguration()
                            .withSubnets("subnet-abcc5f84")
                            .withAssignPublicIp(AssignPublicIp.ENABLED))

            val service = CreateServiceRequest()
                    .withServiceName("$family-service")
                    .withDesiredCount(1)
                    .withLaunchType(LaunchType.FARGATE)
                    .withNetworkConfiguration(networkConfig)
                    .withTaskDefinition(family)

          //  ecs.createService(service)

        }


    }
}