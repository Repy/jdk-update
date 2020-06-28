package info.repy.adoptopenjdkupdate;

import info.repy.adoptopenjdkupdate.plugins.AdoptOpenJDK;
import info.repy.adoptopenjdkupdate.plugins.AmazonCorreto;
import info.repy.adoptopenjdkupdate.plugins.Distribution;
import info.repy.adoptopenjdkupdate.plugins.DistributionFile;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.compress.utils.IOUtils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.*;
import java.util.stream.Collectors;

public class Controller {
    public static final int MB = 1024 * 1024;

    @FXML
    public TextArea textArea;

    @FXML
    public ComboBox<Distribution.DistributionList> distributionComboBox;

    @FXML
    public ComboBox<Distribution.Architecture> architectureComboBox;

    @FXML
    public ComboBox<Distribution.OS> osComboBox;

    @FXML
    public ComboBox<Distribution.Version> versionComboBox;

    public void initialize() {
        String javaVersion = System.getProperty("java.version");
        String javafxVersion = System.getProperty("javafx.version");
        textArea.setText("Hello, JavaFX " + javafxVersion + "\nRunning on Java " + javaVersion + ".\n");

        distributionComboBox.getItems().setAll(Distribution.DistributionList.values());
        distributionComboBox.getSelectionModel().select(Distribution.DistributionList.AdoptOpenJDK);

        architectureComboBox.getItems().setAll(Distribution.Architecture.values());
        String osarch = System.getProperty("os.arch").toLowerCase();
        if (osarch.startsWith("x86_64")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X64);
        } else if (osarch.startsWith("amd64")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X64);
        } else if (osarch.startsWith("x86")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X86);
        } else if (osarch.startsWith("i386")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X86);
        } else if (osarch.startsWith("i486")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X86);
        } else if (osarch.startsWith("i586")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X86);
        } else if (osarch.startsWith("i686")) {
            architectureComboBox.getSelectionModel().select(Distribution.Architecture.X86);
        }

        osComboBox.getItems().setAll(Distribution.OS.values());
        String osname = System.getProperty("os.name").toLowerCase();
        if (osname.startsWith("linux")) {
            osComboBox.getSelectionModel().select(Distribution.OS.Linux);
        } else if (osname.startsWith("mac")) {
            osComboBox.getSelectionModel().select(Distribution.OS.Mac);
        } else if (osname.startsWith("windows")) {
            osComboBox.getSelectionModel().select(Distribution.OS.Windows);
        }

        versionComboBox.getItems().setAll(Distribution.Version.values());
        versionComboBox.getSelectionModel().select(Distribution.Version.JDK11);

    }

    public void download() {
        Distribution distribution;
        Distribution.DistributionList dist = distributionComboBox.getSelectionModel().getSelectedItem();
        switch (dist) {
            case AdoptOpenJDK:
                distribution = new AdoptOpenJDK();
                break;
            case AmazonCorretto:
                distribution = new AmazonCorreto();
                break;
            default:
                throw new RuntimeException("not support");
        }
        final Distribution.Architecture architecture = architectureComboBox.getSelectionModel().getSelectedItem();
        final Distribution.OS os = osComboBox.getSelectionModel().getSelectedItem();
        final Distribution.Version version = versionComboBox.getSelectionModel().getSelectedItem();

        Distribution finalDistribution = distribution;
        new Thread(() -> check(finalDistribution, version, architecture, os)).start();
    }

    public void check(Distribution distribution, Distribution.Version version, Distribution.Architecture arch, Distribution.OS os) {
        log(distribution.getName() + " " + version.name() + " update");
        try {
            Path basedir = Path.of(".").toAbsolutePath();
            basedir = basedir.resolve(distribution.getName());
            Path dir = basedir.resolve(version.name());
            Path ver = dir.resolve("check.version");
            String versionString = null;
            if (Files.exists(dir)) {
                if (!Files.isDirectory(dir)) {
                    throw new RuntimeException();
                }
                if (Files.exists(ver) && Files.isRegularFile(ver)) {
                    try (
                            BufferedReader bufferedReader = Files.newBufferedReader(ver, StandardCharsets.UTF_8)
                    ) {
                        versionString = bufferedReader.lines().collect(Collectors.joining(""));
                    }
                }
            } else {
                Files.createDirectories(dir);
            }
            if (versionString == null) {
                log("installed version none");
            } else {
                log("installed version " + versionString);
            }
            DistributionFile distributionFile = distribution.getDistributionFile(version, arch, os);
            log("latest version " + distributionFile.getName());
            if (Objects.equals(versionString, distributionFile.getName())) {
                log("finish");
                return;
            }
            Path tmpFile = Files.createTempFile(basedir, "tmp", "");


            log(distributionFile.getUrl());

            URL zipurl = new URL(distributionFile.getUrl());
            URLConnection zipcon = zipurl.openConnection();
            zipcon.connect();
            try (
                    BufferedInputStream bis = new BufferedInputStream(zipcon.getInputStream());
                    BufferedOutputStream bos = new BufferedOutputStream(Files.newOutputStream(tmpFile))
            ) {
                byte[] bytes = new byte[MB];
                int progress = -1;
                int nowByte = 0;
                int count;
                while ((count = bis.read(bytes)) > 0) {
                    bos.write(bytes, 0, count);
                    nowByte += count;
                    if (nowByte / MB > progress) {
                        progress = nowByte / MB;
                        log("downloading = " + progress + "MB");
                    }
                }
            }

            log("downloaded");
            if (Files.exists(dir)) {
                for (Iterator<Path> it = Files.walk(dir).sorted(Comparator.reverseOrder()).iterator(); it.hasNext(); ) {
                    Files.delete(it.next());
                }
            }
            log("unzip");

            if (distributionFile.getType() == DistributionFile.Type.ZIP) {
                try (InputStream fi = Files.newInputStream(tmpFile);
                     InputStream bi = new BufferedInputStream(fi);
                     ArchiveInputStream o = new ZipArchiveInputStream(bi)) {
                    extract(dir, o);
                }
            } else if (distributionFile.getType() == DistributionFile.Type.TARGZ) {
                try (InputStream fi = Files.newInputStream(tmpFile);
                     InputStream bi = new BufferedInputStream(fi);
                     InputStream gzi = new GzipCompressorInputStream(bi);
                     ArchiveInputStream o = new TarArchiveInputStream(gzi)) {
                    extract(dir, o);
                }
            }
            Files.delete(tmpFile);
            try (
                    BufferedWriter writer = Files.newBufferedWriter(ver, StandardCharsets.UTF_8)
            ) {
                writer.write(distributionFile.getName());
            }
            log("finish");
        } catch (Exception e) {
            log(e.getMessage());
        }
    }

    public void log(String msg) {
        Platform.runLater(() -> textArea.appendText(msg + "\n"));
    }

    public void extract(Path dir, ArchiveInputStream input) throws IOException {
        ArchiveEntry entry;
        while ((entry = input.getNextEntry()) != null) {
            Path p = Path.of(entry.getName());
            // zipのルートのディレクトリをスキップするので1スタート
            Path file = dir;
            for (int i = 1; i < p.getNameCount(); i++) {
                file = file.resolve(p.getName(i));
            }
            if (!input.canReadEntryData(entry)) {
                throw new RuntimeException("canReadEntryData=false");
            }
            if (entry.isDirectory()) {
                if (!Files.isDirectory(file)) {
                    Files.createDirectories(file);
                }
            } else {
                Path parent = file.getParent();
                if (!Files.isDirectory(parent)) {
                    Files.createDirectories(parent);
                }
                try (OutputStream output = Files.newOutputStream(file)) {
                    IOUtils.copy(input, output);
                }
            }
            setPermission(file, entry);
        }
    }

    @SuppressWarnings("OctalInteger")
    private static void setPermission(Path path, ArchiveEntry entry) throws IOException {
        int mode = 0;
        if (entry instanceof TarArchiveEntry) {
            TarArchiveEntry raw = (TarArchiveEntry) entry;
            mode = raw.getMode();
        } else if (entry instanceof ZipArchiveEntry) {
            ZipArchiveEntry raw = (ZipArchiveEntry) entry;
            mode = raw.getUnixMode();
        }
        if (mode == 0) {
            if (Files.isDirectory(path)) mode = 0755; // 8進数
            else mode = 0644; // 8進数
        }
        try{
            Files.setPosixFilePermissions(path, getPermission(mode));
        }catch (UnsupportedOperationException e){
            // Windows
        }
    }

    @SuppressWarnings("OctalInteger")
    private static Set<PosixFilePermission> getPermission(int mode) {
        Set<PosixFilePermission> ret = new HashSet<>();
        if ((mode & 0400) != 0) ret.add(PosixFilePermission.OWNER_READ);
        if ((mode & 0200) != 0) ret.add(PosixFilePermission.OWNER_WRITE);
        if ((mode & 0100) != 0) ret.add(PosixFilePermission.OWNER_EXECUTE);
        if ((mode & 0040) != 0) ret.add(PosixFilePermission.GROUP_READ);
        if ((mode & 0020) != 0) ret.add(PosixFilePermission.GROUP_WRITE);
        if ((mode & 0010) != 0) ret.add(PosixFilePermission.GROUP_EXECUTE);
        if ((mode & 0004) != 0) ret.add(PosixFilePermission.OTHERS_READ);
        if ((mode & 0002) != 0) ret.add(PosixFilePermission.OTHERS_WRITE);
        if ((mode & 0001) != 0) ret.add(PosixFilePermission.OTHERS_EXECUTE);
        return ret;
    }
}
