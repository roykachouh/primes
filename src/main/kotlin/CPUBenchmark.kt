import benchmarks.ConsumeCPU
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.MetricDatum
import com.amazonaws.services.cloudwatch.model.PutMetricDataRequest
import com.amazonaws.services.cloudwatch.model.StandardUnit
import com.amazonaws.services.cloudwatch.model.StatisticSet
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB
import com.amazonaws.services.dynamodbv2.model.AttributeValue
import com.amazonaws.services.dynamodbv2.model.PutItemRequest
import metadata.CPUMetadataSnatcher
import metadata.CPUMetadataSnatcher.CpuMetadata
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.util.ListStatistics
import java.util.*


fun main(args: Array<String>) {
    startKoin(listOf(PrimeModule))

    CPUBenchmarker()
}

class CPUBenchmarker : KoinComponent {

    val cpuMetadataSnatcher by inject<CPUMetadataSnatcher>()
    val dynamo by inject<AmazonDynamoDB>()
    val cloudWatch by inject<AmazonCloudWatch>()

    init {
        val cpuMetadata = cpuMetadataSnatcher.snatch()

        val opt = OptionsBuilder()
                .include(".*" + ConsumeCPU::class.java!!.getSimpleName() + ".*")
                .forks(1)
                .warmupForks(1)
                .warmupIterations(5)
                .measurementIterations(5)
                .build()

        val benchmarkResult = Runner(opt).run()


        persist(benchmarkResult, cpuMetadata)
    }

    fun persist(benchmarkResult: MutableCollection<RunResult>, cpuMetadata: CpuMetadata) {
        benchmarkResult.forEach {
            it.benchmarkResults.forEach {
                it.iterationResults.forEach {
                    val putItemRequest = PutItemRequest()
                            .withTableName("cpu_benchmarks")
                            .addItemEntry("id", AttributeValue(UUID.randomUUID().toString()))
                            .addItemEntry("vendor", AttributeValue(cpuMetadata.vendor ?: "N/A"))
                            .addItemEntry("modelName", AttributeValue(cpuMetadata.modelName ?: "N/A"))
                            .addItemEntry("cores", AttributeValue(cpuMetadata.cores ?: "N/A"))

                    val stats = it.primaryResult.getStatistics() as ListStatistics

                    val putMetricDataRequest = PutMetricDataRequest()
                    putMetricDataRequest.namespace = "sample.large"
                    val metric = MetricDatum()
                    metric.metricName = it.primaryResult.getLabel()
                    metric.unit = StandardUnit.Count.name
                    metric.timestamp = Date()
                    metric.statisticValues = StatisticSet()
                            .withMinimum(stats.min)
                            .withMaximum(stats.max)
                            .withSum(stats.sum)
                            .withSampleCount(stats.n.toDouble())

                    putMetricDataRequest.withMetricData(metric)

                    putItemRequest.addItemEntry("label", AttributeValue(it.primaryResult.getLabel()))
                            .addItemEntry("unit", AttributeValue(it.scoreUnit ?: "N/A"))
                            .addItemEntry("min", AttributeValue(stats.min.toString()))
                            .addItemEntry("max", AttributeValue(stats.max.toString()))
                            .addItemEntry("n", AttributeValue(stats.n.toString()))
                            .addItemEntry("variance", AttributeValue(stats.variance.toString()))
                            .addItemEntry("stdDev", AttributeValue(stats.standardDeviation.toString()))
                            .addItemEntry("p90", AttributeValue(stats.getPercentile(90.0).toString()))
                            .addItemEntry("p95", AttributeValue(stats.getPercentile(95.0).toString()))
                            .addItemEntry("p99", AttributeValue(stats.getPercentile(99.0).toString()))
                    dynamo.putItem(putItemRequest)

                    cloudWatch.putMetricData(putMetricDataRequest)

                }
            }
        }
    }
}


