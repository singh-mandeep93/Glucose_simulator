package scs.simulators;

import static scs.gui.DisplayControllerHolder.getController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import scs.main.BloodStream;

public class GlucoseSimulator extends AbstractSimulator {

	private final static int SIMULATION_STEPS_IN_MILLIS = 100;

	private double consumptionInProgress_mmol_L = 0;


	private double currentGlucoseLevelOffset = 0;

	public final ConcurrentLinkedQueue<Double> glucoseLevelOffsetSeries = new ConcurrentLinkedQueue<>();

	public BooleanProperty isBalanced = new SimpleBooleanProperty(true);

	public BooleanProperty isBalancing = new SimpleBooleanProperty(false);

	public BooleanProperty shouldThrowError = new SimpleBooleanProperty(false);

	public BooleanProperty isConsuming = new SimpleBooleanProperty();


	private final int MAX_AMOUNT_KCAL_IN_2H = 2000;

	private final double MIN_AMOUNT_KCAL_IN_30MIN = -500d;

	private final int MAX_NEGATIVE_INTAKE_DURATION = 300;

	private final int MAX_POSITIVE_INTAKE_DURATION = 1200;

	private SimulatorThread simulatorThread;

	private DoubleProperty totalAmountInConsumption_mmol_L = new SimpleDoubleProperty(0);

	public GlucoseSimulator() {
		super();
		try
		{
			isConsuming.setValue(false);
			executor = Executors.newCachedThreadPool(new ThreadFactory() {
				@Override
				public Thread newThread(Runnable r)
				{
					Thread thread = new Thread(r);
					thread.setDaemon(true);
					return thread;
				}
			});
		} catch (Exception e)
		{
			getController().printExceptionMessage();
		}
	}

	public void consume(double amountToConsume)
	{
		isConsuming.setValue(true);
		double amount_mmol_L = calculateGlucoseLevelIncrease_Mmol_L(amountToConsume);
		totalAmountInConsumption_mmol_L.setValue(amount_mmol_L);
		if (amountToConsume > 0)
		{
			glucoseLevelOffsetSeries.addAll(generateSeriesOfPositivGlucoseLevelOffsets(amount_mmol_L));
		} else
		{
			glucoseLevelOffsetSeries.addAll(generateSeriesOfNegativeGlucoseLevelOffsets(amount_mmol_L));
		}
	}


	public Property<Boolean> getIsBalancedProperty()
	{
		return isBalanced;
	}

	public Property<Boolean> getIsBalancingProperty()
	{
		return isBalancing;
	}

	public Property<Boolean> getIsConsumingProperty()
	{
		return isConsuming;
	}

	public DoubleProperty getTotalAmountInConsumption_mmol_L()
	{
		return totalAmountInConsumption_mmol_L;
	}


	public void start()
	{
		simulatorThread = new SimulatorThread();
		executor.execute(simulatorThread);
	}

	public double calculateGlucoseLevelIncrease_Mmol_L(Double kcal)
	{
		kcal = verifyKcalWithinBoundary(kcal);

		if (kcal > 0)
		{
			kcal /= 2;
		}

		double amountOfCarbs_mg = (kcal / 4) * 1000;

		double amountOfBlood_dL = BloodStream.getBloodAmountInLitre() * 10;

		// total amount of glucose in mg/dl which will be consumed
		double glucoseLevelIncrease_mg_dL = amountOfCarbs_mg / amountOfBlood_dL;

		double glucoseLevelIncrease_mmol_L = glucoseLevelIncrease_mg_dL / 18;

		return glucoseLevelIncrease_mmol_L;

	}

	private Double verifyKcalWithinBoundary(Double kcal)
	{
		if (kcal > MAX_AMOUNT_KCAL_IN_2H)
		{
			kcal = (double) MAX_AMOUNT_KCAL_IN_2H;
		}

		if (kcal < -500)
		{
			kcal = MIN_AMOUNT_KCAL_IN_30MIN;
		}
		return kcal;
	}

	public Collection<? extends Double> generateSeriesOfNegativeGlucoseLevelOffsets(
			double glucoseLevelDecrease_mmol_L)
	{
		ArrayList<Double> result = new ArrayList<>(MAX_NEGATIVE_INTAKE_DURATION);
		result.addAll(0, getOffsetSerie(glucoseLevelDecrease_mmol_L, MAX_POSITIVE_INTAKE_DURATION));
		return result;
	}

	public ArrayList<Double> generateSeriesOfPositivGlucoseLevelOffsets(
			double glucoseLevelIncrease_mmol_L)
	{
		ArrayList<Double> result = new ArrayList<>(MAX_POSITIVE_INTAKE_DURATION);



		int iterator = 0;
		double glucoseAmountInInit = glucoseLevelIncrease_mmol_L * 0.1;
		int timeSlicesInInit = (int) (MAX_POSITIVE_INTAKE_DURATION * 0.25);
		result.addAll(iterator, getOffsetSerie(glucoseAmountInInit, timeSlicesInInit));

		double glucoseAmountInPeak = glucoseLevelIncrease_mmol_L * 0.45;
		int timeSlicesInPeak = (int) (MAX_POSITIVE_INTAKE_DURATION * 0.125);
		iterator = timeSlicesInInit - 1;
		result.addAll(iterator, getOffsetSerie(glucoseAmountInPeak, timeSlicesInPeak));

		double glucoseAmountInRest_1 = glucoseLevelIncrease_mmol_L * 0.20;
		int timeSlicesInRest_1 = (int) (MAX_POSITIVE_INTAKE_DURATION * 0.125);
		iterator += timeSlicesInPeak;
		result.addAll(iterator, getOffsetSerie(glucoseAmountInRest_1, timeSlicesInRest_1));

		double glucoseAmountInRest_2 = glucoseLevelIncrease_mmol_L * 0.15;
		int timeSlicesInRest_2 = (int) (MAX_POSITIVE_INTAKE_DURATION * 0.25);
		iterator += timeSlicesInRest_1;
		result.addAll(iterator, getOffsetSerie(glucoseAmountInRest_2, timeSlicesInRest_2));

		double glucoseAmountInRest_3 = glucoseLevelIncrease_mmol_L * 0.10;
		int timeSlicesInRest_3 = (int) (MAX_POSITIVE_INTAKE_DURATION * 0.25);
		iterator += timeSlicesInRest_2;
		result.addAll(iterator, getOffsetSerie(glucoseAmountInRest_3, timeSlicesInRest_3));

		return result;
	}

	public ArrayList<Double> getOffsetSerie(double amount, int timeSlices)
	{
		double distiributedAmount = amount / timeSlices;
		ArrayList<Double> result = new ArrayList<>(timeSlices);
		for (int i = 0; i < timeSlices; i++)
		{
			result.add(i, distiributedAmount);
		}
		return result;
	}

	
	public double getRegularValue()
	{
		double value = 5.5;
		double naturalOffset = Math.random() / 5;
		return value + naturalOffset;
	}

	private class SimulatorThread implements Runnable {
		public void run()
		{
			try
			{
				
				if (shouldThrowError.getValue() == true)
				{
					throw new RuntimeException();
				}
				ConcurrentLinkedQueue<Number> glucoseLevelGui = bloodStream.getGlucoseLevelGuiQueue();

				ConcurrentLinkedQueue<Number> glucoseLevelControl = bloodStream.getGlucoseLevelControlQueue();
				double regularValue = getRegularValue();
				if (!glucoseLevelOffsetSeries.isEmpty())
				{
					double nextOffset = glucoseLevelOffsetSeries.remove();
					currentGlucoseLevelOffset += nextOffset;
					updateProgress(nextOffset);
				} else
				{
					isConsuming.setValue(false);
					consumptionInProgress_mmol_L = 0;
				}

				if (isBalanced.getValue())
				{
					final double hormonalEffect = bloodStream.getHormonalEffect();
					currentGlucoseLevelOffset += hormonalEffect;
					setBalancingIcon(hormonalEffect);
				}
				double valueToAdd = regularValue + currentGlucoseLevelOffset;
				glucoseLevelGui.add(valueToAdd);
				glucoseLevelControl.add(valueToAdd);
				try
				{
					Thread.sleep(SIMULATION_STEPS_IN_MILLIS);
					bloodStream.updateElapsedTime();
					executor.execute(this);
				} catch (InterruptedException ex)
				{
					getController().printExceptionMessage();
				}
			} catch (RuntimeException e)
			{
				getController().printExceptionMessage();
			}
		}

		private void setBalancingIcon(final double hormonalEffect)
		{
			Platform.runLater(new Runnable() {
				@Override
				public void run()
				{
					if (hormonalEffect == 0)
					{
						isBalancing.setValue(false);
					} else
					{
						isBalancing.setValue(true);
					}
				}
			});
		}

		private void updateProgress(double currentAmount)
		{
			consumptionInProgress_mmol_L += currentAmount;
			double progress = consumptionInProgress_mmol_L
					/ totalAmountInConsumption_mmol_L.doubleValue();
		}

	}

	public Property<Boolean> getShouldThrowErrorProperty()
	{
		return shouldThrowError;
	}

}
