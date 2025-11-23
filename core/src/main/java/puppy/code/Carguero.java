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

        // Ajuste de tamaño visual
        spr.setSize(150, 150); // (Bajé de 500 a 300 para que no sea monstruoso)
        spr.setOriginCenter();

        // Ajuste de tamaño físico (Hitbox)
        this.hitbox.setSize(150, 150);
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

        // 2. Spawner (Cada 2 segundos)
        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearHijo(juego);
        }

        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                this.spr.setColor(1, 1, 1, 1); // Volver a Blanco
            }
        }
        // --- CORRECCIÓN: LIMPIEZA ---
        // Si por alguna razón sale muy abajo (ej: empujado), muere.
        if (position.y < -200) {
            destruir();
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

    // Variable nueva que debes agregar a la clase para controlar el ritmo de spawn
    // (Actualmente usas '2.0f' o '3.0f' fijos en el update, cámbialos por esta variable)
    private float tiempoEntreSpawns = 2.0f; // (O 3.0f para el Pesado)

    public void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();
        float factorVelocidad = BuffManager.getInstance().getEnemySpeedMultiplier();

        // 1. VELOCIDAD (Aumenta poco, son naves pesadas)
        // Usamos 0.2f para que no se vuelvan ferraris en rondas altas
        this.velocidadPEI *= (1 + (factorRonda - 1) * 0.2f) * factorVelocidad;

        // 2. VIDA (Escala MUCHO)
        // Multiplicamos por 1.2 extra porque son naves grandes que deben aguantar
        this.vidaActual = (int) (this.vidaActual * factorRonda * factorSalud * 1.2f);

        // 3. CADENCIA DE SPAWN (En vez de disparo)
        // Si factorRonda es 2.0, spawnean el doble de rápido.
        this.tiempoEntreSpawns = 2.0f / factorRonda; // (O 3.0f / factorRonda para el Pesado)

        // Tope de seguridad: Que no spawneen más rápido que cada 0.8 segundos
        if (this.tiempoEntreSpawns < 0.8f) {
            this.tiempoEntreSpawns = 0.8f;
        }
    }




}
