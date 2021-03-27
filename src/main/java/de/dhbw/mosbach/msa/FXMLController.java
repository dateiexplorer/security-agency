package de.dhbw.mosbach.msa;

import de.dhbw.mosbach.msa.database.HSQLDB;
import de.dhbw.mosbach.msa.interpreter.CQLInterpreter;
import de.dhbw.mosbach.msa.interpreter.CQLResult;
import de.dhbw.mosbach.msa.interpreter.ICQLInterpreterListener;
import de.dhbw.mosbach.msa.logging.Logger;
import de.dhbw.mosbach.msa.network.INetworkListener;
import de.dhbw.mosbach.msa.network.Network;
import de.dhbw.mosbach.msa.network.events.ResultEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class FXMLController implements ICQLInterpreterListener, INetworkListener {

    public final static Logger logger = new Logger();

    @FXML
    private TextArea txtInput;

    @FXML
    private TextArea txtOutput;

    @FXML
    private TextArea txtMessage;

    @FXML
    private Button btnExecute;

    @FXML
    private Tab tabOutput;

    @FXML
    private Tab tabMessage;

    @FXML
    private TabPane tabs;

    @FXML
    private ToggleButton btnDebug;

    @FXML
    private Button btnLoadLog;

    @FXML
    private Button btnClear;

    private CQLInterpreter interpreter;

    public void initialize() {
        Network network = new Network();
        network.addListener(this);

        interpreter = new CQLInterpreter(network);
        interpreter.addListener(this);
    }

    @FXML
    private void handleBtnExecute() {
        String query = txtInput.getText();
        interpreter.execute(query);
    }

    @FXML
    private void handleBtnDebug() {
        txtOutput.setStyle("-fx-text-fill: orange;");
        logger.setActive(btnDebug.isSelected());

        tabs.getSelectionModel().select(tabOutput);
        txtOutput.setText("set debug to " + logger.isActive());
    }

    @FXML
    private void handleBtnLoadLog() {
        txtOutput.setStyle("-fx-text-fill: black;");

        tabs.getSelectionModel().select(tabOutput);
        txtOutput.setText(logger.getContent(logger.getLastLogFile()));
    }

    @FXML
    private void handleBtnClear() {
        txtOutput.clear();
        txtMessage.clear();
    }

    @Override
    public void onResultReceived(CQLResult result) {
        switch (result.getType()) {
            case OK -> txtOutput.setStyle("-fx-text-fill: black;");
            case ERROR -> txtOutput.setStyle("-fx-text-fill: red;");
        }

        tabs.getSelectionModel().select(tabOutput);
        txtOutput.setText(result.getOutput() + System.lineSeparator());
    }

    // If message send to the network, print it to the output.
    @Override
    public void onMessageReceived(ResultEvent event) {
        txtMessage.setStyle("-fx-text-fill: black;");

        tabs.getSelectionModel().select(tabMessage);
        txtMessage.appendText(String.format(event.getResult().getOutput() + System.lineSeparator()));
    }

    // Getter and setter
    public Button getBtnExecute() {
        return btnExecute;
    }

    public ToggleButton getBtnDebug() {
        return btnDebug;
    }

    public Button getBtnLoadLog() {
        return btnLoadLog;
    }

    public Button getBtnClear() {
        return btnClear;
    }
}
