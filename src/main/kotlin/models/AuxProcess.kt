package models

data class AuxProcess(val pid: String,
                      val ppid: String,
                      val cpuPercent: Double,
                      val memPercent: Double,
                      val command: String?
)