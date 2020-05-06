package info.repy.adoptopenjdkupdate.plugins;

import java.util.Locale;

public class DistributionFile {

    private String name;

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String url;

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    private Type type;

    public Type getType() {
        return this.type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public void setTypeFromName(String filename) {
        String filename2 = filename.toLowerCase(Locale.ENGLISH);
        if (filename2.endsWith(".zip")) {
            this.type = Type.ZIP;
            return;
        }
        if (filename2.endsWith(".tar.gz")) {
            this.type = Type.TARGZ;
            return;
        }
        throw new RuntimeException("not support");
    }

    public static enum Type {
        ZIP,
        TARGZ;
    }
}
