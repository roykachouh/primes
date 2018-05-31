import benchmarks.ConsumeCPU
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.*
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
import org.openjdk.jmh.runner.options.TimeValue
import org.openjdk.jmh.util.ListStatistics
import utils.MetricsUtil
import java.util.*
import java.util.concurrent.TimeUnit


class CPUBenchmarker : KoinComponent {

    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            startKoin(listOf(mainModule))

            CPUBenchmarker()
        }
    }

    private val cpuMetadataSnatcher by inject<CPUMetadataSnatcher>()
    private val dynamo by inject<AmazonDynamoDB>()
    private val cloudWatch by inject<AmazonCloudWatch>()

    init {
        val cpuMetadata = cpuMetadataSnatcher.snatch()

        val opt = OptionsBuilder()
                .include(".*" + ConsumeCPU::class.java.simpleName + ".*")
                .forks(1)
                .threads(Runtime.getRuntime().availableProcessors())
                .warmupForks(1)
                .warmupTime(TimeValue(5, TimeUnit.SECONDS))
                .warmupIterations(3)
                .measurementIterations(5)
                .measurementTime(TimeValue(5, TimeUnit.SECONDS))
                .build()

        val benchmarkResult = Runner(opt).run()


        persist(benchmarkResult, cpuMetadata)
    }

    private fun persist(benchmarkResult: MutableCollection<RunResult>, cpuMetadata: CpuMetadata) {
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

                    val cores = cpuMetadata.cores
                    val cleanNamespace = MetricsUtil.sanitizeNamespace(cpuMetadata.modelName)
                    val putMetricDataRequest = PutMetricDataRequest()
                    val metric = MetricDatum()

                    println("Namespace: $cleanNamespace")
                    println("CPU-Metadata: $cpuMetadata")

                    putMetricDataRequest.namespace = cleanNamespace + "_cores: " + cores

                    val regionDimension = Dimension()
                    regionDimension.name = "region"
                    regionDimension.value = "us-east-1" // TODO externalize

                    metric.metricName = it.primaryResult.getLabel()
                    metric.unit = StandardUnit.None.name
                    metric.timestamp = Date()
                    metric.withDimensions(regionDimension)

                    val statSet = StatisticSet()
                    statSet.minimum = stats.min
                    statSet.maximum = stats.max
                    statSet.sum = stats.sum
                    statSet.sampleCount = stats.n.toDouble()
                    metric.statisticValues = statSet

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


