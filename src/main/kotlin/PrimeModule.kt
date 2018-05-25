import benchmark.CPUMetadataSnatcher
import benchmark.PrimeTester
import com.amazonaws.auth.EnvironmentVariableCredentialsProvider
import com.amazonaws.regions.Regions
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient
import org.koin.dsl.module.applicationContext

val PrimeModule = applicationContext {
    bean {
        AmazonDynamoDBClient
                .builder()
                .withCredentials(EnvironmentVariableCredentialsProvider())
                .withRegion(Regions.US_EAST_1)
                .build()
    }

    bean { PrimeTester(getProperty("primeTester.numPrimes")) }

    bean { CPUMetadataSnatcher() }
}