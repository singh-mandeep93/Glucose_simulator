package scs.gui;

import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.concurrent.ConcurrentLinkedQueue;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.converter.NumberStringConverter;
import scs.main.BloodStream;
import scs.simulators.GlucoseSimulator;
import scs.simulators.PancreasSimulator;

/**
 * This is the Main Controller, it is responsible for the mapping of Data from
 * Model to View Layer. 
 *
 */
public class DisplayController {

	@FXML
	private Label messageBox;


	private final static int NUM_OF_X_AXIS_SLICES = 300;

	public void clearErrorMessage()
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				messageBox.setText("");
				messageBox.setVisible(false);
			}
		});
	}

	public void printConversionErrorMessage()
	{

		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				messageBox.setText(MessageText.NOT_A_NUMBER_ERROR.getText());
				messageBox.setVisible(true);
			}
		});
	}

	public void printError(final String text)
	{
		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				messageBox.setText(text);
				messageBox.setVisible(true);
			}
		});

	}

	public void printExceptionMessage()
	{

		Platform.runLater(new Runnable() {
			@Override
			public void run()
			{
				messageBox.setTextFill(Color.RED);
				messageBox.setText(MessageText.EXCEPTION_MESSAGE.getText());
				messageBox.setVisible(true);
			}
		});
	}


	@FXML
	private TextField balancingRangeMaximum;

	@FXML
	private TextField balancingRangeMinimum;

	private BloodStream bloodStream = BloodStream.getInstance();

	@FXML private Text actiontarget;

	@FXML
	private Button consumeButton;

	@FXML
	private Button StartButton;


	@FXML
	private ProgressBar consumptionBar;

	@FXML
	private TextField consumptionField;

	private SimpleDateFormat dateFormatter;

	@FXML
	private Label elapsedTime;

	private GlucoseSimulator glucoseLevelSimulator = new GlucoseSimulator();

	@FXML
	private LineChart<Number, Number> lineChart;

	private PancreasSimulator pancreasSimulator = new PancreasSimulator();

	@FXML
	private TextField readjustmentNegativeInjectionBoundary;

	@FXML
	private TextField readjustmentPositiveInjectionBoundary;

	@FXML
	private Text logintext;

	@FXML
	private Label usertext;

	@FXML
	private Label passwordtext;


	private Series<Number, Number> series;

	@FXML
	private CheckBox simulationCheckBox;

	@FXML
	private Label statusLabel;

	@FXML
	private Circle statusLed;

	@FXML
	private CheckBox throwError;

	@FXML
	private CheckBox needle_removed;

	@FXML
	private NumberAxis xAxis;

	private double xSeriesData = 0;

	@FXML
	private NumberAxis yAxis;

	@FXML
	private GridPane doctorpane;
	@FXML private TextField user;

	@FXML private TextField password;

	@FXML private Button loginButton;
	@FXML private VBox Plane;

	public DisplayController() {
		dateFormatter = new SimpleDateFormat("mm ss");
		dateFormatter.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
	}

	/**
	 * Initiates the consumption process of the entered calories
	 *
	 * @param e
	 */
	@FXML
	public void login(ActionEvent e) throws IOException {

	if(user.getText().equals("doctor")&&password.getText().equals("password")&&loginButton.getText().equals("Sign In")){
			doctorpane.setVisible(true);
			loginButton.setText("Sign Out");
			password.setText("");
			user.setText("");
			logintext.setVisible(false);
			usertext.setVisible(false);
			passwordtext.setVisible(false);
			password.setVisible(false);
			user.setVisible(false);
			actiontarget.setVisible(false);



		}
		else if(loginButton.getText().equals("Sign Out")){
			doctorpane.setVisible(false);
			loginButton.setText("Sign In");
			logintext.setVisible(true);
			usertext.setVisible(true);
			passwordtext.setVisible(true);
			password.setVisible(true);
			user.setVisible(true);
			actiontarget.setVisible(false);

		}
		else{
			actiontarget.setFill(Color.RED);
			actiontarget.setText("Wrong Credentials.");
			actiontarget.setVisible(true);
		}
	}
	@FXML
	public void consume(ActionEvent e)
	{
		try
		{
			double amountToConsume = 0;
			amountToConsume = getAmountToConsume();
			if (amountToConsume != 0)
			{
				messageBox.setVisible(false);
				glucoseLevelSimulator.consume(amountToConsume);
			}
		} catch (NumberFormatException ex)
		{
			messageBox.setText(MessageText.NOT_A_NUMBER_ERROR.getText());
			messageBox.setVisible(true);
		}
		consumptionField.setText("");
	}
	@FXML
	public void start(ActionEvent e){
		Plane.setVisible(true);
		StartButton.setVisible(false);

	}

	@SuppressWarnings("unchecked")
	public void initialize()
	{
		bindProperties();
		messageBox.setVisible(false);
		series = new XYChart.Series<Number, Number>();
		lineChart.getData().addAll(series);
		series.getNode().setStyle("-fx-stroke: #989898; -fx-stroke-width: 2px; ");
		glucoseLevelSimulator.start();
		pancreasSimulator.start();
		doctorpane.setVisible(false);
		Plane.setVisible(false);
	}

	public void updateGui()
	{
		ConcurrentLinkedQueue<Number> glucoseLevelQueue = BloodStream.getInstance().getGlucoseLevelGuiQueue();

		Double currentGlucoseLevelValue = (Double) glucoseLevelQueue.peek();
		if (currentGlucoseLevelValue != null)
		{
			evaluateCurrentStatus(currentGlucoseLevelValue);
		}


		int numOfPendingValues = glucoseLevelQueue.size();
		for (int i = 0; i < numOfPendingValues; i++)
		{
			series.getData().add(new Data<Number, Number>(xSeriesData++, glucoseLevelQueue.remove()));
		}


		if (series.getData().size() > NUM_OF_X_AXIS_SLICES)
		{
			series.getData().remove(0, series.getData().size()
					- NUM_OF_X_AXIS_SLICES);
		}
		consumptionBar.setVisible(false);
		if(consumeButton.isDisabled()){
			needle_removed.setDisable(true);
		}
		try {
			if (!needle_removed.isSelected()) {
				simulationCheckBox.setSelected(false);
				throw new RuntimeException();

			}

		}catch (RuntimeException e){
			printError("Needle removed");
		}

		updateTimeStamp();


		xAxis.setLowerBound(xSeriesData - NUM_OF_X_AXIS_SLICES);
		xAxis.setUpperBound(xSeriesData - 1);
	}

	private void bindProperties()
	{
		consumeButton.disableProperty().bindBidirectional(glucoseLevelSimulator.getIsConsumingProperty());
		consumptionField.disableProperty().bindBidirectional(glucoseLevelSimulator.getIsConsumingProperty());
		simulationCheckBox.disableProperty().bindBidirectional(glucoseLevelSimulator.getIsConsumingProperty());
		simulationCheckBox.selectedProperty().bindBidirectional(glucoseLevelSimulator.getIsBalancedProperty());
		throwError.selectedProperty().bindBidirectional(glucoseLevelSimulator.getShouldThrowErrorProperty());
		balancingRangeMaximum.textProperty().bindBidirectional(pancreasSimulator.getBalancingRangeMaximumProperty());
		balancingRangeMinimum.textProperty().bindBidirectional(pancreasSimulator.getBalancingRangeMinimumProperty());
		readjustmentNegativeInjectionBoundary.textProperty().bindBidirectional(pancreasSimulator.getReadjustmentNegativeInjectionBoundaryProperty());
		readjustmentPositiveInjectionBoundary.textProperty().bindBidirectional(pancreasSimulator.getReadjustmentPositiveInjectionBoundaryProperty());
	}

	private void evaluateCurrentStatus(double currentGlucoseLevelValue)
	{
		if (currentGlucoseLevelValue > 17 || currentGlucoseLevelValue < 3)
		{
			setStatus(GlucoseStatus.CRITICAL);
		} else if (currentGlucoseLevelValue > 4 && currentGlucoseLevelValue < 8)
		{
			setStatus(GlucoseStatus.GOOD);
		} else
		{
			setStatus(GlucoseStatus.ALERTING);
		}
	}

	private double getAmountToConsume()
	{
		double amountToConsume = 0;
		String fieldText = consumptionField.getText();
		if (!fieldText.isEmpty())
		{
			amountToConsume = Double.parseDouble(fieldText);
		}
		return amountToConsume;
	}

	private void setStatus(GlucoseStatus status)
	{
		statusLabel.setText(status.getValue());
		switch (status)
		{
			case ALERTING:
				statusLed.setFill(Color.ORANGE);
				statusLed.setStroke(Color.ORANGE);
				break;
			case CRITICAL:
				statusLed.setFill(Color.CRIMSON);
				statusLed.setStroke(Color.CRIMSON);
				break;
			case GOOD:
				statusLed.setFill(Color.GREEN);
				statusLed.setStroke(Color.GREEN);
				break;
			default:
				break;
		}
	}

	/**
	 * Updates the time stamp on the GUI
	 */
	private void updateTimeStamp()
	{
		String rawTimeStamp = dateFormatter.format(bloodStream.getElapsedTime());
		String[] timeStamp = rawTimeStamp.split(" ");
		String updatedTime = timeStamp[0] + "h " + timeStamp[1] + "min";
		elapsedTime.setText(updatedTime);
	}

}
