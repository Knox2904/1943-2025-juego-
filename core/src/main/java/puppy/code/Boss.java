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
    protected String nombre;

    private float timerCombustible = 0;
    // Cada 15 segundos cae una vida. Ajusta esto según la dificultad.
    private final float TIEMPO_ENTRE_COMBUSTIBLES = 9f;

    // Fases del Jefe
    // 0: Entrando, 1: Fase Normal, 2: Fase Furiosa (50% vida)
    protected int fase = 0;
    protected float tiempoVida = 0;

    public Boss(float x, float y, Texture tx, Texture txBala , int vidaInicial , String nombre) {
        // Velocidad baja (se mueve lento), Vida 500 (¡Es un tanque!)
        super(tx, x, y, 60f, vidaInicial);
        this.vidaBaseInicial = vidaInicial ;
        this.vidaMax = vidaInicial;
        this.txBala = txBala;

        this.nombre = nombre;


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

        timerCombustible += delta;

        if (timerCombustible > TIEMPO_ENTRE_COMBUSTIBLES) {
            timerCombustible = 0;
            solicitarAyuda(juego);
        }


    }

    protected void movimientoCombate(float delta) {
        // Movimiento en "Ocho" (Infinito) lento
        position.x = (1200 / 2 - spr.getWidth()/2) + MathUtils.sin(tiempoVida * 0.5f) * 300f;
        position.y = 600 + MathUtils.cos(tiempoVida * 1.0f) * 50f;
    }

    private void solicitarAyuda(PantallaJuego juego) {
        // Generamos una posición X aleatoria dentro de la pantalla (con margen)
        // Asumiendo ancho 1200: entre 50 y 1150
        float randomX = MathUtils.random(50, 1150);

        // Y = 800 (Arriba de la pantalla, para que caiga)
        float spawnY = 810;


        juego.crearPowerUpEn(randomX, spawnY, TipoPowerUp.COMBUSTIBLE);

        // Opcional: Reproducir un sonido de "Ayuda en camino" si quieres
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

        // --- FÓRMULA "JUGABLE" (Soft Cap) ---

        // 1. Factor de Ronda Suavizado:
        // En vez de multiplicar x11 en ronda 100, elevamos a la 0.8.
        // Ronda 100 (10.9) -> Se convierte en ~6.7x
        double rondaSuave = Math.pow(factorRonda, 0.8);

        // 2. Factor de Buffs del Jugador Suavizado:
        // Usamos raíz cuadrada. Si el jugador tiene buff x11, el enemigo recibe x3.3
        double saludSuave = Math.sqrt(factorSalud);

        // CÁLCULO FINAL DE VIDA
        int nuevaVida = (int) (this.vidaBaseInicial * rondaSuave * saludSuave);

        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. ESCALADO DE DISPARO (Con límite humano)
        // La cadencia aumenta, pero nunca dispara más rápido que 0.25s
        float nuevaCadencia = 1.0f / factorRonda;
        if (nuevaCadencia < 0.25f) nuevaCadencia = 0.25f;

        this.tiempoEntreDisparos = nuevaCadencia;
    }

    public String getNombre() {
        return this.nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }
    public void setColor(float r, float g, float b, float a) {
        spr.setColor(r, g, b, a);
    }


}
