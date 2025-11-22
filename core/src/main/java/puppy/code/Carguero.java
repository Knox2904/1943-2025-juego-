package puppy.code;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class Carguero extends EntidadJuego {

    private float spawnTimer = 0;

    // Texturas para sus "hijos"
    private Texture txKamikaze;
    private Texture txKamikazeS;
    private Texture txEliminaBuffs;
    private Texture txBala; // Para el KamikazeS

    public Carguero(float x, float y, Texture txSelf, Texture txKamikaze, Texture txKamikazeS, Texture txEliminaBuffs,  Texture txBala) {
        // Velocidad: 80, Vida: 15 (Resistente pero no tanque)
        super(txSelf, x, y, 70f, 15);
        this.txKamikaze = txKamikaze;
        this.txKamikazeS = txKamikazeS;
        this.txEliminaBuffs = txEliminaBuffs;
        this.txBala = txBala;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        // 1. Movimiento: Baja hasta el cuarto superior y patrulla
        if (position.y > 600) {
            position.y -= velocidadPEI * delta;
        } else {
            // Patrulla lateral suave
            position.x += MathUtils.sin(Gdx.graphics.getFrameId() * 0.01f) * 0.5f;
        }
        spr.setPosition(position.x, position.y);

        // 2. Spawner (Cada 3 segundos)
        spawnTimer += delta;
        if (spawnTimer > 3.0f) {
            spawnTimer = 0;
            spawnearHijo(juego);
        }
    }

    private void spawnearHijo(PantallaJuego juego) {
        float x = this.position.x + spr.getWidth()/2;
        float y = this.position.y;

        float r = MathUtils.random();

        if (r < 0.4f) {
            // 40% Kamikaze Normal (Perseguidor)
            juego.agregarEnemigo(new Kamikaze(x, y, juego.getNave(), txKamikaze, 350f));
        } else if (r < 0.8f) {
            // 40% KamikazeS (Sinusoidal que dispara)
            juego.agregarEnemigo(new KamikazeS(x, y, juego.getNave(), txKamikazeS, txBala, 200f));
        } else {
            // 20% EliminaBuffs
            // (Reutilizamos la textura del kamikaze si no tienes una específica para este)
            juego.agregarEnemigo(new EliminaBuffs(x, y, txEliminaBuffs));
        }
    }
}
