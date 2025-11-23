package puppy.code;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;



public class PantallaJuego implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private SpriteBatch batch;
    private Sound explosionSound;

    private int score;
    private int ronda;
    private int velXAsteroides;
    private int velYAsteroides;
    private int cantAsteroides;

    private Nave4 nave;
    private ArrayList<Bullet> balasJugador = new ArrayList<>();
    private ArrayList<Bullet> balasEnemigas = new ArrayList<>();
    private ArrayList<PowerUp> powerUps = new ArrayList<>();
    private Texture powerUpTexture;
    private Texture texturaAliado;
    private Texture backgroundTexture;
    private float scrollSpeed = 100f; // Velocidad de scroll
    private float backgroundOffsetY = 0; // Posicion Y del fondo
    private Texture txBarra;
    private Texture txAsteroide;
    private Texture txEnemigo; // Textura para el kamikaze
    private Texture txBalaEnemiga;
    private Texture txTank;
    private Texture txKamikazeS;
    private Texture txHealer;// Nuevo
    private Texture txEliminaBuffs;
    private Texture txCargueroSmall;
    private Texture txCargueroBig;
    private Texture txBoss_1;
    private Texture txBossThomas; // Promovida a global
    private Texture txBalaBoss;
    private OleadaFactory factory;
    //  Variables añadidas
    private ArrayList<EntidadJuego> enemigos = new ArrayList<>();
    private ArrayList<EntidadJuego> enemigosPendientes = new ArrayList<>();
    private ArrayList<EntidadJuego> enemigosEntrantes = new ArrayList<>();
    private float spawnTimerEnemigo = 0f; // Timer para el kamikaze (5 segundos)

    private float anuncioRondaTimer = 3.0f;

    private Viewport viewport ;
    private Texture txEscudo;
    private Texture txPowerAmmo;
    private Texture txPowerFuel;
    private Texture txPowerAliado;

    private Boss jefeActivo = null;
    private Texture txBarraBoss;


    private Sound soundShieldBreak;
    private Sound soundHeal;
    private Sound soundHealerDown;

    private boolean isPaused = false;
    private ShapeRenderer shapeRenderer;



    public PantallaJuego(SpaceNavigation game, int ronda, float combustible, int score,
                         int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        shapeRenderer = new ShapeRenderer();

        batch = game.getBatch();
        camera = new OrthographicCamera();
        //camera.setToOrtho(false, 480, 640);
        viewport = new FitViewport(1200, 800, camera);
        viewport.apply();


        // --- Cargas de Assets (Sonidos y Texturas) ---
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));



        powerUpTexture = new Texture(Gdx.files.internal("powerUpDobleTiro.png"));
        texturaAliado = new Texture(Gdx.files.internal("MainShip3.png"));
        backgroundTexture = new Texture(Gdx.files.internal("fondo jugando.jpg"));
        txEnemigo = new Texture(Gdx.files.internal("kamikaze.png"));
        txAsteroide = new Texture(Gdx.files.internal("aGreyMedium4.png"));
        txBalaEnemiga = new Texture(Gdx.files.internal("Rocket2.png"));
        txTank = new Texture(Gdx.files.internal("tanque 2025.png"));
        txKamikazeS = new Texture(Gdx.files.internal("kamikazeS.png"));
        txHealer = new Texture(Gdx.files.internal("healer.png"));
        txEliminaBuffs = new Texture(Gdx.files.internal("MainShip3.png"));
        txCargueroSmall = new Texture(Gdx.files.internal("aGreyMedium4.png"));
        txCargueroBig = new Texture(Gdx.files.internal("aGreyLarge.png"));

        txEscudo = new Texture(Gdx.files.internal("escudo.png"));
        txPowerFuel = new Texture(Gdx.files.internal("vida.png"));
        txPowerAliado = new Texture(Gdx.files.internal("companion.png"));


        soundShieldBreak = Gdx.audio.newSound(Gdx.files.internal("escudoRopiendose.mp3"));
        soundHeal = Gdx.audio.newSound(Gdx.files.internal("heal.mp3"));
        soundHealerDown = Gdx.audio.newSound(Gdx.files.internal("healerSinBateria.mp3"));

        txBoss_1 = new Texture(Gdx.files.internal("Boss1.png"));
        txBossThomas = new Texture(Gdx.files.internal("BossThomas.png")); // Asegurate que el archivo exista
        txBalaBoss = new Texture(Gdx.files.internal("Rocket2.png"));


        // Crear textura barra
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        txBarra = new Texture(pixmap);
        txBarraBoss = txBarra;
        pixmap.dispose();
        game.stopMusic();
        game.playMusic();

        // Crear Nave
        nave = new Nave4((float) Gdx.graphics.getWidth() / 2 - 50, 30f,
            new Texture(Gdx.files.internal("MainShip3.png")),
            Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
            new Texture(Gdx.files.internal("Rocket2.png")),
            Gdx.audio.newSound(Gdx.files.internal("disparo.mp3")),
            texturaAliado,
            txEscudo,
            soundShieldBreak
        );





        // --- INICIAR LA PRIMERA OLEADA ---
        // En lugar de todo el código repetido, solo llamamos al método:
        iniciarRonda();
    }

    public void dibujaEncabezado() {
        float anchoBarraTotal = 200f;
        float altoBarra = 20f;
        float margenX = 10f;
        float margenY = Config.ALTO_MUNDO - 40f;

        // Fondo de la barra (Gris)
        batch.setColor(0.2f, 0.2f, 0.2f, 1f);
        batch.draw(txBarra, margenX, margenY, anchoBarraTotal, altoBarra);

        // Barra de vida (Color según porcentaje)
        float porcentaje = nave.getCombustible() / nave.getMaxCombustible();
        float anchoActual = anchoBarraTotal * porcentaje;

        if (porcentaje > 0.6f) batch.setColor(0f, 1f, 0f, 1f); // Verde
        else if (porcentaje > 0.2f) batch.setColor(1f, 1f, 0f, 1f); // Amarillo
        else batch.setColor(1f, 0f, 0f, 1f); // Rojo

        batch.draw(txBarra, margenX, margenY, anchoActual, altoBarra);

        // Resetear color y dibujar textos
        batch.setColor(1f, 1f, 1f, 1f);
        game.getFont().getData().setScale(1.2f);

        game.getFont().draw(batch, "Score: " + this.score, 1200 - 250, Config.ALTO_MUNDO - 20);
        game.getFont().draw(batch, "High: " + game.getHighScore(), 1200 / 2 - 50, Config.ALTO_MUNDO - 20);

        game.getFont().getData().setScale(1.0f);
        game.getFont().draw(batch, "FUEL", margenX + 5, margenY + 15);

        if (jefeActivo != null && !jefeActivo.estaDestruido()) {
            float anchoPantalla = 1200;
            float anchoBarra = 800; // Barra larga
            float xBarra = (anchoPantalla - anchoBarra) / 2;
            float yBarra = 750; // Arriba del todo

            // 1. Fondo Rojo Oscuro
            batch.setColor(0.5f, 0f, 0f, 1f);
            batch.draw(txBarraBoss, xBarra, yBarra, anchoBarra, 20);

            // 2. Vida Actual (Rojo Brillante)
            float porcentajeJefe = (float) jefeActivo.getVidaActual() / jefeActivo.getVidaMax();
            batch.setColor(1f, 0f, 0f, 1f);
            batch.draw(txBarraBoss, xBarra, yBarra, anchoBarra * porcentajeJefe, 20);

            // Texto del Jefe
            game.getFont().draw(batch, "MOTHERSHIP OMEGA", xBarra, yBarra + 40);

            // Resetear color
            batch.setColor(1, 1, 1, 1);
        }


    }


    public Nave4 getNave() { return nave; }

    @Override
    public void render(float delta) {

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            isPaused = !isPaused;
        }

        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        if (jefeActivo instanceof BossThomas && !jefeActivo.estaDestruido()) {
            ((BossThomas) jefeActivo).drawWarning(shapeRenderer);
        }

        if (!isPaused) {
            // --- 1. ACTUALIZACIONES ---
            nave.update(delta, this);

            // 1. Actualizar Balas JUGADOR
            for (Bullet b : balasJugador) {
                b.update();
                if (b.getX() < -10 || b.getX() > viewport.getWorldWidth() + 10 ||
                    b.getY() < -10 || b.getY() > viewport.getWorldHeight() + 10) {
                    b.recibirHit(1, 0);
                }
            }

            // 2. Actualizar Balas ENEMIGAS
            for (Bullet b : balasEnemigas) {
                b.update();
                // Check salida de pantalla
                if (b.getY() < -50 || b.getY() > viewport.getWorldHeight() + 50) {
                    b.recibirHit(1, 0);
                }
            }

            // --- NUEVO: Procesar enemigos entrantes (Hijos de cargueros) ---
            if (!enemigosEntrantes.isEmpty()) {
                enemigos.addAll(enemigosEntrantes);
                enemigosEntrantes.clear();
            }

            // Actualizar TODOS los enemigos (Asteroides, Kamikazes, Tanques)
            for (EntidadJuego enemigo : enemigos) {
                enemigo.update(delta, this);
            }

            // Actualizar PowerUps
            for (PowerUp p : powerUps) {
                p.update(delta, this);
            }

            // --- SPAWNER PROGRESIVO (Sacar de la cola de pendientes) ---
            if (anuncioRondaTimer <= 0) {

                if (!enemigosPendientes.isEmpty()) {
                    // Solo sacamos enemigos si hay en la cola
                    spawnTimerEnemigo -= delta;

                    if (spawnTimerEnemigo <= 0) {
                        // 1. Sacar el siguiente enemigo de la lista de espera
                        EntidadJuego enemigoASpawnear = enemigosPendientes.remove(0);

                        // 2. Activarlo (meterlo al juego real)
                        enemigos.add(enemigoASpawnear);

                        // 3. Calcular cuándo sale el siguiente (ej: entre 0.5 y 1.5 segundos)
                        //    Usamos el multiplicador de BuffManager para que sea más rápido si es difícil
                        float spawnRate = BuffManager.getInstance().getEnemySpawnRateMultiplier();
                        spawnTimerEnemigo = MathUtils.random(0.5f, 1.5f) / spawnRate;
                    }
                }
            }


            // --- 4. COLISIONES ---

            // A. Balas vs Enemigos (Cualquier tipo)
            for (int i = 0; i < balasJugador.size(); i++) {
                Bullet b = balasJugador.get(i);
                if (b.estaDestruido()) continue;

                for (EntidadJuego enemigo : enemigos) {
                    if (!enemigo.estaDestruido()) {
                        if (b.checkCollision(enemigo.getHitbox())) {
                            explosionSound.play(0.35f);
                            enemigo.recibirHit(b.getDamage(), delta); // Dañar enemigo
                            b.recibirHit(1, delta);       // Destruir bala
                            agregarScore(10);

                            if (enemigo.estaDestruido()) {
                                intentarGenerarPowerUp(enemigo.getX(), enemigo.getY());
                            }

                            break; // Bala choca solo una vez
                        }
                    }
                }
            }
            // B. Colisión: Balas ENEMIGAS contra JUGADOR
            for (Bullet b : balasEnemigas) {
                if (b.estaDestruido()) continue;
                if (b.checkCollision(nave.getHitbox())) {
                    nave.recibirHit(10, delta);
                    b.recibirHit(1, 0); // La bala enemiga se destruye
                }
            }

            // D. Colisión Asteroide vs Asteroide (Rebote)
            for (int i = 0; i < enemigos.size(); i++) {
                EntidadJuego e1 = enemigos.get(i);

                // Solo nos interesa si e1 es un Asteroide (Ball2)
                if (e1 instanceof Ball2) {
                    Ball2 b1 = (Ball2) e1;
                    if (b1.estaDestruido()) continue;

                    for (int j = i + 1; j < enemigos.size(); j++) {
                        EntidadJuego e2 = enemigos.get(j);

                        // Y si e2 TAMBIÉN es un Asteroide
                        if (e2 instanceof Ball2) {
                            Ball2 b2 = (Ball2) e2;
                            if (!b2.estaDestruido()) {
                                // ¡Hacemos que reboten!
                                b1.checkCollision(b2);
                            }
                        }
                    }
                }
            }

            // B. Nave vs Enemigos (Cualquier tipo)
            for (EntidadJuego enemigo : enemigos) {
                if (!enemigo.estaDestruido()) {
                    // Usamos el checkCollision de la nave
                    if (nave.checkCollision(enemigo, delta)) {
                        enemigo.recibirHit(10, delta); // El enemigo también recibe impacto

                        if (enemigo.estaDestruido()) {
                            intentarGenerarPowerUp(enemigo.getX(), enemigo.getY());
                        }

                    }
                }
            }

            // C. Nave vs PowerUps
            // Nave vs PowerUps
            for (int i = 0; i < powerUps.size(); i++) {
                PowerUp p = powerUps.get(i);


                if (p.getHitbox().overlaps(nave.getHitbox())) {

                    p.recoger(nave);

                    powerUps.remove(i);
                    i--;
                }
            }


            // --- 5. LIMPIEZA ---
            balasJugador.removeIf(Bullet::estaDestruido);
            balasEnemigas.removeIf(Bullet::estaDestruido);
            enemigos.removeIf(EntidadJuego::estaDestruido); // Limpia Asteroides, Kamikazes, etc.
            powerUps.removeIf(PowerUp::estaDestruido);

            // --- 6. LÓGICA DE NIVEL ---
            if (nave.estaDestruido()) {
                // ... Game Over logic ...
                if (score > game.getHighScore()) game.setHighScore(score);
                game.stopMusic();
                Screen ss = new PantallaGameOver(game);
                ss.resize(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
                game.setScreen(ss);
                dispose();
                return;
            }

            // Si no quedan enemigos, pasamos a la siguiente ronda
            if (enemigos.isEmpty() && enemigosPendientes.isEmpty()) {

                // 1. Aumentar dificultad
                ronda++;
                velXAsteroides += 1;
                velYAsteroides += 1;
                cantAsteroides += 5;

                // 2. Generar la nueva oleada EN ESTA MISMA PANTALLA
                iniciarRonda();

                // 3. ACTIVAR EL MENSAJE
                anuncioRondaTimer = 3.0f;

                // Opcional: Dar un poco de combustible por pasar de ronda
                nave.agregarCombustible(10f);
            }
        }

        // --- 3. DIBUJADO ---
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);


        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        // 1. FONDO (Primero)
        batch.draw(backgroundTexture, 0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());

        // 2. SPRITES (En medio)
        for (PowerUp p : powerUps) p.draw(batch);
        for (Bullet b : balasJugador) b.draw(batch);
        for (Bullet b : balasEnemigas) b.draw(batch);
        for (EntidadJuego e : enemigos) e.draw(batch);
        nave.draw(batch);

        // 3. MENSAJE DE RONDA (Encima de las naves)
        if (anuncioRondaTimer > 0) {
            anuncioRondaTimer -= delta;

            game.getFont().setColor(1, 1, 0, 1); // Amarillo
            game.getFont().getData().setScale(3.0f);
            game.getFont().draw(batch,
                "RONDA " + ronda,
                0,                  // Empieza en X = 0
                Config.ALTO_MUNDO / 2 + 50,       // Altura: Mitad de 800 (+ un poco arriba)
                1200,               // Ancho objetivo: Todo el ancho del mundo (1200)
                com.badlogic.gdx.utils.Align.center, // Alineación: CENTRO
                false);

            game.getFont().setColor(1, 1, 1, 1); // Restaurar Blanco
            game.getFont().getData().setScale(1.0f); // Restaurar tamaño
        }

        // 4. HUD (Encima de todo)
        dibujaEncabezado();

        batch.end();

        if (isPaused) {
            dibujaMenuPausa();
        }
    }


    public boolean agregarBala(Bullet bb) {
        return balasJugador.add(bb);
    }

    public boolean agregarBalaEnemiga(Bullet bb) {
        return balasEnemigas.add(bb);
    }

    public void agregarEnemigo(EntidadJuego enemigo) {
        this.enemigosEntrantes.add(enemigo);
    }

    @Override
    public void show() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);

    }

    @Override
    public void pause() {
        // TODO Auto-generated method stub

    }

    @Override
    public void resume() {
        // TODO Auto-generated method stub

    }

    @Override
    public void hide() {
        // TODO Auto-generated method stub

    }

    @Override
    public void dispose() {
        // TODO Auto-generated method stub
        this.explosionSound.dispose();

        this.txBarra.dispose();
        this.txEnemigo.dispose();
        this.txAsteroide.dispose();
        this.txTank.dispose();
        txPowerFuel.dispose();
        txPowerAliado.dispose();
        txHealer.dispose();
        soundHeal.dispose();
        soundHealerDown.dispose();
        shapeRenderer.dispose();
        txEliminaBuffs.dispose();
        txCargueroSmall.dispose();
        txCargueroBig.dispose();
        txBossThomas.dispose();
        txBoss_1.dispose();
        txBalaBoss.dispose();
    }

    public void crearPowerUpEn(float x, float y, TipoPowerUp tipo) {
        PowerUp p = null;

        switch (tipo) {
            case MEJORA_ARMA:

                p = new PowerUpArma(x, y, powerUpTexture, this);
                break;
            case COMBUSTIBLE:

                p = new PowerUpCombustible(x, y, this.txPowerFuel);
                break;
            case NAVE_ALIADA:
                p = new PowerUpAliado(x, y, this.txPowerAliado);
                break;

            case ESCUDO:

                p = new PowerUpEscudo(x, y, this.txEscudo);
                break;
        }

        if (p != null) {
            powerUps.add(p);
        }
    }


    public void agregarScore(int puntos){
        score+=puntos;
        if (this.score >= proximaMejora) {
            proximaMejora += 1000;
            mostrarPantallaMejoras();
        }
    }

    private int proximaMejora = 250;

    private void intentarGenerarPowerUp(float x, float y) {
        // FORMULA: Empieza en 20%. Restamos 0.3% por cada ronda.
        // Math.max asegura que nunca baje del 5%.
        float probabilidadBase = 20.0f;
        float penalizacionPorRonda = (ronda - 1) * 0.3f;

        float probabilidadActual = Math.max(5.0f, probabilidadBase - penalizacionPorRonda);


        //System.out.println("Probabilidad actual en Ronda " + ronda + ": " + probabilidadActual + "%");

        if (MathUtils.random(0, 100) < probabilidadActual) {
            TipoPowerUp[] tipos = TipoPowerUp.values();
            int indiceRandom = MathUtils.random(0, tipos.length - 1);
            crearPowerUpEn(x, y, tipos[indiceRandom]);
        }
    }

    /**
     * Pausa el juego actual y muestra la pantalla de selección de mejoras.
     */
    public void mostrarPantallaMejoras() {

        game.setScreen(new UpgradeScreen(game, this));
    }




    /**
     * Método para reiniciar los enemigos sin reiniciar la pantalla.
     * Ahora mezcla enemigos de T1 y T2 en rondas superiores.
     */
    private void iniciarRonda() {
        if (jefeActivo != null && !jefeActivo.estaDestruido()) {
            return;
        }

        if (ronda % 5 == 0 ) {
            // Limpiamos enemigos pendientes para que sea un duelo 1 vs 1
            enemigosPendientes.clear();

            OleadaFactory factoryBoss = new EnemigoT4(txBoss_1, txBalaEnemiga, txBossThomas);

            EntidadJuego jefe = null;
            float cx = viewport.getWorldWidth() / 2; // Centro X
            float cy = 900; // Arriba Y

            // 2. Selección de Jefe
            if (ronda == 5) {
                jefe = factoryBoss.createEnemigoT1(cx - 125, cy, this); // Boss 1
            }
            else if (ronda == 10) {
                jefe = factoryBoss.createEnemigoT2(cx, cy, this); // Thomas
            }
            else if (ronda >= 15) {
                // Por ahora repetimos T1 o T2 aleatorio hasta tener el T3
                if(MathUtils.randomBoolean())
                    jefe = factoryBoss.createEnemigoT1(cx - 125, cy, this);
                else
                    jefe = factoryBoss.createEnemigoT2(cx, cy, this);
            }
            else {
                // Fallback
                jefe = factoryBoss.createEnemigoT1(cx - 125, cy, this);
            }

            // 3. Asignar
            if (jefe instanceof Boss) {
                jefeActivo = (Boss) jefe;
                enemigos.add(jefe);
            }

            return; // FIN, no spawneamos enemigos normales
        }




        // 1. Instanciamos AMBAS factories
        OleadaFactory factoryT1 = new EnemigoT1(txAsteroide, txEnemigo,txEliminaBuffs );
        OleadaFactory factoryT2 = new EnemigoT2(txKamikazeS, txTank, txBalaEnemiga, txHealer, soundHeal, soundHealerDown);
        OleadaFactory factoryT3 = new EnemigoT3(txCargueroSmall, txCargueroBig, txEnemigo, txKamikazeS, txTank, txEliminaBuffs, txBalaEnemiga);
        // OBTENEMOS EL MULTIPLICADOR (Ej: 1.0 en ronda 1, 5.0 en ronda 40)
        float dificultad = getFactorDificultad();

        Random r = new Random();
        int presupuesto = 10 + ((ronda - 1) * 5);

        while (presupuesto > 0) {
            // Usamos viewport.getWorldWidth() para que nazcan dentro del mundo lógico
            float x = r.nextInt((int)viewport.getWorldWidth() - 64);
            float y = Gdx.graphics.getHeight() + 50; // Ojo: Aquí podrías usar 800 + 50 si usas Viewport

            EntidadJuego nuevoEnemigo = null;
            int costo = 0;

            // --- RONDA 1 ---
            if (ronda == 1) {
                if (r.nextBoolean()) {
                    nuevoEnemigo = factoryT1.createEnemigoT1(x, y, this);
                    // MODIFICADO: Le pasamos 'dificultad' al metodo del asteroide
                    configurarAsteroide(nuevoEnemigo, r, dificultad);
                } else {
                    nuevoEnemigo = factoryT1.createEnemigoT2(x, y, this);
                }
                costo = 1;
            }

            // --- RONDA 2+ ---
            else {
                int dado = r.nextInt(100);

                // A. Jefe 1
                if (ronda > 7 && dado < 5 && presupuesto >= 10) {
                    nuevoEnemigo = new Boss(x, y, txBoss_1, txBalaEnemiga, 150);
                    costo = 10;
                }

                // CARGUEROS...
                else if (ronda >= 4 && dado < 10 && presupuesto >= 8) {
                    // ... (Tu código de cargueros sigue igual) ...
                    if (r.nextBoolean() && presupuesto >= 12) {
                        nuevoEnemigo = factoryT3.createEnemigoT2(x, y, this);
                        costo = 12;
                    } else {
                        nuevoEnemigo = factoryT3.createEnemigoT1(x, y, this);
                        costo = 8;
                    }
                }

                // B. HEALER
                else if (ronda >= 3 && dado < 20 && presupuesto >= 4) {
                    nuevoEnemigo = factoryT2.createEnemigoT3(x, y, this);
                    costo = 4;
                }

                // C. TANQUE
                else if (dado < 30 && presupuesto >= 3) {
                    nuevoEnemigo = factoryT2.createEnemigoT2(x, y, this); // Tanque
                    costo = 3;
                }
                // D. ELIMINA BUFFS (Usamos Factory T1 - Tier 3)
                else if (ronda >= 2 && dado < 55 && presupuesto >= 2) {
                    // Probabilidad dedicada para EliminaBuffs
                    nuevoEnemigo = factoryT1.createEnemigoT3(x, y, this);
                    costo = 2; // (Ajusta el costo si quieres, 2 me parece bien)
                }

                // E. KAMIKAZE SUPER
                else if (dado < 50 && presupuesto >= 2) {
                    nuevoEnemigo = factoryT2.createEnemigoT1(x, y, this); // Kamikaze S
                    costo = 2;
                }

                // F. KAMIKAZE NORMAL
                else if (dado < 75 && presupuesto >= 1) {
                    nuevoEnemigo = factoryT1.createEnemigoT2(x, y, this); // Kamikaze Normal
                    costo = 1;
                }
                //G. ASTEROIDE
                else {
                    if (presupuesto >= 1) {
                        nuevoEnemigo = factoryT1.createEnemigoT1(x, y, this); // Asteroide
                        // MODIFICADO: Le pasamos 'dificultad'
                        configurarAsteroide(nuevoEnemigo, r, dificultad);
                        costo = 1;
                    } else {
                        break;
                    }
                }
            }

            // --- APLICAR LA DIFICULTAD A LOS ENEMIGOS INTELIGENTES ---
            if (nuevoEnemigo != null) {

                // Si es Kamikaze, KamikazeS o Tanque (y si implementaste aumentarDificultad en ellos)
                if (nuevoEnemigo instanceof Kamikaze) {
                    ((Kamikaze) nuevoEnemigo).aumentarDificultad(dificultad);
                }
                else if (nuevoEnemigo instanceof KamikazeS) {
                    ((KamikazeS) nuevoEnemigo).aumentarDificultad(dificultad);
                }
                // Si tienes clase Tanque, agrégala aquí también:
                else if (nuevoEnemigo instanceof NaveTanque) { ((NaveTanque)nuevoEnemigo).aumentarDificultad(dificultad); }

                else if (nuevoEnemigo instanceof EnemigoHealer) {
                    ((EnemigoHealer) nuevoEnemigo).aumentarDificultad(dificultad);
                }

                enemigosPendientes.add(nuevoEnemigo);
                presupuesto -= costo;
            } else {
                if (presupuesto < 1) break;
            }
        }
    }


    // MODIFICADO: Ahora recibe el float dificultad
    private void configurarAsteroide(EntidadJuego enemigo, Random r, float dificultad) {
        if (enemigo instanceof Ball2) {
            Ball2 b = (Ball2) enemigo;

            // Velocidad base aleatoria
            float velocidadBase = 150f + r.nextInt((int) MathUtils.random(500f, 1000f));

            // APLICAMOS EL MULTIPLICADOR
            float velocidadFinal = velocidadBase * dificultad;

            // (Opcional) Tope máximo para que no atraviesen paredes si es ronda 100
            if (velocidadFinal > 1200f) velocidadFinal = 1200f;

            float angulo = MathUtils.random(0f, 360f);
            b.setXSpeed(MathUtils.cosDeg(angulo) * velocidadFinal);
            b.setySpeed(MathUtils.sinDeg(angulo) * velocidadFinal);
        }
    }

    // Calcula un multiplicador basado en la ronda.
    // Ronda 1 = 1.0 (Normal)
    // Ronda 10 = 1.9 (Casi doble velocidad)
    // Ronda 48 = 5.7 (Suerte)
    public float getFactorDificultad() {
        // Aumenta un 10% la dificultad por ronda
        return 1.0f + ((ronda - 1) * 0.1f);
    }


    public ArrayList<EntidadJuego> getEnemigos() {
        return this.enemigos;
    }

    // GETTER NECESARIO PARA ELIMINABUFFS
    public ArrayList<PowerUp> getPowerUps() {
        return this.powerUps;
    }

    private void dibujaMenuPausa() {
        // 1. Velo Negro Transparente (Usando ShapeRenderer)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.6f);
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();

        Gdx.gl.glDisable(GL20.GL_BLEND);

        // 2. Texto "PAUSA" (Usando SpriteBatch)
        batch.begin();
        BuffManager buffs = BuffManager.getInstance();

        // --- ENCABEZADO DE PAUSA ---
        game.getFont().getData().setScale(3.0f);
        game.getFont().draw(batch, "JUEGO EN PAUSA", 0, viewport.getWorldHeight() - 50, viewport.getWorldWidth(), 1, false);

        game.getFont().getData().setScale(1.5f);
        game.getFont().draw(batch, "Presiona Esc para continuar", 0, viewport.getWorldHeight() - 100, viewport.getWorldWidth(), 1, false);

        // --- 2. INFORMACIÓN DEL JUGADOR (Columna Izquierda) ---
        float xJugador = 100;
        float yJugador = viewport.getWorldHeight() - 200;

        game.getFont().getData().setScale(1.2f);
        game.getFont().draw(batch, "=== JUGADOR ===", xJugador, yJugador);
        yJugador -= 30;

        game.getFont().draw(batch, "Nivel Actual Total: " + buffs.getTotalUpgradesApplied(), xJugador, yJugador);
        yJugador -= 25;

        // NIVEL DE ARMA ACTUAL
        game.getFont().draw(batch, "Nivel de Arma: " + nave.getNivelArma(), xJugador, yJugador);
        yJugador -= 25;

        game.getFont().draw(batch, "Nivel de Maxima: " + BuffManager.getInstance().getMaxWeaponLevel(), xJugador, yJugador);
        yJugador -= 25;

        game.getFont().draw(batch, "Vida Máxima: " + (int)nave.getMaxCombustible(), xJugador, yJugador);
        yJugador -= 25;
        game.getFont().draw(batch, "Velocidad de Tiro: " + String.format("%.2f", buffs.getFireRateMultiplier()) + "x", xJugador, yJugador);
        yJugador -= 25;
        game.getFont().draw(batch, "Daño por Bala: " + buffs.getDamageMultiplier() + "x", xJugador, yJugador);
        yJugador -= 25;
        game.getFont().draw(batch, "Aliados: " + nave.getContadorAliados(), xJugador, yJugador);
        yJugador -= 25;
        game.getFont().draw(batch, "Escudo: " + (nave.getTieneEscudo() ? "ACTIVO" : "INACTIVO"), xJugador, yJugador);

        // --- 3. INFORMACIÓN DEL ENEMIGO (Columna Derecha) ---
        float xEnemigo = viewport.getWorldWidth() / 2 + 100; // Ajuste para que la columna empiece en 700
        float yEnemigo = viewport.getWorldHeight() - 200;
        float factorDificultad = getFactorDificultad();

        // FACTORES GLOBALES DE UPGRADE (Aumentan por cada mejora del jugador)
        float factorUpgradeVel = buffs.getEnemySpeedMultiplier();
        float factorUpgradeHP = buffs.getEnemyHealthMultiplier();

        game.getFont().getData().setScale(1.2f);
        game.getFont().draw(batch, "=== AMENAZA (RONDA " + ronda + ") ===", xEnemigo, yEnemigo);
        yEnemigo -= 30;

        // FACTOR DE RONDA (Afecta a todo el juego)
        game.getFont().draw(batch, "Factor de Ronda: " + String.format("%.2f", factorDificultad) + "x", xEnemigo, yEnemigo);
        yEnemigo -= 25;

        // VELOCIDAD ENEMIGA: Factor de Ronda x Factor Upgrade
        game.getFont().draw(batch, "Velocidad Base Enemiga: " + String.format("%.2f", factorUpgradeVel) + "x (Global)", xEnemigo, yEnemigo);
        yEnemigo -= 25;

        // VIDA ENEMIGA: Factor de Ronda x Factor Upgrade
        game.getFont().draw(batch, "Vida Base Enemiga: " + String.format("%.2f", factorUpgradeHP) + "x (Global)", xEnemigo, yEnemigo);
        yEnemigo -= 25;

        game.getFont().draw(batch, "Spawn Rate: " + String.format("%.2f", buffs.getEnemySpawnRateMultiplier()) + "x", xEnemigo, yEnemigo);
        yEnemigo -= 25;
        game.getFont().draw(batch, "Próximo Jefe: Ronda " + (ronda + (5 - (ronda % 5))), xEnemigo, yEnemigo);


        game.getFont().getData().setScale(1.0f); // Restaurar tamaño
        batch.end();
    }



}
