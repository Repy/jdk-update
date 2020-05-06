package info.repy.adoptopenjdkupdate.plugins;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class AmazonCorreto implements Distribution {
    public String getName() {
        return "AmazonCorreto";
    }

    private String getAPI(boolean checksum, Version version, Architecture architecture, OS os) {
        StringBuilder builder = new StringBuilder();
        if (checksum) {
            builder.append("https://corretto.aws/downloads/latest_checksum/");
        } else {
            builder.append("https://corretto.aws/downloads/latest/");
        }
        switch (version) {
            case JDK8:
                builder.append("amazon-corretto-8");
                break;
            case JDK11:
                builder.append("amazon-corretto-11");
                break;
            default:
                throw new RuntimeException("not support");
        }
        switch (architecture) {
            case X64:
                builder.append("-x64");
                break;
            default:
                throw new RuntimeException("not support");
        }
        switch (os) {
            case Windows:
                builder.append("-windows-jdk.zip");
                break;
            case Linux:
                builder.append("-linux-jdk.tar.gz");
                break;
            case Mac:
                builder.append("-macos-jdk.tar.gz");
                break;
            default:
                throw new RuntimeException("not support");
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
