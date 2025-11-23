package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class BossBlackShip extends Boss {

    // --- Texturas Específicas ---
    private Texture txMinion;

    // --- Timers y Lógica ---
    private float spawnTimer = 0;
    private float tiempoEntreSpawns = 2.0f; // ¡Más spawns en fase 1!

    private float bulletHellTimer = 0;
    private float anguloEspiral = 0;

    public BossBlackShip(float x, float y, Texture tx, Texture txBala, Texture txMinion) {
        super(x, y, tx, txBala, 1200);

        this.txMinion = txMinion;

        spr.setSize(300, 200);
        spr.setOriginCenter();
        hitbox.setSize(250, 150);
        spr.setColor(0.6f, 0.4f, 0.8f, 1f);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        super.update(delta, juego);

        // Visual: En fase 2 parpadea más rápido y oscuro
        if (this.fase == 2 && !enHit) {
            float parpadeo = 0.5f + MathUtils.sin(tiempoVida * 15) * 0.2f;
            spr.setColor(0.6f, 0f, 0.8f, parpadeo);
        }
    }

    @Override
    protected void movimientoCombate(float delta) {
        float centroX = (1200 / 2) - (spr.getWidth() / 2);

        // --- CORRECCIÓN DE ALTURA ---
        // Antes estaba en 750, muy arriba. Lo bajamos a 600 para que sea un blanco justo.
        float alturaCombate = 600f;

        // Oscilación lateral lenta y pesada
        position.x = centroX + MathUtils.sin(tiempoVida * 0.3f) * 200f;
        // Flota suavemente
        position.y = alturaCombate + MathUtils.cos(tiempoVida * 0.5f) * 20f;
    }

    @Override
    protected void disparar(float delta, PantallaJuego juego) {
        if (this.fase == 1) {
            comportamientoFase1(delta, juego);
        } else if (this.fase == 2) {
            comportamientoFase2(delta, juego);
        }
    }

    // --- FASE 1: ESPIRAL RÁPIDA + MINIONS ---
    private void comportamientoFase1(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        // Dispara la espiral simple (lo que antes era Fase 2)
        // Velocidad media, una sola línea de balas girando
        if (bulletHellTimer > 0.1f) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;
            float velocidadBala = 7f; // Velocidad normal

            anguloEspiral += 12f; // Giro medio

            float vx = MathUtils.cosDeg(anguloEspiral) * velocidadBala;
            float vy = MathUtils.sinDeg(anguloEspiral) * velocidadBala;

            juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
        }

        // SPAWNER AGRESIVO
        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearMinion(juego);
        }
    }

    // --- FASE 2: EL MURO DE LA MUERTE (LENTO Y DENSO) ---
    private void comportamientoFase2(float delta, PantallaJuego juego) {
        // SIN SPAWNS, SOLO BALAS
        bulletHellTimer += delta;

        // Cadencia MUY alta (0.05s), pero balas LENTAS
        if (bulletHellTimer > 0.05f) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;

            // BALAS LENTAS: Esto crea el efecto de "muro" que se acumula
            float velocidadBala = 3.5f;

            // Giro suave
            anguloEspiral += 5f;

            // DISPARAMOS 4 BALAS A LA VEZ (Forma de Cruz / Molino)
            for (int i = 0; i < 4; i++) {
                float anguloActual = anguloEspiral + (i * 90); // 0, 90, 180, 270

                float vx = MathUtils.cosDeg(anguloActual) * velocidadBala;
                float vy = MathUtils.sinDeg(anguloActual) * velocidadBala;

                juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
            }
        }
    }

    private void spawnearMinion(PantallaJuego juego) {
        float xSpawn = position.x + MathUtils.random(20, spr.getWidth() - 20);
        float ySpawn = position.y;

        // Kamikazes rápidos para molestar mientras esquivas la espiral
        Kamikaze k = new Kamikaze(xSpawn, ySpawn, juego.getNave(), txMinion, 450f);
        juego.agregarEnemigo(k);
    }
}
