module utalca.chatpyme {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.desktop;
    requires org.mongodb.driver.sync.client;
    requires org.mongodb.driver.core;
    requires org.mongodb.bson;


    opens utalca.chatpyme to javafx.fxml;
    exports utalca.chatpyme;
}