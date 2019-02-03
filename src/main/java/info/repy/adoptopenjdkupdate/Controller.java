package info.repy.adoptopenjdkupdate;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.*;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class Controller {
    @FXML
    public TextArea textArea;


    public void initialize() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        textArea.setText("Hello, JavaFX " + javafxVersion + "\nRunning on Java " + javaVersion + ".\n");
    }

    public void check11() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                check(11);
            }
        }).start();
    }

    private static final String VERSION = "JAVA_VERSION=";

    public void check(int number) {
        final String API = "https://api.adoptopenjdk.net/v2/info/releases/openjdk" + number + "?release=latest&arch=x64&type=jdk&os=windows&openjdk_impl=hotspot";
        Platform.runLater(() -> textArea.appendText("openjdk" + number + " update\n"));
        File cd = getApplicationPath();
        File dir = new File(cd, "jdk" + Integer.toString(number));
        String versionString = null;
        if (dir.exists()) {
            if (!dir.isDirectory()) {
                throw new RuntimeException();
            }
            File release = new File(dir, "release");
            if (release.exists() && release.isFile()) {
                try (
                        FileReader filereader = new FileReader(release);
                        BufferedReader bufferedReader = new BufferedReader(filereader);
                ) {
                    List<String> version = bufferedReader.lines().filter(str -> str.startsWith(VERSION)).collect(Collectors.toList());
                    if (version.size() == 1) {
                        String vstr = version.get(0);
                        vstr = vstr.substring(VERSION.length());
                        vstr = vstr.trim();
                        vstr = trim(vstr, '"');
                        vstr = vstr.trim();
                        versionString = vstr;
                        final String fversion = versionString;
                        Platform.runLater(() -> textArea.appendText("installed " + fversion + "\n"));
                    }
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        try {
            URL url = new URL(API);
            URLConnection con = url.openConnection();
            con.connect();
            String jsonstr;
            try (
                    InputStream is = con.getInputStream();
                    InputStreamReader isr = new InputStreamReader(is);
                    BufferedReader br = new BufferedReader(isr);
            ) {
                StringBuilder sb = new StringBuilder();
                while ((jsonstr = br.readLine()) != null) {
                    sb.append(jsonstr);
                }
                jsonstr = sb.toString();
            }
            JSONObject json = new JSONObject(jsonstr);
            JSONArray binaries = json.getJSONArray("binaries");
            JSONObject binary = binaries.getJSONObject(0);
            JSONObject versionData = binary.getJSONObject("version_data");
            final String openjdkVersion = versionData.getString("openjdk_version");
            Platform.runLater(() -> textArea.appendText("latest " + openjdkVersion + "\n"));
            if (versionString != null) {
                if (Objects.equals(versionString, openjdkVersion.split("\\+")[0])) {
                    Platform.runLater(() -> textArea.appendText("finish\n"));
                    return;
                }
            }
            final String binaryLink = binary.getString("binary_link");
            File tmpFile = File.createTempFile("tmp", "", cd);
            tmpFile.deleteOnExit();

            Platform.runLater(() -> textArea.appendText("downloading\n"));
            Platform.runLater(() -> textArea.appendText(binaryLink + "\n"));
            URL zipurl = new URL(binaryLink);
            URLConnection zipcon = zipurl.openConnection();
            zipcon.connect();
            try (
                    BufferedInputStream bis = new BufferedInputStream(zipcon.getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(tmpFile));
            ) {
                byte[] bytes = new byte[1024];
                int count;
                while ((count = bis.read(bytes)) > 0) {
                    bos.write(bytes, 0, count);
                }
            }

            Platform.runLater(() -> textArea.appendText("downloaded\n"));
            if (dir.exists()) {
                Files.walk(Paths.get(dir.getAbsolutePath()))
                        .sorted(Comparator.reverseOrder())
                        .map(Path::toFile)
                        .forEach(File::delete);
            }
            Platform.runLater(() -> textArea.appendText("unzip\n"));

            try (ZipInputStream zin = new ZipInputStream(new FileInputStream(tmpFile))) {
                ZipEntry zipEntry;
                while ((zipEntry = zin.getNextEntry()) != null) {
                    Path path = Paths.get(zipEntry.getName());
                    int len = path.getNameCount();
                    if (len < 2) continue;
                    path = path.subpath(1, len);
                    File dst = new File(dir, path.toString());
                    dst.getParentFile().mkdirs();
                    if (zipEntry.isDirectory()) {
                        dst.mkdir();
                        continue;
                    }
                    try (
                            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(dst));
                    ) {
                        byte[] bytes = new byte[1024];
                        int count;
                        while ((count = zin.read(bytes)) > 0) {
                            bos.write(bytes, 0, count);
                        }
                    }
                }
            }
            Platform.runLater(() -> textArea.appendText("finish\n"));


        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        }


    }


    public String trim(String value, char ch) {
        int len = value.length();
        int st = 0;
        while ((st < len) && (value.charAt(st) <= ch)) {
            st++;
        }
        while ((st < len) && (value.charAt(len - 1) <= ch)) {
            len--;
        }
        return ((st > 0) || (len < value.length())) ? value.substring(st, len) : value;
    }

    public static File getApplicationPath() {
        URL url = Controller.class.getProtectionDomain().getCodeSource().getLocation();
        if (Objects.equals(url.getProtocol(), "file")) {
            try {
                URI uri = url.toURI();
                File file = new File(uri);
                return file.getParentFile().getAbsoluteFile();
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
        return new File(".").getAbsoluteFile();
    }
}
