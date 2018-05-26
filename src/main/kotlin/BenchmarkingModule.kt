import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.cloudwatch.AmazonCloudWatchClient
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import metadata.CPUMetadataSnatcher
import org.koin.dsl.module.applicationContext

val PrimeModule = applicationContext {
    bean {
        AmazonDynamoDBClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean {
        AmazonCloudWatchClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean { CPUMetadataSnatcher() }
}