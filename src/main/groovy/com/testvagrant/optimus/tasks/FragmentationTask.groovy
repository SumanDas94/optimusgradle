package com.testvagrant.optimus.tasks

import com.testvagrant.optimus.extensions.OptimusExtension
import com.testvagrant.optimus.extensions.OptimusServiceExtension
import com.testvagrant.optimus.utils.OptimusHelper
import com.testvagrant.optimus.utils.OptimusSetup
import groovyx.gpars.GParsPool
import org.gradle.api.DefaultTask
import org.gradle.api.reporting.ReportingExtension
import org.gradle.api.tasks.TaskAction

class FragmentationTask extends DefaultTask {

    FragmentationTask() {
        outputs.upToDateWhen { false };
    }

    @TaskAction
    def runFragmentation() {
        OptimusExtension optimusExtension = project.getExtensions().findByType(OptimusExtension.class);
        ReportingExtension reportingExtension = project.getExtensions().findByType(ReportingExtension.class);
        OptimusServiceExtension serviceExtension = project.getExtensions().findByType(OptimusServiceExtension.class)
        OptimusHelper.setupServiceEnvoirment(serviceExtension)
        OptimusHelper.setup(project,optimusExtension,serviceExtension)
        def run = new OptimusSetup().getDevicesForThisRun(project, optimusExtension.testFeed)
        runDeviceFragmentation(run, optimusExtension,serviceExtension,reportingExtension);
    }

    def runDeviceFragmentation(List<String> udidList, OptimusExtension optimusExtension, OptimusServiceExtension serviceExtension,ReportingExtension reportingExtension) {
        OptimusServiceExtension optimusServiceExtension = project.getExtensions().findByType(OptimusServiceExtension.class);
        def size = udidList.size()
        println "Total devices -- " + size
        GParsPool.withPool(size) {
                udidList.eachParallel { String udid ->
                    project.javaexec {
                        main = "cucumber.api.cli.Main"
                        classpath = optimusExtension.classpath
                        args = getArgs(udid,optimusExtension,reportingExtension)
                        ignoreExitValue = true
                        systemProperties = [
                                "testFeed"      : optimusExtension.testFeed,
                                "udid"          : udid,
                                "runMode"       : "Fragmentation",
                                "setupCompleted": "true",
                                "devMode"       : optimusExtension.devMode,
                                "regression"    : optimusExtension.regression,
                                "env"           : optimusExtension.env,
                                "database"      : serviceExtension.database,
                                "uri"           : serviceExtension.uri,
                                "serviceUrl"    : OptimusHelper.getServiceUrl(optimusServiceExtension)
                        ]
                    }
                }
        }

    }


    static def String updateReportFileName(String name) {
        String[] deviceIdString = name.split(":");
        return deviceIdString.length > 1 ? "emulator_" + deviceIdString[0].substring(deviceIdString[0].lastIndexOf(".") + 1) : name;
    }

    def getArgs(String udid, OptimusExtension optimusExtension, ReportingExtension reportingExtension) {
        if(optimusExtension.tags!=null) {
                return ["-p", "pretty", "-p", ("json:${reportingExtension.baseDir}/cucumber/" + updateReportFileName(udid) + ".json"), "--glue", "steps", "-t", optimusExtension.tags,"-t","~@intent,~@Intent,~@dataIntent,~@DataIntent",
                                "${project.projectDir}/src/test/resources/features"];
        } else {
            return ["-p", "pretty", "-p", ("json:${reportingExtension.baseDir}/cucumber/" + updateReportFileName(udid) + ".json"), "--glue", "steps","-t","~@intent,~@Intent,~@dataIntent,~@DataIntent",
                                "${project.projectDir}/src/test/resources/features"];
        }
    }
}
