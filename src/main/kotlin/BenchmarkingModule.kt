import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.ecs.AmazonECSClient
import commands.AuxProcessSnatcher
import commands.CPUMetadataSnatcher
import org.koin.dsl.module.applicationContext

val region: String = System.getenv("region") ?: "us-east-1"

val mainModule = applicationContext {
    bean {
        AmazonDynamoDBClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(region)
                .build()
    }

    bean {
        AmazonCloudWatchClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean {
        AmazonECSClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(region)
                .build()
    }

    bean { CPUMetadataSnatcher() }

    bean { AuxProcessSnatcher() }

    bean { region }
}