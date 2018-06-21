import benchmarks.ConsumeCPU
import com.amazonaws.services.cloudwatch.AmazonCloudWatch
import com.amazonaws.services.cloudwatch.model.*
import commands.AuxProcessSnatcher
import commands.CPUCoresSnatcher
import commands.CPUModelNameSnatcher
import commands.CPUVendorSnatcher
import io.reactivex.Observable
import models.AuxProcess
import models.CpuMetadata
import org.koin.standalone.KoinComponent
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.inject
import org.openjdk.jmh.results.RunResult
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder
import org.openjdk.jmh.runner.options.TimeValue
import org.openjdk.jmh.util.ListStatistics
import utils.MetricsUtil
import java.lang.System.getenv
import java.util.*
import java.util.concurrent.TimeUnit


class CPUBenchmarker : KoinComponent {
    val RUN_WATCHDOG: Boolean = true
    val EXPORT_METRICS: Boolean = true
    val FORKS = 1
    val WARMUP_FORKS = 1


    companion object {

        @JvmStatic
        fun main(args: Array<String>) {

            startKoin(listOf(mainModule))

            CPUBenchmarker()
        }
    }

    private val cpuVendorSnatcher by inject<CPUVendorSnatcher>()
    private val cpuModelNameSnatcher by inject<CPUModelNameSnatcher>()
    private val cpuCoresSnatcher by inject<CPUCoresSnatcher>()
    private val auxProcessSnatcher by inject<AuxProcessSnatcher>()

    private val cloudWatch by inject<AmazonCloudWatch>()

    init {
        val cpuMetadata = CpuMetadata(
                vendor = cpuVendorSnatcher.snatch().replace("\n", ""),
                cores = cpuCoresSnatcher.snatch().replace("\n", ""),
                modelName = cpuModelNameSnatcher.snatch().replace("\n", "")
        )


        val metricsNamespace = MetricsUtil.sanitizeNamespace(cpuMetadata.modelName)
        val cores = cpuMetadata.cores

        if (RUN_WATCHDOG) {
            kickoffTopWatchdog(metricsNamespace, cores)
        }

        val opt = OptionsBuilder()
                .include(".*" + ConsumeCPU::class.java.simpleName + ".*")
                .forks(FORKS)
                .threads(Runtime.getRuntime().availableProcessors())
                .warmupForks(WARMUP_FORKS)
                .warmupTime(TimeValue(5, TimeUnit.SECONDS))
                .warmupIterations(3)
                .measurementIterations(5)
                .measurementTime(TimeValue(5, TimeUnit.SECONDS))
                .build()

        val benchmarkResult = Runner(opt).run()

        println("Sending cpu metdata to cloudwatch: $cpuMetadata")

        if (EXPORT_METRICS) {
            persist(benchmarkResult, cpuMetadata)
        }

    }

    private fun kickoffTopWatchdog(metricsNamespace: String?, cores: String?) {
        println("Kicking off process watchdog...")

        // every 1 second take a snapshot of the process tree with ps aux and report utilization to cloudwatch
        Observable
                .interval(1, TimeUnit.SECONDS) // TODO environmentalize
                .timeInterval()
                .flatMap {
                    Observable.just(auxProcessSnatcher.snatch())
                }.flatMap {
                    Observable.just(it.split("\n"))
                }.flatMapIterable {
                    it
                }.filter {
                    it.isNotEmpty() && !it.contains("CPU")
                }.flatMap {
                    val cleansed = it.trim().replace("\\s+".toRegex(), ",")
                    val fields = cleansed.split(",")
                    Observable.just(AuxProcess(
                            pid = fields[0],
                            ppid = fields[1],
                            cpuPercent = fields[2].toDouble(),
                            memPercent = fields[3].toDouble(),
                            command = fields[4]
                    ))
                }.doOnError {
                    println("Error occurred in observable sequence for publishing process metrics " + it.message)
                }.subscribe {
                    println("Sending process metrics to cloudwatch: $it")

                    if (EXPORT_METRICS) {
                        persist(it, metricsNamespace, cores)
                    }
                }
    }

    private fun persist(auxProcess: AuxProcess, metricsNamespace: String?, cores: String?) {
        val putMetricDataRequest = PutMetricDataRequest()
        val cpuMetric = MetricDatum()
        val memMetric = MetricDatum()

        putMetricDataRequest.namespace = metricsNamespace + "_cores: " + cores

        val regionDimension = Dimension()
        regionDimension.name = "region"
        regionDimension.value = getenv("region") ?: "us-east-1"

        val configDimension = Dimension()
        configDimension.name = "config"
        configDimension.value = getenv("config") ?: "N/A"

        cpuMetric.metricName = auxProcess.command + "-" + "CPU"
        cpuMetric.unit = StandardUnit.Percent.name
        cpuMetric.timestamp = Date()
        cpuMetric.withDimensions(regionDimension, configDimension)
        cpuMetric.value = auxProcess.cpuPercent

        memMetric.metricName = auxProcess.command + "-" + "MEM"
        memMetric.unit = StandardUnit.Percent.name
        memMetric.timestamp = Date()
        memMetric.withDimensions(regionDimension, configDimension)
        memMetric.value = auxProcess.memPercent

        putMetricDataRequest.withMetricData(cpuMetric, memMetric)

        cloudWatch.putMetricData(putMetricDataRequest)
        println("Completing putting metric $putMetricDataRequest")
    }


    private fun persist(benchmarkResult: MutableCollection<RunResult>, cpuMetadata: CpuMetadata) {
        benchmarkResult.forEach {
            it.benchmarkResults.forEach {
                it.iterationResults.forEach {
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
                    regionDimension.value = getenv("region") ?: "us-east-1"

                    val configDimension = Dimension()
                    configDimension.name = "config"
                    configDimension.value = getenv("config") ?: "N/A"

                    metric.metricName = it.primaryResult.getLabel()
                    metric.unit = StandardUnit.None.name
                    metric.timestamp = Date()
                    metric.withDimensions(regionDimension, configDimension)

                    val statSet = StatisticSet()
                    statSet.minimum = stats.min
                    statSet.maximum = stats.max
                    statSet.sum = stats.sum
                    statSet.sampleCount = stats.n.toDouble()
                    metric.statisticValues = statSet

                    putMetricDataRequest.withMetricData(metric)

                    cloudWatch.putMetricData(putMetricDataRequest)
                }
            }
        }
    }
}


