package examen.bilbao201411;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.io.Serializable;

import javax.swing.ImageIcon;
import javax.swing.JLabel;

/** Clase para visualizar un objeto gráfico en Swing como un JLabel,
 *  con un gráfico definido al construir
 * @author Andoni Eguíluz
 */
public class JLabelEstrella extends JLabel implements Serializable {
	private static final long serialVersionUID = 1L;  // Para serialización
	public static final int TAMANYO_ESTRELLA = 40;  // Tamaño cuadrado de la estrella
	public static final int RADIO_ESFERA = 17;  // Radio en píxels del bounding circle de la estrella (para choques)
	private static boolean DIBUJAR_ESFERA = false; // Dibujado (para depuración) del bounding circle de choque de la estrella
	private static boolean DIBUJAR_RECTANGULO = false; // Dibujado (para depuración) del rectángulo del label de la estrella
	private long horaCreacion = System.currentTimeMillis();
	
	/** Construye y devuelve el JLabel de la estrella con su gráfico y tamaño
	 */
	public JLabelEstrella() {
		try {
			setIcon( new ImageIcon( JLabelEstrella.class.getResource( "img/estrella.png" ).toURI().toURL() ) );
		} catch (Exception e) {
			System.err.println( "Error en carga de recurso: estrella.png no encontrado" );
			e.printStackTrace();
		}
		setBounds( 0, 0, TAMANYO_ESTRELLA, TAMANYO_ESTRELLA );
	}
	
	// giro
	private double miGiro = 0;
	/** Cambia el giro del JLabel
	 * @param gradosGiro	Grados de giro de la estrella,
	 * 						considerados con el 0 en el eje OX positivo,
	 * 						positivo en sentido antihorario, negativo horario.
	 */
	public void setGiro( double gradosGiro ) {
		// De grados a radianes...
		miGiro = gradosGiro/180*Math.PI;
		// El giro en la pantalla es en sentido horario (inverso):
		miGiro = -miGiro;  // Cambio el sentido del giro
		if (miGiro > Math.PI*2) miGiro -= Math.PI*2;
		else if (miGiro < 0) miGiro += Math.PI*2;
	}
	
	/** Gira el JLabel
	 * @param gradosGiro	Grados a girar (en sentido antihorario)
	 */
	public void addGiro( double gradosGiro ) {
		miGiro -= gradosGiro/180*Math.PI;
	}
	
	/** Devuelve el giro del JLabel
	 * @return	Grados de giro (0 -> Eje OX positivo, sentido antihorario)
	 */
	public double getGiro() {
		return -miGiro*180/Math.PI;
	}
	
	/** Devuelve la hora de creación del objeto
	 * @return	milisegundos, de acuerdo a la hora del sistema
	 */
	public long getHoraCreacion() {
		return horaCreacion;
	}

	// Redefinición del paintComponent para que se escale y se rote el gráfico
	@Override
	protected void paintComponent(Graphics g) {
//		super.paintComponent(g);   // En este caso no nos sirve el pintado normal de un JLabel
		Image img = ((ImageIcon)getIcon()).getImage();
		Graphics2D g2 = (Graphics2D) g;  // El Graphics realmente es Graphics2D
		// Escalado más fino con estos 3 parámetros:
		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.setRenderingHint(RenderingHints.KEY_RENDERING,RenderingHints.VALUE_RENDER_QUALITY);
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,RenderingHints.VALUE_ANTIALIAS_ON);	
        if (DIBUJAR_RECTANGULO) g2.drawRect(0, 0, TAMANYO_ESTRELLA-1, TAMANYO_ESTRELLA-1);
		// Prepara rotación (siguientes operaciones se rotarán)
        g2.rotate( miGiro, TAMANYO_ESTRELLA/2, TAMANYO_ESTRELLA/2 ); // getIcon().getIconWidth()/2, getIcon().getIconHeight()/2 );
        // Dibujado de la imagen
        g2.drawImage( img, 0, 0, TAMANYO_ESTRELLA, TAMANYO_ESTRELLA, null );
        if (DIBUJAR_ESFERA) g2.drawOval( TAMANYO_ESTRELLA/2-RADIO_ESFERA, TAMANYO_ESTRELLA/2-RADIO_ESFERA,
        		RADIO_ESFERA*2, RADIO_ESFERA*2 );
	}
	
	@Override
	public boolean equals(Object obj){
		JLabelEstrella est = (JLabelEstrella)obj;
		if (est.getLocation().equals(this.getLocation()))
			return true;
		else
			return false;
	}
	
}
