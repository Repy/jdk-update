package info.repy.adoptopenjdkupdate.plugins;

import org.json.JSONArray;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class AdoptOpenJDK implements Distribution {
    public String getName() {
        return "AdoptOpenJDK";
    }

    private String getAPI(Version version, Architecture architecture, OS os) {
        StringBuilder builder = new StringBuilder();
        switch (version) {
            case JDK8:
                builder.append("https://api.adoptopenjdk.net/v3/assets/feature_releases/8/ga");
                break;
            case JDK11:
                builder.append("https://api.adoptopenjdk.net/v3/assets/feature_releases/11/ga");
                break;
            default:
                throw new RuntimeException("not support");
        }
        builder.append("?image_type=jdk");
        builder.append("&jvm_impl=hotspot");
        builder.append("&vendor=openjdk");
        builder.append("&heap_size=normal");
        builder.append("&project=jdk");
        builder.append("&page=0");
        builder.append("&page_size=1");
        builder.append("&sort_order=DESC");
        switch (architecture) {
            case X64:
                builder.append("&architecture=x64");
                break;
            case X86:
                builder.append("&architecture=x32");
                break;
            default:
                throw new RuntimeException("not support");
        }
        switch (os) {
            case Windows:
                builder.append("&os=windows");
                break;
            case Linux:
                builder.append("&os=linux");
                break;
            case Mac:
                builder.append("&os=mac");
                break;
            default:
                throw new RuntimeException("not support");
        }
        return builder.toString();
    }

    @Override
    public DistributionFile getDistributionFile(Version version, Architecture architecture, OS os) throws IOException {
        URL url = new URL(getAPI(version, architecture, os));
        URLConnection con = url.openConnection();
        con.connect();
        String jsonstr;
        try (
                InputStream is = con.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr)
        ) {
            StringBuilder sb = new StringBuilder();
            while ((jsonstr = br.readLine()) != null) {
                sb.append(jsonstr);
            }
            jsonstr = sb.toString();
        }
        JSONArray json = new JSONArray(jsonstr);
        DistributionFile distributionFile = new DistributionFile();
        distributionFile.setName(json.getJSONObject(0).getJSONArray("binaries").getJSONObject(0).getJSONObject("package").getString("name"));
        distributionFile.setUrl(json.getJSONObject(0).getJSONArray("binaries").getJSONObject(0).getJSONObject("package").getString("link"));
        distributionFile.setTypeFromName(distributionFile.getName());
        return distributionFile;
    }
}
