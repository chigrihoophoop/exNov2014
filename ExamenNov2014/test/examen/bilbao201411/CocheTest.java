package examen.bilbao201411;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import examen.bilbao201411.Coche;

public class CocheTest {

	Coche c;
	@Before
	public void setUp() throws Exception {
		c = new Coche();
	}

	@Test
	public void testGira() {
		c.gira( 10 );
		assertEquals( 10.0, c.getDireccionActual(), 0.0 );
		c.gira( 360 );
		assertEquals( 10.0, c.getDireccionActual(), 0.0 );
		c.gira( -360 );
		assertEquals( 10.0, c.getDireccionActual(), 0.0 );
	}

	@Test
	public void testFuerzaAceleracionAdelante() {
		double[] tablaVel =    { -600, -500, -150,  -75,   0,  125, 250, 500, 750, 875, 1000 };
		double[] tablaFuerza = {    1,    1,    1, 0.75, 0.5, 0.75,   1,   1,   1, 0.5,    0 };  
		for (int i=0;i<tablaVel.length;i++) {
			c.setVelocidad( tablaVel[i] );
			assertEquals( "Velocidad " + tablaVel[i], tablaFuerza[i]*Coche.FUERZA_BASE_ADELANTE, c.fuerzaAceleracionAdelante(), 0.0000001 );
		}
	}

	@Test
	public void testFuerzaAceleracionAtras() {
		double[] tablaVel =    { -500, -425, -300, -250, -200, -100,   0,   125,  250,  500, 1100 };
		double[] tablaFuerza = {    0,  0.5,    1,    1,    1, 0.65, 0.3, 0.575, 0.85, 0.85, 0.85 };  
		for (int i=0;i<tablaVel.length;i++) {
			c.setVelocidad( tablaVel[i] );
			assertEquals( "Velocidad " + tablaVel[i], tablaFuerza[i]*Coche.FUERZA_BASE_ATRAS, c.fuerzaAceleracionAtras(), 0.0000001 );
		}
	}
	
	@Test
	public void testAcelera() {
		c.setVelocidad(0);
		c.acelera(100, 1);
		assertEquals(100, c.getVelocidad(),0.0);
		c.acelera(50,0.02);
		assertEquals(101, c.getVelocidad(),0.0);
		//pruebo con aceleraciones negativas
		c.setVelocidad(0);
		c.acelera(-100, 1);
		assertEquals(-100, c.getVelocidad(),0.0);
		c.setVelocidad(100);
		c.acelera(-50,0.02);
		assertEquals(99, c.getVelocidad(),0.0);
	}
	

}
