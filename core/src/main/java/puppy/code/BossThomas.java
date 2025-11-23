package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

public class BossThomas extends Boss {

    private enum EstadoThomas {
        ENTRANDO,
        BUSCANDO,
        PREPARANDO_EMBESTIDA,
        EMBISTIENDO,
        REPOSICIONANDO
    }

    private EstadoThomas estadoThomas = EstadoThomas.ENTRANDO;
    private Nave4 nave;

    // --- Configuración de Velocidades ---
    // Fase 1
    private final float VEL_CARGA_NORMAL = 1100f;
    private final float TIEMPO_PREP_NORMAL = 1.0f;
    private final float TIEMPO_BUSQUEDA_NORMAL = 3.0f;

    // Fase 2 (Furia)
    private final float VEL_CARGA_FURIA = 2200f; // ¡Hiper Velocidad!
    private final float TIEMPO_PREP_FURIA = 0.5f; // Menos tiempo para reaccionar
    private final float TIEMPO_BUSQUEDA_FURIA = 0.5f; // Casi no espera entre ataques

    // Variables dinámicas
    private float velocidadActualEmbestida;
    private float tiempoPreparacionActual;
    private float tiempoBusquedaActual;

    private float velocidadBusqueda = 180f;
    private float temporizadorAtaque = 0;

    protected String nombre;
    // Vectores
    private Vector2 objetivoEmbestida = new Vector2();
    private Vector2 direccionEmbestida = new Vector2();

    // Variable para no matar al jugador en 1 frame (Cooldown de daño por contacto)
    private float tiempoInmuneContacto = 0;

    public BossThomas(float x, float y, Texture tx, Texture txBala, int vidaInicial, Nave4 nave , String nombre) {
        super(x, y, tx, txBala, vidaInicial , "THOMAS EL ARRASADOR");
        this.nave = nave;
        this.nombre = nombre ;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        // 1. Actualizar lógica base (colores de daño, fases de vida, etc)
        super.update(delta, juego);

        temporizadorAtaque += delta;
        if (tiempoInmuneContacto > 0) tiempoInmuneContacto -= delta;

        // --- CONFIGURACIÓN SEGÚN FASE (Heredada de Boss) ---
        if (this.fase == 2) {
            // FASE 2: MODO PESADILLA
            velocidadActualEmbestida = VEL_CARGA_FURIA;
            tiempoPreparacionActual = TIEMPO_PREP_FURIA;
            tiempoBusquedaActual = TIEMPO_BUSQUEDA_FURIA;

            // Visual: Vibrar más fuerte o ponerse rojo constante
            if (estadoThomas != EstadoThomas.PREPARANDO_EMBESTIDA) {
                // Solo cambiamos color si no está parpadeando por daño
                if(!enHit) spr.setColor(1f, 0.3f, 0.3f, 1f);
            }
        } else {
            // FASE 1: NORMAL
            velocidadActualEmbestida = VEL_CARGA_NORMAL;
            tiempoPreparacionActual = TIEMPO_PREP_NORMAL;
            tiempoBusquedaActual = TIEMPO_BUSQUEDA_NORMAL;
        }

        // --- MÁQUINA DE ESTADOS ---
        switch (estadoThomas) {
            case ENTRANDO:
                if (position.y > 600) {
                    position.y -= 150 * delta;
                } else {
                    estadoThomas = EstadoThomas.BUSCANDO;
                    temporizadorAtaque = 0;
                }
                break;

            case BUSCANDO:
                movimientoBusqueda(delta);

                // --- CAMBIO: SOLO DISPARA EN FASE 1 ---
                // En fase 2 está demasiado ocupado embistiendo
                if (this.fase == 1) {
                    super.disparar(delta, juego);
                }

                if (temporizadorAtaque > tiempoBusquedaActual) {
                    prepararEmbestida();
                }
                break;

            case PREPARANDO_EMBESTIDA:
                // EFECTO VISUAL: VIBRACIÓN INTENSA
                float intensidad = (fase == 2) ? 10f : 5f;
                float offsetX = MathUtils.random(-intensidad, intensidad);
                float offsetY = MathUtils.random(-intensidad, intensidad);
                spr.setPosition(position.x + offsetX, position.y + offsetY);

                // En fase 1 puede disparar mientras prepara, en fase 2 no.
                if (this.fase == 1) {
                    super.disparar(delta, juego);
                }

                if (temporizadorAtaque > tiempoPreparacionActual) {
                    iniciarEmbestida();
                }
                break;

            case EMBISTIENDO:
                movimientoEmbestida(delta);
                comprobarLimitesMapa();
                break;

            case REPOSICIONANDO:
                reposicionarThomas();
                break;
        }

        // Si NO estamos vibrando, actualizamos la posición normal del sprite
        if (estadoThomas != EstadoThomas.PREPARANDO_EMBESTIDA) {
            spr.setPosition(position.x, position.y);
        }

        // Mantener hitbox sincronizada
        getHitbox().setPosition(position.x + 25, position.y + 15);
    }

    // --- MÉTODOS DE ESTADO ---

    private void movimientoBusqueda(float delta) {
        float targetX = nave.getX();
        float diffX = targetX - position.x;

        // En fase 2 se mueve lateralmente más rápido para buscarte
        float velLateral = (fase == 2) ? 400f : velocidadBusqueda;

        if (Math.abs(diffX) > 10) {
            float signo = Math.signum(diffX);
            position.x += signo * velLateral * delta;
        }

        position.y = 600 + MathUtils.sin(Gdx.graphics.getFrameId() * 0.05f) * 20;
    }

    private void prepararEmbestida() {
        // Guardar dónde está el jugador AHORA
        objetivoEmbestida.set(nave.getX(), nave.getY());

        // Calcular dirección (Extender el vector para que cruce el mapa)
        // Simplemente calculamos la dirección normalizada
        direccionEmbestida.set(objetivoEmbestida.x - position.x, objetivoEmbestida.y - position.y).nor();

        temporizadorAtaque = 0;
        estadoThomas = EstadoThomas.PREPARANDO_EMBESTIDA;
    }

    private void iniciarEmbestida() {
        temporizadorAtaque = 0;
        estadoThomas = EstadoThomas.EMBISTIENDO;
        // Sonido de ataque aquí si quieres
    }

    private void movimientoEmbestida(float delta) {
        // Movimiento Rectilíneo A TODA VELOCIDAD
        position.x += direccionEmbestida.x * velocidadActualEmbestida * delta;
        position.y += direccionEmbestida.y * velocidadActualEmbestida * delta;

        // Girar sprite
        spr.rotate(1500 * delta);

        // COLISIÓN CON JUGADOR
        if (this.getHitbox().overlaps(nave.getHitbox())) {
            if (tiempoInmuneContacto <= 0) {
                // Daño masivo en fase 2
                int dano = (fase == 2) ? 60 : 30;
                nave.recibirHit(dano, delta);
                tiempoInmuneContacto = 0.5f;
            }
        }
    }

    private void comprobarLimitesMapa() {
        // Si se sale, reposicionar
        if (position.x < -500 || position.x > 1700 ||
            position.y < -500 || position.y > 1500) {
            estadoThomas = EstadoThomas.REPOSICIONANDO;
            temporizadorAtaque = 0;
        }
    }

    private void reposicionarThomas() {
        // Teletransporte
        position.x = 1200 / 2 - spr.getWidth() / 2;
        position.y = 900;
        spr.setRotation(0);

        // En Fase 2, el reposicionamiento es casi instantáneo
        float tiempoEspera = (fase == 2) ? 0.2f : 1.0f;

        if (temporizadorAtaque > tiempoEspera) {
            estadoThomas = EstadoThomas.ENTRANDO;
            temporizadorAtaque = 0;
        }
    }

    // --- OVERRIDES PARA ANULAR PADRE ---
    @Override
    protected void movimientoCombate(float delta) { }

    @Override
    protected void disparar(float delta, PantallaJuego juego) { }

    // --- DIBUJO DE ADVERTENCIA MEJORADO ---
    public void drawWarning(ShapeRenderer sr) {
        if (estadoThomas == EstadoThomas.PREPARANDO_EMBESTIDA) {
            Gdx.gl.glEnable(Gdx.gl.GL_BLEND); // Activar transparencia
            sr.begin(ShapeRenderer.ShapeType.Filled);

            // Color: En fase 1 Naranja, En fase 2 ROJO SANGRE
            if (fase == 2) {
                // Efecto parpadeo rápido
                float alpha = 0.5f + MathUtils.sin(Gdx.graphics.getFrameId() * 20) * 0.3f;
                sr.setColor(1f, 0f, 0f, alpha);
            } else {
                sr.setColor(1f, 0.5f, 0f, 0.5f);
            }

            float cx = position.x + spr.getWidth()/2;
            float cy = position.y + spr.getHeight()/2;

            // Calculamos un punto final muy lejano en la dirección del ataque
            // para que la línea atraviese toda la pantalla
            float finX = cx + direccionEmbestida.x * 3000;
            float finY = cy + direccionEmbestida.y * 3000;

            // Dibuja el "Camino de la muerte" (rectLine dibuja una linea con grosor)
            float grosor = (fase == 2) ? 80f : 50f; // Más grueso en fase 2
            sr.rectLine(cx, cy, finX, finY, grosor);

            sr.end();
            Gdx.gl.glDisable(Gdx.gl.GL_BLEND);
        }
    }

    @Override
    public void aumentarDificultad(float factorRonda) {
        float factorSalud = BuffManager.getInstance().getEnemyHealthMultiplier();
        float factorVelocidad = BuffManager.getInstance().getEnemySpeedMultiplier();

        // 1. ESCALADO DE VIDA (Común, basado en Boss.vidaBaseInicial)
        int nuevaVida = (int) (this.vidaBaseInicial * factorRonda * factorSalud * 1.5f); // 1.5x por ser jefe de embestida
        this.vidaActual = nuevaVida;
        this.vidaMax = nuevaVida;

        // 2. ESCALADO DE VELOCIDADES DE EMBESTIDA (Específico de Thomas)
        // Cuanto más alta la ronda, más rápido se mueve en ambas fases.
        float escaladoVel = 1.0f + (factorRonda - 1) * 0.2f; // Aumenta 20% por factorRonda.

        // Aplicamos el escalado a las variables base de Thomas.
        // FASE 1
        this.velocidadActualEmbestida = VEL_CARGA_NORMAL * escaladoVel;
        this.tiempoPreparacionActual = TIEMPO_PREP_NORMAL / escaladoVel; // Menos tiempo para prepararse
        this.tiempoBusquedaActual = TIEMPO_BUSQUEDA_NORMAL / escaladoVel;

        // FASE 2 (Modo Furia, aún más rápido)
        // Usamos las variables originales de Furia y las aceleramos un poco más
        this.velocidadActualEmbestida = VEL_CARGA_FURIA * escaladoVel;
        this.tiempoPreparacionActual = TIEMPO_PREP_FURIA / (escaladoVel * 1.2f);
        this.tiempoBusquedaActual = TIEMPO_BUSQUEDA_FURIA / (escaladoVel * 1.2f);

        // 3. ESCALADO DE DISPARO (Usamos la lógica base de Boss)
        super.aumentarDificultad(factorRonda);
    }


}
