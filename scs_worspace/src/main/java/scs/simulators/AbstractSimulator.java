package scs.simulators;

import java.util.concurrent.ExecutorService;

import scs.main.BloodStream;

public abstract class AbstractSimulator {


	protected BloodStream bloodStream = BloodStream.getInstance();

	protected ExecutorService executor;

	public abstract void start();

}
