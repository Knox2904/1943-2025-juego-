package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class BossBlackShip extends Boss {

    // --- Texturas Específicas ---
    private Texture txMinion;

    // --- Timers y Lógica ---
    private float spawnTimer = 0;
    // [BALANCEO] Aumentamos tiempo base entre spawns (antes 2.0f)
    private float tiempoEntreSpawns = 3.5f;

    private float bulletHellTimer = 0;
    private float anguloEspiral = 0;

    // --- CADENCIA BASE PARA CADA FASE ---
    // [BALANCEO] Disparar más lento para dejar huecos esquivables
    // Antes 0.1f (10 balas/seg) -> Ahora 0.15f (6 balas/seg)
    private final float CADENCIA_FASE1_BASE = 0.15f;
    private float cadenciaFase1Actual = CADENCIA_FASE1_BASE;

    // Antes 0.05f (20 balas/seg) -> Ahora 0.12f (8 balas/seg)
    private final float CADENCIA_FASE2_BASE = 0.12f;
    private float cadenciaFase2Actual = CADENCIA_FASE2_BASE;

    public BossBlackShip(float x, float y, Texture tx, Texture txBala, Texture txMinion, int vidaInicial, String nombre) {
        super(x, y, tx, txBala, vidaInicial, nombre);

        this.txMinion = txMinion;

        spr.setSize(400f, 400f);
        spr.setOriginCenter();
        // Ajustamos hitbox para que sea un poco más permisiva (centro del jefe)
        hitbox.setSize(300f, 250f);
        spr.setColor(0.6f, 0.4f, 0.8f, 1f);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        super.update(delta, juego);

        if (this.fase == 2 && !enHit) {
            float parpadeo = 0.5f + MathUtils.sin(tiempoVida * 15) * 0.2f;
            spr.setColor(0.6f, 0f, 0.8f, parpadeo);
        }
    }

    @Override
    protected void movimientoCombate(float delta) {
        float centroX = (1200f / 2f) - (spr.getWidth() / 2f);
        float alturaCombate = 250f;

        position.x = centroX + MathUtils.sin(tiempoVida * 0.5f) * 50f;
        position.y = alturaCombate + MathUtils.sin(tiempoVida * 0.5f) * 10f;
    }

    @Override
    protected void disparar(float delta, PantallaJuego juego) {
        if (this.fase == 1) {
            comportamientoFase1(delta, juego);
        } else if (this.fase == 2) {
            comportamientoFase2(delta, juego);
        }
    }

    // --- FASE 1: ESPIRAL ABIERTA + MINIONS ---
    private void comportamientoFase1(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        if (bulletHellTimer > cadenciaFase1Actual) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;
            float velocidadBala = 7f;

            // [BALANCEO] Aumentamos el giro para abrir más la espiral
            // Antes 12f -> Ahora 18f (Deja huecos más grandes)
            anguloEspiral += 18f;

            float vx = MathUtils.cosDeg(anguloEspiral) * velocidadBala;
            float vy = MathUtils.sinDeg(anguloEspiral) * velocidadBala;

            juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
        }

        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearMinion(juego);
        }
    }

    // --- FASE 2: EL MURO DE LA MUERTE (LENTO) ---
    private void comportamientoFase2(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        if (bulletHellTimer > cadenciaFase2Actual) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;

            // [BALANCEO] Balas un poco más lentas para dar tiempo a reaccionar
            float velocidadBala = 3.2f; // Antes 3.5f

            // [BALANCEO] Giro lento para barrer la pantalla
            anguloEspiral += 8f;

            // Disparamos 4 balas en cruz
            for (int i = 0; i < 4; i++) {
                float anguloActual = anguloEspiral + (i * 90);
                float vx = MathUtils.cosDeg(anguloActual) * velocidadBala;
                float vy = MathUtils.sinDeg(anguloActual) * velocidadBala;
                juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
            }
        }
    }

    private void spawnearMinion(PantallaJuego juego) {
        // Limitar minions en pantalla para no saturar
        if (juego.getEnemigos().size() > 20) return;

        float xSpawn = position.x + MathUtils.random(50, spr.getWidth() - 50);
        float ySpawn = position.y + spr.getHeight() / 2;

        Kamikaze k = new Kamikaze(xSpawn, ySpawn, juego.getNave(), txMinion, 450f);
        juego.agregarEnemigo(k);
    }

    @Override
    public void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();

        // 1. VIDA (Se mantiene tanque)
        int nuevaVida = (int) (this.vidaBaseInicial * factorRonda * factorSalud * 1.2f);
        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. SPAWN (Se acelera con la ronda pero con tope)
        this.tiempoEntreSpawns = 3.5f / factorRonda;
        if (this.tiempoEntreSpawns < 1.0f) this.tiempoEntreSpawns = 1.0f;

        // 3. DISPARO (Escalado más suave)
        // Dividimos por la raíz cuadrada del factor para que no escale tan agresivamente
        // Si Ronda 10 (Factor 2.0): sqrt(2) = 1.41 -> Cadencia 0.10s (Jugable)
        // Antes era lineal -> Cadencia 0.07s (Imposible)
        float factorSuave = (float) Math.sqrt(factorRonda);

        this.cadenciaFase1Actual = CADENCIA_FASE1_BASE / factorSuave;
        if (this.cadenciaFase1Actual < 0.05f) this.cadenciaFase1Actual = 0.05f;

        this.cadenciaFase2Actual = CADENCIA_FASE2_BASE / factorSuave;
        if (this.cadenciaFase2Actual < 0.06f) this.cadenciaFase2Actual = 0.06f;
    }
}
