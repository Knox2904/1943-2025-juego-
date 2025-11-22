package puppy.code;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class EnemigoT1 implements OleadaFactory {
    private Texture txAsteroide;
    private Texture txKamikaze;
    private Texture txEliminaBuffs; // <--- Nueva textura

    public EnemigoT1(Texture txAsteroide, Texture txKamikaze, Texture txEliminaBuffs) {
        this.txAsteroide = txAsteroide;
        this.txKamikaze = txKamikaze;
        this.txEliminaBuffs = txEliminaBuffs;
    }

    @Override
    public EntidadJuego createEnemigoT1(float x, float y, PantallaJuego juego) {
        return new Ball2(x, y, 30, 150f, txAsteroide);
    }

    @Override
    public EntidadJuego createEnemigoT2(float x, float y, PantallaJuego juego) {
        // LÓGICA DE PROBABILIDAD:
        // 30% EliminaBuffs, 70% Kamikaze Normal
        if (MathUtils.randomBoolean(0.3f)) {
            return new EliminaBuffs(x, y, txEliminaBuffs);
        } else {
            return new Kamikaze(x, y, juego.getNave(), txKamikaze, 300f);
        }
    }
}
