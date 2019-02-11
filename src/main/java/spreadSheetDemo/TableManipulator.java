package spreadSheetDemo;

import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.util.Callback;
import org.semanticweb.yars.nx.Node;
import org.semanticweb.yars.nx.parser.NxParser;
import org.semanticweb.yars.turtle.TurtleParser;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.*;

public class TableManipulator {
    private TableView tableView;
    private int numRows = 0;


    public TableManipulator (TableView tableView) {
        this.tableView = tableView;
    }

    public void readCSVInTable(URL csvFile) {
        populateTable(tableView, csvFile.toString(), true);
    }

    public void readTTLInTable(URL ttlFile) {
        tableView.getItems().clear();
        tableView.getColumns().clear();
        TurtleParser parser = new TurtleParser();
        try {
            parser.parse(new FileInputStream(new File(ttlFile.toURI())), Charset.defaultCharset(), new URI("http://example.com/"));
            Map<String, Map<String, String>> data = new HashMap<>();
            List<String> headerValues = new ArrayList<>();
            headerValues.add("Subject");
            for (Node[] nx : parser) {
                // prints the subject, eg. <http://example.org/>
                String subject = nx[0].getLabel();
                String pred = nx[1].getLabel();
                String value = nx[2].getLabel();
                if(!data.containsKey(subject)){
                    data.put(subject, new HashMap<>());
                }
                data.get(subject).put(pred, value);
                if(!headerValues.contains(pred))
                    headerValues.add(pred);
            }
            Platform.runLater(new Runnable() {
                @Override
                public void run() {
                    for (int column = 0; column < headerValues.size(); column++) {
                        TableColumn col = createColumn(column, headerValues.get(column));
                        col.setCellFactory(TextFieldTableCell.forTableColumn());
                        tableView.getColumns().add(col);
                    }
                }
            });

            for (Map.Entry<String, Map<String, String>> entry :
                    data.entrySet()) {
                String subject = entry.getKey();
                String[] values = new String[headerValues.size()];
                values[0] = subject;
                for (Map.Entry<String, String> datapoint: entry.getValue().entrySet()) {
                    String predicate = datapoint.getKey();
                    int columnIndex = headerValues.indexOf(predicate);
                    String value = datapoint.getValue();
                    values[columnIndex] = value;
                }
                Platform.runLater(new Runnable() {
                    @Override
                    public void run() {
                        // Add additional columns if necessary:
                        for (int columnIndex = tableView.getColumns().size(); columnIndex < values.length; columnIndex++) {
                            tableView.getColumns().add(createColumn(columnIndex, ""));
                        }
                        // Add data to table:
                        ObservableList<StringProperty> data = FXCollections
                                .observableArrayList();
                        for (String value : values) {
                            data.add(new SimpleStringProperty(value));
                        }
                        tableView.getItems().add(data);
                    }
                });
            }

        } catch (Exception e) { e.printStackTrace(); }

    }

    public int getNumRows() {
        return numRows;
    }

    private void populateTable(
            final TableView<ObservableList<StringProperty>> table,
            final String urlSpec, final boolean hasHeader) {
        table.getItems().clear();
        table.getColumns().clear();
        table.setPlaceholder(new Label("Loading..."));
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                BufferedReader in = getReaderFromUrl(urlSpec);
                // Header line
                if (hasHeader) {
                    final String headerLine = in.readLine();
                    final String[] headerValues = headerLine.split(";");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            for (int column = 0; column < headerValues.length; column++) {
                                TableColumn col = createColumn(column, headerValues[column]);
                                col.setCellFactory(TextFieldTableCell.forTableColumn());
                                table.getColumns().add(col);
                            }
                        }
                    });
                }

                // Data:

                String dataLine;
                while ((dataLine = in.readLine()) != null) {
                    final String[] dataValues = dataLine.split(";");
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            // Add additional columns if necessary:
                            for (int columnIndex = table.getColumns().size(); columnIndex < dataValues.length; columnIndex++) {
                                table.getColumns().add(createColumn(columnIndex, ""));
                            }
                            // Add data to table:
                            ObservableList<StringProperty> data = FXCollections
                                    .observableArrayList();
                            for (String value : dataValues) {
                                data.add(new SimpleStringProperty(value));
                            }
                            table.getItems().add(data);
                        }
                    });
                    numRows++;
                }
                return null;
            }
        };
        Thread thread = new Thread(task);
        thread.setDaemon(true);
        thread.start();
    }

    private TableColumn<ObservableList<StringProperty>, String> createColumn(
            final int columnIndex, String columnTitle) {
        TableColumn<ObservableList<StringProperty>, String> column = new TableColumn<>();
        String title;
        if (columnTitle == null || columnTitle.trim().length() == 0) {
            title = "Column " + (columnIndex + 1);
        } else {
            title = columnTitle;
        }
        column.setText(title);
        column
                .setCellValueFactory(new Callback<TableColumn.CellDataFeatures<ObservableList<StringProperty>, String>, ObservableValue<String>>() {
                    @Override
                    public ObservableValue<String> call(
                            TableColumn.CellDataFeatures<ObservableList<StringProperty>, String> cellDataFeatures) {
                        ObservableList<StringProperty> values = cellDataFeatures.getValue();
                        if (columnIndex >= values.size()) {
                            return new SimpleStringProperty("");
                        } else {
                            return cellDataFeatures.getValue().get(columnIndex);
                        }
                    }
                });
        return column;
    }

    private BufferedReader getReaderFromUrl(String urlSpec) throws Exception {
        URL url = new URL(urlSpec);
        URLConnection connection = url.openConnection();
        InputStream in = connection.getInputStream();
        return new BufferedReader(new InputStreamReader(in));
    }
}
