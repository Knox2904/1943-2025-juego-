package puppy.code;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;

public class BossBlackShip extends Boss {

    // --- Texturas Específicas ---
    private Texture txMinion;

    // --- Timers y Lógica ---
    private float spawnTimer = 0;
    private float tiempoEntreSpawns = 2.0f;

    private float bulletHellTimer = 0;
    private float anguloEspiral = 0;

    // --- CADENCIA BASE PARA CADA FASE ---
    private final float CADENCIA_FASE1_BASE = 0.1f;
    private float cadenciaFase1Actual = CADENCIA_FASE1_BASE;

    private final float CADENCIA_FASE2_BASE = 0.05f;
    private float cadenciaFase2Actual = CADENCIA_FASE2_BASE;

    // YA NO USAMOS ESTA CONSTANTE FIJA AQUÍ, SE PASA POR CONSTRUCTOR
    // private static final int VIDA_BASE_BLACKSHIP = 1200;

    public BossBlackShip(float x, float y, Texture tx, Texture txBala, Texture txMinion, int vidaInicial, String nombre) {
        // Pasamos 'vidaInicial' al padre (Boss) en lugar del número fijo
        super(x, y, tx, txBala, vidaInicial, nombre);

        this.txMinion = txMinion;

        // Tamaño imponente
        spr.setSize(400f, 400f);
        spr.setOriginCenter();
        hitbox.setSize(300f, 250f); // Ajusté un poco la hitbox para que no sea todo el cuadrado
        spr.setColor(0.6f, 0.4f, 0.8f, 1f); // Tono Púrpura/Oscuro
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
        float alturaCombate = 250f; // Se mantiene bajo

        // Oscilación lateral lenta y pesada
        position.x = centroX + MathUtils.sin(tiempoVida * 0.5f) * 50f;
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

        spawnTimer += delta;
        if (spawnTimer > tiempoEntreSpawns) {
            spawnTimer = 0;
            spawnearMinion(juego);
        }
    }

    // --- FASE 2: EL MURO DE LA MUERTE ---
    private void comportamientoFase2(float delta, PantallaJuego juego) {
        bulletHellTimer += delta;

        if (bulletHellTimer > cadenciaFase2Actual) {
            bulletHellTimer = 0;

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;
            float velocidadBala = 3.5f;

            anguloEspiral += 5f;

            for (int i = 0; i < 4; i++) {
                float anguloActual = anguloEspiral + (i * 90);
                float vx = MathUtils.cosDeg(anguloActual) * velocidadBala;
                float vy = MathUtils.sinDeg(anguloActual) * velocidadBala;
                juego.agregarBalaEnemiga(new Bullet(cx, cy, vx, vy, getTxBala()));
            }
        }
    }

    private void spawnearMinion(PantallaJuego juego) {
        // Spawnea minions aleatoriamente cerca de la nave
        float xSpawn = position.x + MathUtils.random(50, spr.getWidth() - 50);
        float ySpawn = position.y + spr.getHeight() / 2;

        // Kamikazes rápidos
        Kamikaze k = new Kamikaze(xSpawn, ySpawn, juego.getNave(), txMinion, 450f);
        juego.agregarEnemigo(k);
    }

    @Override
    public void aumentarDificultad(float factorRonda) {
        // Nota: Usamos 'super.aumentarDificultad' si quisieras la lógica base,
        // pero como BlackShip tiene lógica de fases muy específica, la sobrescribimos aquí.

        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();

        // 1. ESCALADO DE VIDA
        // Usamos vidaBaseInicial (que viene del constructor y se guarda en Boss)
        int nuevaVida = (int) (this.vidaBaseInicial * factorRonda * factorSalud * 1.2f);
        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. CADENCIA DE SPAWN
        this.tiempoEntreSpawns = 2.0f / factorRonda;
        if (this.tiempoEntreSpawns < 0.8f) this.tiempoEntreSpawns = 0.8f;

        // 3. CADENCIA DE DISPARO
        this.cadenciaFase1Actual = CADENCIA_FASE1_BASE / factorRonda;
        this.cadenciaFase2Actual = CADENCIA_FASE2_BASE; // Fase 2 ya es muy densa, no la aceleramos tanto
    }
}
