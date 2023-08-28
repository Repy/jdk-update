package info.repy.adoptopenjdkupdate.plugins;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class MicrosoftBuildOfOpenJDK implements Distribution {
    public String getViewName() {
        return "Microsoft Build of OpenJDK";
    }
    public String getDirName() {
        return "MicrosoftOpenJDK";
    }

    private String getAPI(boolean checksum, Version version, Architecture architecture, OS os) {
        StringBuilder builder = new StringBuilder();
        builder.append("https://aka.ms/download-jdk/");
        switch (version) {
            case JDK11:
                builder.append("microsoft-jdk-17");
                break;
            case JDK17:
                builder.append("microsoft-jdk-17");
                break;
            default:
                throw new RuntimeException("not support");
        }
        switch (architecture) {
            case X64:
                switch (os) {
                    case Windows:
                        builder.append("-windows-x64.zip");
                        break;
                    case Linux:
                        builder.append("-linux-x64.tar.gz");
                        break;
                    case Mac:
                        builder.append("-macOS-x64.tar.gz");
                        break;
                    default:
                        throw new RuntimeException("not support");
                }
                break;
            default:
                throw new RuntimeException("not support");
        }
        if (checksum) {
            builder.append(".sha256sum.txt");
        }
        return builder.toString();
    }

    @Override
    public DistributionFile getDistributionFile(Version version, Architecture architecture, OS os) throws IOException {
        URL url = new URL(getAPI(true, version, architecture, os));
        URLConnection con = url.openConnection();
        con.connect();
        String checksum;
        try (
                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)
        ) {
            StringBuilder sb = new StringBuilder();
            while ((checksum = br.readLine()) != null) {
                sb.append(checksum);
            }
            checksum = sb.toString();
        }
        DistributionFile distributionFile = new DistributionFile();
        distributionFile.setName(checksum);
        distributionFile.setUrl(getAPI(false, version, architecture, os));
        distributionFile.setTypeFromName(distributionFile.getUrl());
        return distributionFile;
    }
}
