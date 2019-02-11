package spreadSheetDemo;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.ParseException;


public class Controller {
    @FXML
    public TableView tableView;

    private TableManipulator tableManipulator;

    public void initialize() {
        this.tableManipulator = new TableManipulator(tableView);
    }

    public void loadFile(Event e) throws MalformedURLException {
        System.out.println(e);
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open TTL File");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Turtle file with RDF data", "*.ttl"));
        File file = fileChooser.showOpenDialog(tableView.getScene().getWindow());
        tableManipulator.readTTLInTable(file.toURI().toURL());
    }

    public void inferMissing(Event e) {
        TableDataReasoner tableDataReasoner = new TableDataReasoner(tableView, tableManipulator.getNumRows());
        try {
            tableDataReasoner.inferData();
        } catch (IOException e1) {
            e1.printStackTrace();
        } catch (ParseException e1) {
            e1.printStackTrace();
        }
    }
}
