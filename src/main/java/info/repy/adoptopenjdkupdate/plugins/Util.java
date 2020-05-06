package info.repy.adoptopenjdkupdate.plugins;

import info.repy.adoptopenjdkupdate.Controller;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Objects;

public final class Util {
    public static final File getApplicationPath() {
        URL url = Util.class.getProtectionDomain().getCodeSource().getLocation();
        if (Objects.equals(url.getProtocol(), "file")) {
            try {
                URI uri = url.toURI();
                File file = new File(uri);
                return file.getAbsoluteFile().getParentFile().getParentFile().getParentFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return new File(".").getAbsoluteFile();
    }
}
