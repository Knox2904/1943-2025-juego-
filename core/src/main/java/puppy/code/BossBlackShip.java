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

    // --- CADENCIA BASE PARA CADA FASE ---
    private final float CADENCIA_FASE1_BASE = 0.1f;
    private float cadenciaFase1Actual = CADENCIA_FASE1_BASE;

    private final float CADENCIA_FASE2_BASE = 0.05f;
    private float cadenciaFase2Actual = CADENCIA_FASE2_BASE;

    private static final int VIDA_BASE_BLACKSHIP = 1200;


    public BossBlackShip(float x, float y, Texture tx, Texture txBala, Texture txMinion) {
        super(x, y, tx, txBala, VIDA_BASE_BLACKSHIP);

        this.txMinion = txMinion;

        spr.setSize(400f, 400f);
        spr.setOriginCenter();
        hitbox.setSize(400f, 400f);
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
        float centroX = (1200f / 2f) - (spr.getWidth() / 2f);

        // --- CORRECCIÓN DE ALTURA ---
        // Antes estaba en 750, muy arriba. Lo bajamos a 600 para que sea un blanco justo.
        float alturaCombate = 250f;

        // Oscilación lateral lenta y pesada
        position.x = centroX;
        // Flota suavemente
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

    // --- FASE 1: ESPIRAL RÁPIDA + MINIONS ---
    private void comportamientoFase1(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        // Dispara la espiral simple (lo que antes era Fase 2)
        // Velocidad media, una sola línea de balas girando
        if (bulletHellTimer > cadenciaFase1Actual) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;
            float velocidadBala = 7f;

            anguloEspiral += 12f;

            float vx = MathUtils.cosDeg(anguloEspiral) * velocidadBala;
            float vy = MathUtils.sinDeg(anguloEspiral) * velocidadBala;

            juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
        }

        // SPAWNER AGRESIVO (Controlado por tiempoEntreSpawns, ajustado en aumentarDificultad)
        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearMinion(juego);
        }
    }

    // --- FASE 2: EL MURO DE LA MUERTE (LENTO Y DENSO) ---
    private void comportamientoFase2(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        // [MODIFICADO] Usamos la cadencia ajustada por dificultad
        if (bulletHellTimer > cadenciaFase2Actual) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;

            float velocidadBala = 3.5f;

            anguloEspiral += 5f;

            // DISPARAMOS 4 BALAS A LA VEZ (Forma de Cruz / Molino)
            for (int i = 0; i < 4; i++) {
                float anguloActual = anguloEspiral + (i * 90);

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

    // --- 1. Lógica de Dificultad Aumentada ---
    @Override
    public void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();

        // 1. ESCALADO DE VIDA (Común, basado en Boss.vidaBaseInicial)
        int nuevaVida = (int) (this.vidaBaseInicial * factorRonda * factorSalud * 1.2f); // 1.2x por ser nave pesada
        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. CADENCIA DE SPAWN (Fase 1: Se acelera)
        this.tiempoEntreSpawns = 2.0f / factorRonda;
        if (this.tiempoEntreSpawns < 0.8f) {
            this.tiempoEntreSpawns = 0.8f;
        }

        // 3. CADENCIA DE DISPARO (Fase 1: Se acelera)
        this.cadenciaFase1Actual = CADENCIA_FASE1_BASE / factorRonda;

        // 4. CADENCIA DE DISPARO (Fase 2: Se mantiene fija, ya es densa)
        this.cadenciaFase2Actual = CADENCIA_FASE2_BASE;

        // Nota: Las velocidades de los minions deben escalarse dentro de spawnearMinion si es necesario.
    }





}
