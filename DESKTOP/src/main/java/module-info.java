module com.jtjmpm.desktop {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.kordamp.bootstrapfx.core;

    opens com.jtjmpm.desktop to javafx.fxml;
    exports com.jtjmpm.desktop;
}