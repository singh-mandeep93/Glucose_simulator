package scs.test;

import static org.junit.Assert.*;

import org.junit.Test;

import scs.simulators.PancreasSimulator;

public class PancreasSimulatorTest {
	
	PancreasSimulator object = new PancreasSimulator();
	@Test
	public void testGetBalancingRangeMaximumProperty() {
		assertEquals(object.getBalancingRangeMaximumProperty().getValue(),"6");
	}

	@Test
	public void testGetBalancingRangeMinimumProperty() {
		assertEquals(object.getBalancingRangeMinimumProperty().getValue(), "5");
	}

	@Test
	public void testGetReadjustmentNegativeInjectionBoundaryProperty() {
		assertEquals(object.getReadjustmentNegativeInjectionBoundaryProperty().getValue(), "5.5");
	}

	@Test
	public void testGetReadjustmentPositiveInjectionBoundaryProperty() {
		assertEquals(object.getReadjustmentPositiveInjectionBoundaryProperty().getValue(), "8");
	}		
	
}
