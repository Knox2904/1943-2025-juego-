package puppy.code;
import com.badlogic.gdx.graphics.Texture;

public class EnemigoT3 implements OleadaFactory {
    // Texturas necesarias
    private Texture txCargueroSmall;
    private Texture txCargueroBig;
    private Texture txKamikaze;
    private Texture txKamikazeS;
    private Texture txTank;
    private Texture txBala;
    private Texture txEliminaBuffs;

    public EnemigoT3(Texture txSmall, Texture txBig, Texture txKam, Texture txKamS, Texture txTank, Texture txEliminaBuffs , Texture txBala) {
        this.txCargueroSmall = txSmall;
        this.txCargueroBig = txBig;
        this.txKamikaze = txKam;
        this.txKamikazeS = txKamS;
        this.txTank = txTank;
        this.txBala = txBala;
        this.txEliminaBuffs = txEliminaBuffs;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        // Nivel 3 Débil: Carguero Pequeño
        // (Ya no es tan débil, ¡el nivel 3 es difícil!)
        return new Carguero(x, y, txCargueroSmall, txKamikaze, txKamikazeS,txEliminaBuffs , txBala);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // Nivel 3 Fuerte: Carguero Pesado
        return new CargueroPesado(x, y, txCargueroBig, txTank, txBala, txCargueroSmall, txKamikaze, txKamikazeS , txEliminaBuffs);
    }
    @Override
    public EntidadJuego createEnemigoT3(float x, float y, PantallaJuego juego) {
        // Por ahora, esta fábrica no tiene un "Especial Tier 3", retornamos null.
        return null;
    }
}
