package benchmark

import PrimeModule
import benchmark.CPUMetadataSnatcher.CpuMetadata
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import utils.Stopwatch
import java.util.*

class CPUBenchmarker : KoinComponent {
    val primeTester by inject<PrimeTester>()
    val cpuMetadataSnatcher by inject<CPUMetadataSnatcher>()
    val dynamo by inject<AmazonDynamoDB>()

    init {
        val cpuMetadata = cpuMetadataSnatcher.snatch()

        val elapsedMillis = Stopwatch.elapse {
            primeTester.run()
        }

        persist(elapsedMillis, cpuMetadata)
    }

    fun persist(elapsedMillis: Long, cpuMetadata: CpuMetadata) {
        val putItemRequest = PutItemRequest()
                .withTableName("cpu_benchmarks")
                .addItemEntry("id", AttributeValue(UUID.randomUUID().toString()))
                .addItemEntry("elapsedMillis", AttributeValue(elapsedMillis.toString()))
                .addItemEntry("vendor", AttributeValue(cpuMetadata.vendor ?: "N/A"))
                .addItemEntry("modelName", AttributeValue(cpuMetadata.modelName ?: "N/A"))
                .addItemEntry("cores", AttributeValue(cpuMetadata.cores ?: "N/A"))

        dynamo.putItem(putItemRequest)
    }
}

fun main(args: Array<String>) {
    startKoin(listOf(PrimeModule))

    CPUBenchmarker()
}