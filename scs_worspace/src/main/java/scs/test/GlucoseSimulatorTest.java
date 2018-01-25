package scs.test;

import static org.junit.Assert.*;

import java.util.Collection;

import org.junit.Test;

import scs.simulators.GlucoseSimulator;

public class GlucoseSimulatorTest{
	
	GlucoseSimulator obj = new GlucoseSimulator();
	

	@Test
	public void testConsume() {
		obj.consume(200);
		assertEquals(obj.glucoseLevelOffsetSeries.size()>0,true);
	}

	@Test
	public void testGetIsBalancedProperty() {
		assertEquals(obj.getIsBalancedProperty().getValue(),true);		
	}

	@Test
	public void testGetIsBalancingProperty() {
		assertEquals(obj.getIsBalancingProperty().getValue(),false);
	}

	@Test
	public void testGetIsConsumingProperty() {
		obj.consume(200);
		assertEquals(obj.getIsConsumingProperty().getValue(),true);
	}

	@Test
	public void testGetTotalAmountInConsumption_mmol_L() {
		obj.consume(200.0);
		assertEquals(obj.getTotalAmountInConsumption_mmol_L().getValue()==27.77777777777778,true);
	}

	@Test
	public void testCalculateGlucoseLevelIncrease_Mmol_L() {
		assertEquals(obj.calculateGlucoseLevelIncrease_Mmol_L(200.0)==27.77777777777778,true);
	}

	@Test
	public void testGenerateSeriesOfNegativeGlucoseLevelOffsets() {
		Collection offsetSerie = obj.generateSeriesOfNegativeGlucoseLevelOffsets(-200);
		assertEquals(offsetSerie.size()>0,true);
	}

	@Test
	public void testGenerateSeriesOfPositivGlucoseLevelOffsets() {
		Collection offsetSerie = obj.generateSeriesOfPositivGlucoseLevelOffsets(200);
		assertEquals(offsetSerie.size()>0,true);
	}

	@Test
	public void testGetOffsetSerie() {
		assertEquals(obj.getOffsetSerie(10, 10).size()==10,true);
	}

	@Test
	public void testGetRegularValue() {
		Double val = obj.getRegularValue();
		assertEquals(val<6 && val>5,true);
	}

	@Test
	public void testGetShouldThrowErrorProperty() {
		assertEquals(obj.getShouldThrowErrorProperty().getValue(),false);
	}

}
