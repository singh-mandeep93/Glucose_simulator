package scs.main;

import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.ConcurrentLinkedQueue;

import scs.simulators.GlucoseSimulator;

/**
 * Bloodstream class offers a singleton object which is accessed by whole project, This
 * Object Contains the information about the Blood, like glucose level and Insulin amount available. 
 * This Object Showcases the Stream of data which is displayed in graph.
 *
 */
public class BloodStream {

	private static final double BLOOD_AMOUNT_LTR = 5;

	private static BloodStream instance;

	private static long simulationStartTime;

	public static double getBloodAmountInLitre()
	{
		return BLOOD_AMOUNT_LTR;
	}

	public static BloodStream getInstance()
	{
		if (BloodStream.instance == null)
		{
			BloodStream.instance = new BloodStream();
			Date buffer = new Date();
			simulationStartTime = buffer.getTime();
		}
		return BloodStream.instance;
	}

	/*Time in milliseconds since the simulation started.*/
	private Date elapsedTimeInMillis;
	private ConcurrentLinkedQueue<Number> glucoseLevelControlQueue = new ConcurrentLinkedQueue<Number>();

	
	private ConcurrentLinkedQueue<Number> glucoseLevelGuiQueue = new ConcurrentLinkedQueue<Number>();

	private ArrayList<ConcurrentLinkedQueue<Double>> hormoneInjections = new ArrayList<>();

	private BloodStream() {
		super();
	}


	public Date getElapsedTime()
	{
		return elapsedTimeInMillis;
	}

	public ConcurrentLinkedQueue<Number> getGlucoseLevelControlQueue()
	{
		return glucoseLevelControlQueue;
	}


	public ConcurrentLinkedQueue<Number> getGlucoseLevelGuiQueue()
	{
		return glucoseLevelGuiQueue;
	}

	public double getHormonalEffect()
	{
		double effect = 0;
		if (!hormoneInjections.isEmpty())
		{
			effect = calculateEffect();
			while (cleanupQueue())
				;
		}
		return effect;
	}

	public synchronized ArrayList<ConcurrentLinkedQueue<Double>> getHormoneInjections()
	{
		return hormoneInjections;
	}


	public void updateElapsedTime()
	{
		elapsedTimeInMillis = new Date(System.currentTimeMillis()
				- simulationStartTime);
	}

	private double calculateEffect()
	{
		double effect = 0;
		for (ConcurrentLinkedQueue<Double> injection : hormoneInjections)
		{
			if (!injection.isEmpty())
			{
				effect = injection.remove();
			}
		}
		return effect;
	}

	private boolean cleanupQueue()
	{
		int size = hormoneInjections.size();
		boolean isCleanNecessary = false;
		for (int i = 0; i < size; i++)
		{
			ConcurrentLinkedQueue<Double> queue = hormoneInjections.get(i);
			if (queue.isEmpty())
			{
				hormoneInjections.remove(i);
				isCleanNecessary = true;
				return isCleanNecessary;
			}
		}
		return isCleanNecessary;
	}

}
