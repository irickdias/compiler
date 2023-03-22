module com.example.rdcompiler {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;

    opens com.example.rdcompiler to javafx.fxml;
    exports com.example.rdcompiler;
}