package examen.bilbao201411;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import javax.swing.JPanel;

/** "Mundo" del juego del coche.
 * Incluye las f�sicas para el movimiento y los choques de objetos.
 * Representa un espacio 2D en el que se mueven el coche y los objetos de puntuaci�n.
 * @author Andoni Egu�luz Mor�n
 * Facultad de Ingenier�a - Universidad de Deusto
 */
public class MundoJuego implements Serializable {
	JPanel panel;  // panel visual del juego
	CocheJuego miCoche;    // Coche del juego
	//ArrayList<JLabelEstrella> estrellas = new ArrayList<JLabelEstrella>(); // PASO 5 - lista de estrellas
	HashSet<JLabelEstrella> estrellas = new HashSet<JLabelEstrella>(); // PASO 5 - lista de estrellas
	
	private static long tiempoUltEstrellaCreada = 0; // PASO 5 - momento de creaci�n de la �ltima estrella
	private int puntosJuego = 0; // PASO 6 - puntuaci�n
	private int estrellasPerdidas = 0; // PASO 6 - conteo de estrellas perdidas para acabar
	
	/** Construye un mundo de juego
	 * @param panel	Panel visual del juego
	 */
	public MundoJuego( JPanel panel ) {
		this.panel = panel;
	}

		private static Random r = new Random();
	/** Si han pasado m�s de 1,1 segundos desde la �ltima,
	 * crea una estrella nueva en una posici�n aleatoria y la a�ade al mundo y al panel visual
	 */
	public void creaEstrella() {
		if (System.currentTimeMillis() - tiempoUltEstrellaCreada > 1200) {
			// Crear y a�adir la estrella a la ventana
			JLabelEstrella nuevaEstrella = new JLabelEstrella();
			nuevaEstrella.setLocation( r.nextInt( panel.getWidth()-JLabelEstrella.TAMANYO_ESTRELLA ), 
					r.nextInt( panel.getHeight()-JLabelEstrella.TAMANYO_ESTRELLA ) );  // Posici�n aleatoria
			
			while(estrellas.contains(nuevaEstrella)){
				nuevaEstrella.setLocation( r.nextInt( panel.getWidth()-JLabelEstrella.TAMANYO_ESTRELLA ), 
						r.nextInt( panel.getHeight()-JLabelEstrella.TAMANYO_ESTRELLA ) );  // Posici�n aleatoria
				
			}
			
			panel.add( nuevaEstrella );  // A�ade al panel visual
			nuevaEstrella.repaint();     // Refresca el dibujado de la estrella
			estrellas.add( nuevaEstrella );
			
			tiempoUltEstrellaCreada = System.currentTimeMillis();
		}
	}
	
	/** Quita todas las estrellas que lleven en pantalla demasiado tiempo
	 * y rota 10 grados las que sigan
	 * @param maxTiempo	Tiempo m�ximo para que se mantengan las estrellas (msegs)
	 * @return	N�mero de estrellas quitadas
	 */
	public int quitaYRotaEstrellas( long maxTiempo ) {
		int numEstFuera = 0;
		/*for (int i = estrellas.size()-1; i>=0; i--) {
			JLabelEstrella est = estrellas.get(i);
			if (System.currentTimeMillis() - est.getHoraCreacion() > maxTiempo) {
				panel.remove( est );
				panel.repaint();
				estrellas.remove( est );
				numEstFuera++;
				estrellasPerdidas++;
			} else {
				est.addGiro( 10 );
				est.repaint();
			}
		}*/
		
		Iterator it = estrellas.iterator();
        while(it.hasNext()) {
        	JLabelEstrella est =  (JLabelEstrella) it.next();
        	if (System.currentTimeMillis() - est.getHoraCreacion() > maxTiempo) {
				panel.remove( est );
				panel.repaint();
				estrellas.remove( est );
				numEstFuera++;
				estrellasPerdidas++;
			} else {
				est.addGiro( 10 );
				est.repaint();
			}
        }
		return numEstFuera;
	}

		private boolean chocaCocheConEstrella( JLabelEstrella est ) {
			double distX = est.getX()+JLabelEstrella.TAMANYO_ESTRELLA/2-miCoche.getPosX()-JLabelCoche.TAMANYO_COCHE/2;
			double distY = est.getY()+JLabelEstrella.TAMANYO_ESTRELLA/2-miCoche.getPosY()-JLabelCoche.TAMANYO_COCHE/2;
			double dist = Math.sqrt( distX*distX + distY*distY );
			return (dist <= JLabelCoche.RADIO_ESFERA_COCHE + JLabelEstrella.RADIO_ESFERA);
			// Si su distancia es menor que la suma de sus radios, es que tocan
		}
		
	/** Calcula si hay choques del coche con alguna estrella (o varias).
	 * Se considera el choque si se tocan las esferas l�gicas del coche y 
	 * la de la estrella.
	 * Si es as�, elimina las estrellas chocadas.
	 * @return	N�mero de estrellas eliminadas
	 */
	public int choquesConEstrellas() {
		int numChoques = 0;
		
		for (JLabelEstrella est:estrellas)
		{
			//for (int i=estrellas.size()-1; i>=0; i--) {
			//JLabelEstrella est = estrellas.get(i);
			if (chocaCocheConEstrella(est)) {
				Connection con = VentanaJuego.bd.getConnection();
				Statement stmt = VentanaJuego.bd.getStatement();
				String sql="UPDATE ESTRELLAS SET ELIMINADA=TRUE" +
						" WHERE X="+est.getAlignmentX()+" AND Y="+est.getAlignmentY();
				try {
					stmt.executeUpdate(sql);
					stmt.close();
					con.close();
				} catch (SQLException e1) {
					System.out.println("ERROR EN LA ACTUALIZACI�N DE LA ESTRELLA");
				}
				
				numChoques++;
				panel.remove( est );
				panel.repaint();
				estrellas.remove( est );
				puntosJuego += 5;  // PASO 6
			}
		}
		return numChoques;
	}
	
	/** Devuelve la puntuaci�n actual
	 * @return	Puntos del jugador
	 */
	public int getPuntuacion() {
		return puntosJuego;
	}
	
	/** Devuelve el n�mero de estrellas perdidas
	 * @return	N� de estrellas perdidas hasta ahora en el juego
	 */
	public int getEstrellasPerdidas() {
		return estrellasPerdidas;
	}
	
	/** Informa cu�ndo el juego ha acabado (se han perdido 10 o m�s estrellas)
	 * @return	true si el juego se ha acabado, false en caso contrario
	 */
	public boolean finJuego() {
		return (estrellasPerdidas>=10);
	}
	
	/** Crea un coche nuevo y lo a�ade al mundo y al panel visual
	 * @param posX	Posici�n X de pixel del nuevo coche
	 * @param posY	Posici�n Y de p�xel del nuevo coche
	 */
	public void creaCoche( int posX, int posY ) {
		// Crear y a�adir el coche a la ventana
		miCoche = new CocheJuego();
		miCoche.setPosicion( posX, posY );
		panel.add( miCoche.getGrafico() );  // A�ade al panel visual
		miCoche.getGrafico().repaint();  // Refresca el dibujado del coche
	}
	
	
	/** Devuelve el coche del mundo
	 * @return	Coche en el mundo. Si no lo hay, devuelve null
	 */
	public CocheJuego getCoche() {
		return miCoche;
	}

	/** Calcula si hay choque en horizontal con los l�mites del mundo
	 * @param coche	Coche cuyo choque se comprueba con su posici�n actual
	 * @return	true si hay choque horizontal, false si no lo hay
	 */
	public boolean hayChoqueHorizontal( CocheJuego coche ) {
		return (coche.getPosX() < JLabelCoche.RADIO_ESFERA_COCHE-JLabelCoche.TAMANYO_COCHE/2 
				|| coche.getPosX()>panel.getWidth()-JLabelCoche.TAMANYO_COCHE/2-JLabelCoche.RADIO_ESFERA_COCHE );
	}
	
	/** Calcula si hay choque en vertical con los l�mites del mundo
	 * @param coche	Coche cuyo choque se comprueba con su posici�n actual
	 * @return	true si hay choque vertical, false si no lo hay
	 */
	public boolean hayChoqueVertical( CocheJuego coche ) {
		return (coche.getPosY() < JLabelCoche.RADIO_ESFERA_COCHE-JLabelCoche.TAMANYO_COCHE/2 
				|| coche.getPosY()>panel.getHeight()-JLabelCoche.TAMANYO_COCHE/2-JLabelCoche.RADIO_ESFERA_COCHE );
	}

	/** Realiza un rebote en horizontal del objeto de juego indicado
	 * @param coche	Objeto que rebota en horizontal
	 */
	public void rebotaHorizontal( CocheJuego coche ) {
		corregirPosicionRec(coche);
		// System.out.println( "Choca X");
		double dir = coche.getDireccionActual();
		dir = 180-dir;   // Rebote espejo sobre OY (complementario de 180)
		if (dir < 0) dir = 360+dir;  // Correcci�n para mantenerlo en [0,360)
		coche.setDireccionActual( dir );
		
	}
	
	private void corregirPosicionRec(CocheJuego coche) {
		if ((coche.posX!=coche.posXAnt)||(coche.posY!=coche.posYAnt)){
			//corregir la posici�n del coche de forma recursiva
			if (coche.posX > coche.posXAnt)
			{
				coche.posX -= 0.1;
			}else if (coche.posX < coche.posXAnt)	
			{
				coche.posX += 0.1;
			}
			if (coche.posY > coche.posYAnt){
				coche.posY -= 0.1;
			}else if (coche.posY < coche.posYAnt){
				coche.posY += 0.1;
			}
			corregirPosicionRec(coche);
		}	
	}

	/** Realiza un rebote en vertical del objeto de juego indicado
	 * @param coche	Objeto que rebota en vertical
	 */
	public void rebotaVertical( CocheJuego coche ) {
		corregirPosicionRec(coche);
		// System.out.println( "Choca Y");
		double dir = miCoche.getDireccionActual();
		dir = 360 - dir;  // Rebote espejo sobre OX (complementario de 360)
		miCoche.setDireccionActual( dir );
	}
	
	/** Calcula y devuelve la posici�n X de un movimiento
	 * @param vel    	Velocidad del movimiento (en p�xels por segundo)
	 * @param dir    	Direcci�n del movimiento en grados (0� = eje OX positivo. Sentido antihorario)
	 * @param tiempo	Tiempo del movimiento (en segundos)
	 * @return
	 */
	public static double calcMovtoX( double vel, double dir, double tiempo ) {
		return vel * Math.cos(dir/180.0*Math.PI) * tiempo;
	}
	
	/** Calcula y devuelve la posici�n Y de un movimiento
	 * @param vel    	Velocidad del movimiento (en p�xels por segundo)
	 * @param dir    	Direcci�n del movimiento en grados (0� = eje OX positivo. Sentido antihorario)
	 * @param tiempo	Tiempo del movimiento (en segundos)
	 * @return
	 */
	public static double calcMovtoY( double vel, double dir, double tiempo ) {
		return vel * -Math.sin(dir/180.0*Math.PI) * tiempo;
		// el negativo es porque en pantalla la Y crece hacia abajo y no hacia arriba
	}

	/** Calcula el cambio de velocidad en funci�n de la aceleraci�n
	 * @param vel		Velocidad original
	 * @param acel		Aceleraci�n aplicada (puede ser negativa) en pixels/sg2
	 * @param tiempo	Tiempo transcurrido en segundos
	 * @return	Nueva velocidad
	 */
	public static double calcVelocidadConAceleracion( double vel, double acel, double tiempo ) {
		return vel + (acel*tiempo);
	}
	
	// PASO 4
	/** Calcula la fuerza de rozamiento que sufre un objeto movi�ndose
	 * @param masa
	 * @param coefRozSuelo
	 * @param coefRozAire
	 * @param vel
	 * @return
	 */
	public static double calcFuerzaRozamiento( double masa, double coefRozSuelo, double coefRozAire, double vel ) {
		double fuerzaRozamientoSuelo = masa * coefRozSuelo * ((vel>0)?(-1):1);  // En contra del movimiento
		double fuerzaRozamientoAire = coefRozAire * (-vel);  // En contra del movimiento
		return fuerzaRozamientoAire + fuerzaRozamientoSuelo;
	}
	
	/** Calcula la aceleraci�n de un objeto dada una fuerza y una masa
	 * @param fuerza	Fuerza aplicada al objeto (en Newton_pixels = Kg*pixels/sg2)
	 * @param masa	Masa del objeto
	 * @return	Aceleraci�n aplicada al objeto (en pixels/sg2)
	 */
	public static double calcAceleracionConFuerza( double fuerza, double masa ) {
		// 2� ley de Newton: F = m*a --->  a = F/m
		return fuerza/masa;
	}
	
	/** Aplica la fuerza a un coche, produci�ndose una aceleraci�n y un movimiento acorde.
	 * Se tienen en cuenta las fuerzas de rozamiento.
	 * @param fuerza	Fuerza aplicada en la direcci�n del movimiento, en Newtixels (negativo = sentido contrario)
	 * @param coche	Coche al que se aplica la fuerza
	 */
	public static void aplicarFuerza( double fuerza, Coche coche ) {
		double fuerzaRozamiento = calcFuerzaRozamiento( Coche.MASA ,
				Coche.COEF_RZTO_SUELO, Coche.COEF_RZTO_AIRE, coche.getVelocidad() );
		double aceleracion = calcAceleracionConFuerza( fuerza+fuerzaRozamiento, Coche.MASA );
		if (fuerza==0) {
			// No hay fuerza, solo se aplica el rozamiento
			double velAntigua = coche.getVelocidad();
			coche.acelera( aceleracion, 0.04 );
			if (velAntigua>=0 && coche.getVelocidad()<0
				|| velAntigua<=0 && coche.getVelocidad()>0) {
				coche.setVelocidad(0);  // Si se est� frenando, se para (no anda al rev�s)
			}
		} else {
			coche.acelera( aceleracion, 0.04 );
		}
		// System.out.println( 
		//		String.format( "Vel: %1$,1.3f / Fza: %2$,1.3f -- Rzto: %3$,1.3f ==> Acel: %4$,1.3f", 
		//				coche.getVelocidad(), fuerza, fuerzaRozamiento, aceleracion ) );
	}
	
}
