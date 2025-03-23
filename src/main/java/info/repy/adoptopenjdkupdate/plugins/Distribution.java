package info.repy.adoptopenjdkupdate.plugins;

import java.io.IOException;

public interface Distribution {
    public String getViewName();
    public String getDirName();

    public DistributionFile getDistributionFile(Version version, Architecture architecture, OS os) throws IOException;

    public static enum DistributionList {
        MicrosoftBuildOfOpenJDK,
        AmazonCorretto;
    }

    public static enum Architecture {
        X64,
        X86;
    }

    public static enum OS {
        Windows,
        Linux,
        Mac;
    }

    public static enum Version {
        JDK8,
        JDK11,
        JDK17,
        JDK21;
    }
}
