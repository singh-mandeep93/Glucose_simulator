package scs.main;

import java.io.IOException;
import java.io.InputStream;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Stage;
import scs.exceptions.ThrowableConverter;
import scs.gui.DisplayController;
import scs.gui.DisplayControllerHolder;

// Main class which starts the application.
public class Main extends Application {

	public static void main(String[] args)
	{
		launch(args);
	}

	private DisplayController mainController;

	@Override
	public void start(Stage stage)
	{
		try
		{
			stage.setTitle("Insuline/Glucagon Pump");
			Pane mainPane = loadMainPane();
			Scene scene = new Scene(mainPane);
			scene.getStylesheets().add
					(Main.class.getResource("background.css").toExternalForm());
			stage.setScene(scene);

			stage.show();

			prepareClock();
		} catch (Exception e)
		{
			final String message = "Unexpected Exception in main function \n"
					+ ThrowableConverter.convertStackTrace(e);
		}
	}

	private Pane loadMainPane()
	{
		Pane mainPane = null;
		try
		{
			FXMLLoader loader = new FXMLLoader();
			InputStream i = getClass().getClassLoader().getResourceAsStream("main.fxml");
			mainPane = (Pane) loader.load(i);
			mainController = (DisplayController) loader.getController();
			DisplayControllerHolder.setController(mainController);

		} catch (IOException e)
		{
			e.printStackTrace();
		} catch (RuntimeException e1)
		{
			e1.printStackTrace();
		}
		return mainPane;
	}


	private void prepareClock()
	{
		new AnimationTimer() {
			@Override
			public void handle(long now)
			{
				mainController.updateGui();
			}
		}.start();
	}
}
