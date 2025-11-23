package puppy.code;


import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

public class Boss extends EntidadJuego {

    protected int vidaMax;
    private float fireTimer = 0;
    private float tiempoEntreDisparos = 1.0f; // Dispara cada segundo
    private Texture txBala;
    protected int vidaBaseInicial;

    // Fases del Jefe
    // 0: Entrando, 1: Fase Normal, 2: Fase Furiosa (50% vida)
    protected int fase = 0;
    protected float tiempoVida = 0;

    public Boss(float x, float y, Texture tx, Texture txBala , int vidaInicial) {
        // Velocidad baja (se mueve lento), Vida 500 (¡Es un tanque!)
        super(tx, x, y, 60f, vidaInicial);
        this.vidaBaseInicial = vidaInicial ;
        this.vidaMax = vidaInicial;
        this.txBala = txBala;


        // LO HACEMOS GIGANTE
        spr.setSize(250, 150);
        spr.setOriginCenter();
        hitbox.setSize(200, 120); // Hitbox un poco más chica para ser justos

        // Color distintivo (Rojo oscuro o algo imponente)
        spr.setColor(1f, 0.6f, 0.6f, 1f);
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        tiempoVida += delta;

        // --- MÁQUINA DE ESTADOS (FASES) ---

        switch (fase) {
            case 0:
                // Baja lentamente hasta la posición de combate (Y = 600)
                if (position.y > 600) {
                    position.y -= 50 * delta;
                } else {
                    fase = 1; // Empieza la pelea
                }
                break;

            case 1: // COMBATE NORMAL
            case 2: // COMBATE FURIOSO
                movimientoCombate(delta);
                disparar(delta, juego);
                break;
        }

        spr.setPosition(position.x, position.y);

        // Cambiar de fase si baja del 50% de vida
        if (fase == 1 && vidaActual < vidaMax / 2) {
            fase = 2;
            spr.setColor(1f, 0.2f, 0.2f, 1f); // Se pone ROJO FURIA
            this.tiempoEntreDisparos = 0.6f; // Dispara más rápido
        }

        // Feedback visual de golpe (igual que los otros)
        if (enHit) {
            tiempoHit -= delta;
            if (tiempoHit <= 0) {
                enHit = false;
                // Restauramos color según la fase
                if (fase == 2) spr.setColor(1f, 0.2f, 0.2f, 1f);
                else spr.setColor(1f, 0.6f, 0.6f, 1f);
            }
        }
    }

    protected void movimientoCombate(float delta) {
        // Movimiento en "Ocho" (Infinito) lento
        position.x = (1200 / 2 - spr.getWidth()/2) + MathUtils.sin(tiempoVida * 0.5f) * 300f;
        position.y = 600 + MathUtils.cos(tiempoVida * 1.0f) * 50f;
    }

    protected void disparar(float delta, PantallaJuego juego) {
        fireTimer += delta;
        if (fireTimer > tiempoEntreDisparos) {
            fireTimer = 0;

            float centroX = position.x + spr.getWidth() / 2;
            float centroY = position.y + 20;

            // PATRÓN: LLUVIA DE BALAS
            if (fase == 1) {
                // Dispara abanico de 5 balas
                crearAbanico(juego, centroX, centroY, 5, 60); // 60 grados de apertura
            } else {
                // FASE 2: BULLET HELL TOTAL
                // Dispara abanico de 9 balas
                crearAbanico(juego, centroX, centroY, 9, 120);
            }
        }
    }

    private void crearAbanico(PantallaJuego juego, float x, float y, int cantidadBalas, float aperturaGrados) {
        float anguloInicial = 270 - (aperturaGrados / 2); // 270 es abajo
        float paso = aperturaGrados / (cantidadBalas - 1);

        for (int i = 0; i < cantidadBalas; i++) {
            float angulo = anguloInicial + (paso * i);
            float velX = 8f * MathUtils.cosDeg(angulo);
            float velY = 8f * MathUtils.sinDeg(angulo);

            juego.agregarBalaEnemiga(new Bullet(x, y, velX, velY, txBala));
        }
    }

    @Override
    public Rectangle getHitbox() {
        // Centrar hitbox
        hitbox.setPosition(position.x + 25, position.y + 15);
        return hitbox;
    }

    // Getters para la barra de vida
    public int getVidaActual() { return vidaActual; }
    public int getVidaMax() { return vidaMax; }

    public Texture getTxBala() {
        return this.txBala;
    }

    protected void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();

        // 1. ESCALADO DE VIDA (Común a todos los jefes)
        // La vida escalada se calcula a partir del valor base, NO del valor actual.
        int nuevaVida = (int) (this.vidaBaseInicial * factorRonda * factorSalud);

        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. ESCALADO DE DISPARO (Común a todos los jefes)
        // Hacemos que la cadencia base del Boss genérico se acelere
        float nuevaCadencia = 1.0f / factorRonda;
        this.tiempoEntreDisparos = nuevaCadencia;
        if (this.tiempoEntreDisparos < 0.25f) this.tiempoEntreDisparos = 0.25f; // Tope de seguridad

        // No hay lógica específica de movimiento o spawn aquí, eso lo hacen las subclases.
    }


}
