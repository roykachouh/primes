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

    private val ecs by inject<AmazonECS>()

    data class CpuMemCombo(val cpu: String, val memory: String)

    private val cpuConfigs = listOf(
            CpuMemCombo("0.25 vCPU", "0.5GB"),
            CpuMemCombo("0.25 vCPU", "1GB"),
            CpuMemCombo("0.25 vCPU", "2GB"),
            CpuMemCombo("0.5 vCPU", "1GB"),
            CpuMemCombo("0.5 vCPU", "2GB"),
            CpuMemCombo("0.5 vCPU", "3GB"),
            CpuMemCombo("0.5 vCPU", "4GB"),
            CpuMemCombo("1 vCPU", "2GB"),
            CpuMemCombo("1 vCPU", "3GB"),
            CpuMemCombo("1 vCPU", "4GB"),
            CpuMemCombo("1 vCPU", "5GB"),
            CpuMemCombo("1 vCPU", "6GB"),
            CpuMemCombo("1 vCPU", "7GB"),
            CpuMemCombo("1 vCPU", "8GB"),
            CpuMemCombo("2 vCPU", "4GB"),
            CpuMemCombo("2 vCPU", "5GB"),
            CpuMemCombo("2 vCPU", "6GB"),
            CpuMemCombo("2 vCPU", "7GB"),
            CpuMemCombo("2 vCPU", "8GB"),
            CpuMemCombo("2 vCPU", "9GB"),
            CpuMemCombo("2 vCPU", "10GB"),
            CpuMemCombo("2 vCPU", "11GB"),
            CpuMemCombo("2 vCPU", "12GB"),
            CpuMemCombo("2 vCPU", "13GB"),
            CpuMemCombo("2 vCPU", "14GB"),
            CpuMemCombo("2 vCPU", "15GB"),
            CpuMemCombo("2 vCPU", "16GB"),
            CpuMemCombo("4 vCPU", "8GB")
    )

    init {
        setupEcs()
    }

    private fun setupEcs() {
        val cluster = "benchmark"
        val services = ecs.listServices(ListServicesRequest().withCluster(cluster))

        services.serviceArns.forEach { serviceArn ->
            val serviceName = serviceArn.substring(serviceArn.lastIndexOf("/") + 1)
            try {
                ecs.updateService(UpdateServiceRequest().withService(serviceName).withDesiredCount(0))

                ecs.deleteService(DeleteServiceRequest().withService(serviceName).withCluster(cluster))
            } catch (e: Exception) {
                println("Could not delete service with arn: $serviceName. Reason: ${e.message}")
            }
        }

        cpuConfigs.forEach { combo ->
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
                            .withEnvironment(KeyValuePair().withName("cpu-spec").withValue(combo.cpu))
                            .withEnvironment(KeyValuePair().withName("AWS_ACCESS_KEY").withValue(System.getenv("AWS_ACCESS_KEY")))
                            .withEnvironment(KeyValuePair().withName("AWS_SECRET_KEY").withValue(System.getenv("AWS_SECRET_KEY")))

            val family =
                    "benchmark${combo.cpu.replace(" ", "").replace(".", "_")}_X_" +
                            combo.memory.replace(" ", "").replace(".", "_")

            val registerTaskDefinitionRequest =
                    RegisterTaskDefinitionRequest()
                            .withFamily(family)
                            .withExecutionRoleArn("ecsExecutionRole")
                            .withCpu(combo.cpu)
                            .withMemory(combo.memory)
                            .withRequiresCompatibilities("FARGATE")
                            .withNetworkMode(NetworkMode.Awsvpc)
                            .withContainerDefinitions(image)

            val registerTaskDefinition = ecs.registerTaskDefinition(registerTaskDefinitionRequest)

            val taskArn = registerTaskDefinition.taskDefinition.taskDefinitionArn

            val updateServiceRequest = UpdateServiceRequest()
                    .withService("$family-service")
                    .withForceNewDeployment(true)
                    .withTaskDefinition(taskArn)


//          ecs.updateService(updateServiceRequest)

            val networkConfig = NetworkConfiguration()
                    .withAwsvpcConfiguration(AwsVpcConfiguration()
                            .withSubnets("subnet-abcc5f84")
                            .withAssignPublicIp(AssignPublicIp.ENABLED))


            val service = CreateServiceRequest()
                    .withServiceName("$family-service")
                    .withDesiredCount(1)
                    .withLaunchType(LaunchType.FARGATE)
                    .withCluster("benchmark")
                    .withNetworkConfiguration(networkConfig)
                    .withTaskDefinition(family)

            ecs.createService(service)
        }
    }
}