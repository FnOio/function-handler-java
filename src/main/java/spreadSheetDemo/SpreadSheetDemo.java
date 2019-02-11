package spreadSheetDemo;

import FunctionHub.FunctionProcessor.FunctionInstance;
import FunctionHub.Query;
import FunctionHub.FunctionProcessor.ImplementationHandler;
import FunctionHub.Server;
import FunctionHub.models.Function;
import FunctionHub.models.Parameter;
import FunctionHub.models.Problem;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.NumberFormat;

public class SpreadSheetDemo extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) throws IOException {
        URL res = getClass().getResource("/spreadSheetDemo.fxml");
        Parent root = FXMLLoader.load(res);

        Scene scene = new Scene(root, 1000, 600);

        stage.setTitle("FunctionHub Inferencer");
        stage.setScene(scene);
        stage.show();


    }
}
