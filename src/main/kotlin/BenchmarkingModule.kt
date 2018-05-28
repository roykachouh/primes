import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import com.amazonaws.services.ecs.AmazonECSClient
import metadata.CPUMetadataSnatcher
import org.koin.dsl.module.applicationContext
import java.lang.System.getenv

val mainModule = applicationContext {
    bean {
        AmazonDynamoDBClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean {
        var region: String = System.getenv("region") ?: "us-east-1"

        AmazonCloudWatchClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(getenv(region))
                .build()
    }

    bean {
        AmazonECSClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean { CPUMetadataSnatcher() }
}