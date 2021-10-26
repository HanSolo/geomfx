module eu.hansolo.fx.geomfx {
    // Java
    requires java.base;
    requires java.logging;

    // Java-FX
    requires javafx.base;
    requires javafx.graphics;
    requires javafx.controls;
    requires javafx.swing;

    exports eu.hansolo.fx.geomfx;
    exports eu.hansolo.fx.geomfx.tools;
    exports eu.hansolo.fx.geomfx.transform;
}