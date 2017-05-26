package com.testvagrant.optimus.utils

import com.testvagrant.monitor.MongoMain
import com.testvagrant.optimus.helpers.DeviceHelper
import com.testvagrant.optimus.register.DeviceRegistrar
import org.gradle.api.Project


class OptimusSetup {

    def setup(String testFeed) {
        MongoMain.main()
        new DeviceRegistrar().setUpDevices(new DeviceMatrix(testFeed + ".json"));
    }

    def List<String> getDevicesForThisRun(Project project, String testFeedName) {
        new DeviceHelper(getAppJson(project, testFeedName)).getConnectedDevicesMatchingRunCriteria();
    }

    private String getAppJson(Project project, String testfeedName) {
        File file = new File(project.projectDir.toString() + "/src/test/resources/" + testfeedName + ".json");
        println file.getAbsolutePath()
        if (file.exists()) {
            return file.text
        }
        return ''
    }

    def List<String> getTags(String tags) {
        if (!tags.contains(",")) {
            List<String> tagsList = new ArrayList<>();
            tagsList.add(tags);
            return tagsList;
        }
        List<String> tagsList = Arrays.asList(tags.split(","));
        tagsList.stream().forEach({ item -> item.toString().replaceAll("\\s", "") });
        return tagsList;
    }
}
