package spreadSheetDemo;

import FunctionHub.FunctionProcessor.FunctionInstance;
import FunctionHub.FunctionProcessor.ImplementationHandler;
import FunctionHub.Query;
import FunctionHub.Server;
import FunctionHub.models.Function;
import FunctionHub.models.Parameter;
import FunctionHub.models.Problem;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.*;

public class TableDataReasoner {
    private TableView tableView;
    private int numRows;
    private Server fH;

    public TableDataReasoner(TableView tableView, int numRows) {
        this.tableView = tableView;
        this.numRows = numRows;
        try {
            this.fH = new Server(new URL("http://localhost:4000"));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public void inferData() throws IOException, ParseException {
        NumberFormat numberInstance = NumberFormat.getNumberInstance(Locale.ENGLISH);
        numberInstance.setGroupingUsed(false);
        int numColumns = tableView.getColumns().size();
        Map<Integer, String> dataTypes = new HashMap<>();

        for (int i = 0; i<numColumns;i++) {
            TableColumn column = (TableColumn) tableView.getColumns().get(i);
            if(column.getText().startsWith("http://")) {
                dataTypes.put(i, column.getText());
            }
        }

        List<ObservableList> newItems = new ArrayList<>();

        for (Object r: tableView.getItems()){
            List<String> expects = new ArrayList<>();
            String output = null;
            Map<String, Object> inputs = new HashMap<>();
            int outputPosition = -1;
            List row = (List) r;
            for (int i=0; i < numColumns; i++) {
                if (dataTypes.get(i) == null) {

                } else if(i < row.size() && row.get(i) != null && ((SimpleStringProperty)row.get(i)).getValue() != null && !((SimpleStringProperty)row.get(i)).getValue().equals("")) {
                    expects.add(dataTypes.get(i));
                    inputs.put(dataTypes.get(i), row.get(i));
                } else {
                    output = dataTypes.get(i);
                    outputPosition = i;
                }
            }
            if (outputPosition == -1) {
                newItems.add((ObservableList) row);
                continue;
            }
            Query query = new Query(null, null, null, new Problem(expects.toArray(new String[0]), output));
            Function[] functions = fH.query(query);
            if(functions.length == 0) {
                newItems.add((ObservableList) row);
                continue;
            }
            Function func = functions[0];
            if (functions.length > 1) {
                //TODO: ask which function to use
            }
            FunctionInstance fn = ImplementationHandler.instantiateFunctionImplementation(func, func.implementationMappings[0]);

            Problem solves = null;

            Set<String> expectsTypes = new HashSet<>();
            for (String p: expects) {
                expectsTypes.add(p);
            }

            for (Problem p: func.solves) {
                Set<String> types = new HashSet<>();
                Collections.addAll(types, p.input);
                if (expectsTypes.containsAll(types)) {
                    solves = p;
                }
            }

            Object fnOutput = null;
            
            List<Object> castInputs = new ArrayList<>();
            Class[] parameterTypes = fn.getParameterTypes();
            for (int i = 0; i< solves.input.length; i++) {
                Class wantedClass = parameterTypes[i];
                switch (wantedClass.getName()) {
                    case "java.lang.Float":
                    case "float":
                        Object property = numberInstance.parse(((StringProperty)inputs.get(solves.input[i])).getValue()).floatValue();
                        castInputs.add(property);
                        break;
                    case "java.lang.Double":
                    case "double":
                        castInputs.add(numberInstance.parse(((StringProperty)inputs.get(solves.input[i])).getValue()).doubleValue());
                        break;
                    default:
                        castInputs.add(wantedClass.cast(((StringProperty)inputs.get(solves.input[i])).getValue()));
                }

            }
            
            try {
                fnOutput = fn.executeFunction(castInputs.toArray());
            } catch (InvocationTargetException | IllegalAccessException e) {
                e.printStackTrace();
            }

            switch (fn.getReturnType().getName()){
                case "java.lang.Float":
                case "float":
                case "java.lang.Double":
                case "double":
                    fnOutput = numberInstance.format(fnOutput);
                    break;
            }

            ObservableList<StringProperty> newData = FXCollections
                    .observableArrayList();
            for (int i =0; i < numColumns; i++) {
                if (i == outputPosition) {
                    newData.add(new SimpleStringProperty(fnOutput.toString()));
                } else {
                    newData.add(new SimpleStringProperty(((SimpleStringProperty)row.get(i)).getValue()));
                }
            }
            newItems.add(newData);

        }
        tableView.getItems().clear();
        tableView.getItems().addAll(newItems);
    }
}
