package puppy.code;
import com.badlogic.gdx.graphics.Texture;

public class EnemigoT2 implements OleadaFactory {
    private Texture txKamikazeS; // Textura para KamikazeS
    private Texture txTank;      // Textura para NaveTanque
    private Texture txBala;      // Textura para las balas enemigas

    public EnemigoT2(Texture txKamikazeS, Texture txTank, Texture txBala) {
        this.txKamikazeS = txKamikazeS;
        this.txTank = txTank;
        this.txBala = txBala;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        // Nivel 2 Débil: KamikazeS (Sinusoidal)
        // Constructor: (x, y, objetivo, texturaNave, texturaBala, velocidad)
        return new KamikazeS(x, y, juego.getNave(), txKamikazeS, txBala, 200f);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // Nivel 2 Fuerte: NaveTanque
        return new NaveTanque(x, y, txTank, txBala);
    }

    /*@Override
    public EntidadJuego createBoss(float x, float y, PantallaJuego juego) {
        // Boss Nivel 2: NaveTanque (


        solo ideas por ahora
    }

     */
}
