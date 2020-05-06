module info.repy.adoptopenjdkupdate {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.json;
    requires org.apache.commons.compress;
    opens info.repy.adoptopenjdkupdate;
    exports info.repy.adoptopenjdkupdate;
    exports info.repy.adoptopenjdkupdate.plugins;
}