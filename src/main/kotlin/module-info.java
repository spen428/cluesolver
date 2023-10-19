module com.lykat.cluesolver {
    requires javafx.controls;
    requires javafx.fxml;
    requires kotlin.stdlib;
    requires javafx.web;
    requires jnativehook;
    requires java.logging;
    requires java.desktop;


    opens com.lykat.cluesolver to javafx.fxml;
    exports com.lykat.cluesolver;
}