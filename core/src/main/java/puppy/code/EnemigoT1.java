package puppy.code;
import com.badlogic.gdx.graphics.Texture;

public class EnemigoT1 implements OleadaFactory {
    private Texture txAsteroide;
    private Texture txKamikaze;

    public EnemigoT1(Texture txAsteroide, Texture txKamikaze) {
        this.txAsteroide = txAsteroide;
        this.txKamikaze = txKamikaze;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        // Nivel 1 Débil: Ball2 (Asteroide)
        return new Ball2(x, y, 30, 150f, txAsteroide);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // Nivel 1 Fuerte: Kamikaze (Perseguidor)
        return new Kamikaze(x, y, juego.getNave(), txKamikaze, 300f);
    }

    //@Override
    /*public EntidadJuego createBoss(float x, float y, PantallaJuego juego) {
        Boss temporal: Kamikaze muy rápido
        Boss temporal : un tanque mas rapido y que dispare
        por ahora solo ideas
    }
    enemigo sin crear todavia..... , solo ideas para bosses
     */
}
