package examen.bilbao201411;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.*;
import java.io.*;
import java.sql.*;
import java.util.Arrays;
import java.util.List;

import javax.swing.*;

/** Clase principal de minijuego de coche para Práctica 02 - Prog III
 * Ventana del minijuego.
 * @author Andoni Eguíluz
 * Facultad de Ingeniería - Universidad de Deusto (2014)
 */
public class VentanaJuego extends JFrame {
	private static final long serialVersionUID = 1L;  // Para serialización
	JPanel pPrincipal;         // Panel del juego (layout nulo)
	JLabel lMensaje;           // Mensajes del juego
	MundoJuego miMundo;        // Mundo del juego
	CocheJuego miCoche;        // Coche del juego
	MiRunnable miHilo = null;  // Hilo del bucle principal de juego	
	static Integer codsTeclasControladas[] = { KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT }; 
	static List<Integer> listaTeclas = Arrays.asList( codsTeclasControladas );
		// Guardan las teclas que vamos a controlar - asociado con el siguiente array:
	boolean teclasPulsadas[];  // Información de teclas pulsadas (true) o no (false)
	static BaseDeDatos bd;
	/** Constructor de la ventana de juego. Crea y devuelve la ventana inicializada
	 * sin coches dentro
	 */
	public VentanaJuego() {
		// Liberación de la ventana por defecto al cerrar
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		// Creación contenedores y componentes
		pPrincipal = new JPanel();
		JPanel pBotonera = new JPanel();
		lMensaje = new JLabel( " " ); // PASO 6
		// Formato y layouts
		pPrincipal.setLayout( null );
		pPrincipal.setBackground( Color.white );
		// Añadido de componentes a contenedores
		add( pPrincipal, BorderLayout.CENTER );
		JButton bGuardar = new JButton("vuelvo juego");
		pBotonera.setLayout(new BorderLayout());
		pBotonera.add( "Center",lMensaje );  // PASO 6
		pBotonera.add("East", bGuardar);
		add( pBotonera, BorderLayout.SOUTH );
		// Formato de ventana
		setSize( 1000, 750 );
		setResizable( false );
		
		teclasPulsadas = new boolean[ codsTeclasControladas.length ];  // se inicializa a falses
		pPrincipal.addKeyListener( new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (listaTeclas.contains(e.getKeyCode())) {
					// Si la tecla es de las que controlamos (cursores)
					teclasPulsadas[ listaTeclas.indexOf( e.getKeyCode() ) ] = true;
				}
			}
			@Override
			public void keyReleased(KeyEvent e) {
				if (listaTeclas.contains(e.getKeyCode())) {
					// Si la tecla es de las que controlamos (cursores)
					teclasPulsadas[ listaTeclas.indexOf( e.getKeyCode() ) ] = false;
				}
			}
		} );
		pPrincipal.setFocusable(true);
		pPrincipal.requestFocus();
		pPrincipal.addFocusListener( new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				pPrincipal.requestFocus();
			}
		});
		// Cierre del hilo al cierre de la ventana
		addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				if (miHilo!=null) miHilo.acaba();
			}
		});
		
		bGuardar.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
					guardarPartida();	
			}
		});
		
	}
	
	protected void guardarPartida() {
		try {
			FileOutputStream fos = new FileOutputStream("juegocoche-lastgame.dat");
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(miMundo);
			oos.writeObject(null);
			oos.close();
		} catch (FileNotFoundException e) {
			System.out.println("ERROR al guardarPartida 1");
		}catch (IOException e) {
			System.out.println("ERROR al guardarPartida 2");
		}
	}

	/** Programa principal de la ventana de juego
	 * @param args
	 */
	public static void main(String[] args) {
		// Crea y visibiliza la ventana con el coche
		try {
			final VentanaJuego miVentana = new VentanaJuego();
			SwingUtilities.invokeAndWait( new Runnable() {
				@Override
				public void run() {
					miVentana.setVisible( true );
				}
			});
			bd = new BaseDeDatos();
			bd.initBD("bd.sqlite");
			try {
				FileInputStream fis = new FileInputStream("juegocoche-lastgame.dat");
				ObjectInputStream ois = new ObjectInputStream(fis);
				miVentana.miMundo = (MundoJuego)ois.readObject();
				miVentana.miMundo.panel = miVentana.pPrincipal;
				miVentana.miMundo.creaCoche( (int)miVentana.miMundo.getCoche().getPosX(), (int)miVentana.miMundo.getCoche().getPosY() );
				ois.close();
			} catch (FileNotFoundException e) {
				System.out.println("Fichero no existe");
				miVentana.miMundo = new MundoJuego( miVentana.pPrincipal );
				miVentana.miMundo.creaCoche( 150, 100 );
			}catch (IOException e) {
				System.out.println("ERROR al prepararlo para lectura");
			}
			
			miVentana.miCoche = miVentana.miMundo.getCoche();
			miVentana.miCoche.setPiloto( "Fernando Alonso" );
			// Crea el hilo de movimiento del coche y lo lanza
			miVentana.miHilo = miVentana.new MiRunnable();  // Sintaxis de new para clase interna
			Thread nuevoHilo = new Thread( miVentana.miHilo );
			nuevoHilo.start();
		} catch (Exception e) {
			System.exit(1);  // Error anormal
		}
	}
	
	/** Clase interna para implementación de bucle principal del juego como un hilo
	 * @author Andoni Eguíluz
	 * Facultad de Ingeniería - Universidad de Deusto (2014)
	 */
	class MiRunnable implements Runnable {
		boolean sigo = true;
		@Override
		public void run() {
			// Bucle principal forever hasta que se pare el juego...
			while (sigo) {
				// Chequear choques
				// (se comprueba tanto X como Y porque podría a la vez chocar en las dos direcciones (esquinas)
				if (miMundo.hayChoqueHorizontal(miCoche)) // Espejo horizontal si choca en X
					miMundo.rebotaHorizontal(miCoche);
				if (miMundo.hayChoqueVertical(miCoche)) // Espejo vertical si choca en Y
					miMundo.rebotaVertical(miCoche);
				double fuerzaAceleracion = 0;
				// Actuación de teclas
				if (teclasPulsadas[listaTeclas.indexOf(KeyEvent.VK_UP)]) {
					fuerzaAceleracion = miCoche.fuerzaAceleracionAdelante();
				}
				if (teclasPulsadas[listaTeclas.indexOf(KeyEvent.VK_DOWN)]) {
					fuerzaAceleracion = -miCoche.fuerzaAceleracionAtras();
				}
				MundoJuego.aplicarFuerza( fuerzaAceleracion, miCoche );
				if (teclasPulsadas[listaTeclas.indexOf(KeyEvent.VK_LEFT)]) miCoche.gira( +10 );
				if (teclasPulsadas[listaTeclas.indexOf(KeyEvent.VK_RIGHT)]) miCoche.gira( -10 );				
				// Mover coche
				miCoche.mueve( 0.040 );
				// Acciones sobre el mundo del juego
				int estrellasPerdidas = miMundo.quitaYRotaEstrellas( 6000 );  // Quita estrellas que lleven más de 6 segundos
				if (estrellasPerdidas > 0) {
					String mensaje = "Puntos: " + miMundo.getPuntuacion();
					mensaje += "  -  ESTRELLAS PERDIDAS: " + miMundo.getEstrellasPerdidas();
					lMensaje.setText( mensaje );
				}
				miMundo.creaEstrella();  // Crea estrella si procede (si han pasado 2 segundos)
				//insertamos la estrella creada en la BD
				Connection con = bd.getConnection();
				Statement stmt = bd.getStatement();
				int size=0;
				JLabelEstrella nuevaE = null;
				for(JLabelEstrella est: miMundo.estrellas)
				{
					if (size==miMundo.estrellas.size()-1)
						nuevaE=est;
					size++;
				}	
				String sql="INSERT INTO ESTRELLAS VALUES("+nuevaE.getAlignmentX()+","+
				                      nuevaE.getAlignmentY()+",0,0)";
				try {
					stmt.executeUpdate(sql);
					stmt.close();
					con.close();
				} catch (SQLException e1) {
					System.out.println("ERROR EN LA INSERCION DE LA ESTRELLA: "+sql);
				}
				
				int choquesEstrellas = miMundo.choquesConEstrellas();
				if (choquesEstrellas>0) {
					
					String mensaje = "Puntos: " + miMundo.getPuntuacion();
					lMensaje.setText( mensaje );
				}
				if (miMundo.finJuego()) {  // Acabar el juego
					sigo = false;
					lMensaje.setText( "SE ACABÓ EL JUEGO!!! Has sacado " + 
							miMundo.getPuntuacion() + " puntos." );
					// Dormir el hilo 3000 milisegundos antes de acabar
					try {
						Thread.sleep( 3000 );
					} catch (Exception e) {
					}
					VentanaJuego.this.dispose();
				}
				// Dormir el hilo 40 milisegundos entre paso y paso del bucle principal del juego
				try {
					Thread.sleep( 40 );
				} catch (Exception e) {
				}
			}
		}
		/** Ordena al hilo detenerse en cuanto sea posible
		 */
		public void acaba() {
			sigo = false;
		}
	};
	
}
