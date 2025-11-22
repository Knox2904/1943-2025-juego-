package puppy.code;

import java.util.ArrayList;
import java.util.Random;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;




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
    private Texture txKamikazeS; // Nuevo
    private OleadaFactory factory;
    //  Variables añadidas
    private ArrayList<EntidadJuego> enemigos = new ArrayList<>();
    private ArrayList<EntidadJuego> enemigosPendientes = new ArrayList<>();
    private float spawnTimerEnemigo = 0f; // Timer para el kamikaze (5 segundos)

    private float anuncioRondaTimer = 3.0f;



    public PantallaJuego(SpaceNavigation game, int ronda, float combustible, int score,
                         int velXAsteroides, int velYAsteroides, int cantAsteroides) {
        this.game = game;
        this.ronda = ronda;
        this.score = score;
        this.velXAsteroides = velXAsteroides;
        this.velYAsteroides = velYAsteroides;
        this.cantAsteroides = cantAsteroides;

        batch = game.getBatch();
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 480, 640);

        // --- Cargas de Assets (Sonidos y Texturas) ---
        explosionSound = Gdx.audio.newSound(Gdx.files.internal("explosion.ogg"));
        explosionSound.setVolume(1, 0.25f);
        // (La música ya la maneja SpaceNavigation, puedes borrar estas líneas de gameMusic)
        // gameMusic = ...

        powerUpTexture = new Texture(Gdx.files.internal("powerUpDobleTiro.png"));
        texturaAliado = new Texture(Gdx.files.internal("MainShip3.png"));
        backgroundTexture = new Texture(Gdx.files.internal("fondo jugando.jpg"));
        txEnemigo = new Texture(Gdx.files.internal("Kamikaze.png"));
        txAsteroide = new Texture(Gdx.files.internal("aGreyMedium4.png"));
        txBalaEnemiga = new Texture(Gdx.files.internal("Rocket2.png"));
        txTank = new Texture(Gdx.files.internal("tanque 2025.png"));
        txKamikazeS = new Texture(Gdx.files.internal("kamikazeS.png"));

        // Crear textura barra
        com.badlogic.gdx.graphics.Pixmap pixmap = new com.badlogic.gdx.graphics.Pixmap(1, 1, com.badlogic.gdx.graphics.Pixmap.Format.RGBA8888);
        pixmap.setColor(com.badlogic.gdx.graphics.Color.WHITE);
        pixmap.fill();
        txBarra = new Texture(pixmap);
        pixmap.dispose();
        game.playMusic();

        // Crear Nave
        nave = new Nave4((float) Gdx.graphics.getWidth() / 2 - 50, 30f,
            new Texture(Gdx.files.internal("MainShip3.png")),
            Gdx.audio.newSound(Gdx.files.internal("hurt.ogg")),
            new Texture(Gdx.files.internal("Rocket2.png")),
            Gdx.audio.newSound(Gdx.files.internal("pop-sound.mp3")),
            texturaAliado
        );

        // --- INICIAR LA PRIMERA OLEADA ---
        // En lugar de todo el código repetido, solo llamamos al método:
        iniciarRonda();
    }

        public void dibujaEncabezado() {
            float anchoBarraTotal = 200f;
            float altoBarra = 20f;
            float margenX = 10f;
            float margenY = Gdx.graphics.getHeight() - 40f;

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
            game.getFont().draw(batch, "Score: " + this.score, Gdx.graphics.getWidth() - 150, Gdx.graphics.getHeight() - 20);
            game.getFont().draw(batch, "High: " + game.getHighScore(), Gdx.graphics.getWidth() / 2 - 50, Gdx.graphics.getHeight() - 20);
            game.getFont().getData().setScale(1.0f);
            game.getFont().draw(batch, "FUEL", margenX + 5, margenY + 15);
        }


    public Nave4 getNave() { return nave; }

    @Override
    public void render(float delta) {

        // --- 1. ACTUALIZACIONES ---
        nave.update(delta, this);

        // 1. Actualizar Balas JUGADOR
        for (Bullet b : balasJugador) {
            b.update();
            if (b.getX() < -10 || b.getX() > Gdx.graphics.getWidth() + 10 ||
                b.getY() < -10 || b.getY() > Gdx.graphics.getHeight() + 10) {
                b.recibirHit(1, 0);
            }
        }

        // 2. Actualizar Balas ENEMIGAS
        for (Bullet b : balasEnemigas) {
            b.update();
            // Check salida de pantalla
            if (b.getY() < -50 || b.getY() > Gdx.graphics.getHeight() + 50) {
                b.recibirHit(1, 0);
            }
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

        // --- 3. DIBUJADO ---
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.begin();

        // 1. FONDO (Primero)
        batch.draw(backgroundTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());

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
                0,
                Gdx.graphics.getHeight() / 2 + 50,
                Gdx.graphics.getWidth(),
                com.badlogic.gdx.utils.Align.center,
                false);

            game.getFont().setColor(1, 1, 1, 1); // Restaurar Blanco
            game.getFont().getData().setScale(1.0f); // Restaurar tamaño
        }

        // 4. HUD (Encima de todo)
        dibujaEncabezado();

        batch.end();



        // --- 4. COLISIONES ---

            // A. Balas vs Enemigos (Cualquier tipo)
            for (int i = 0; i < balasJugador.size(); i++) {
                Bullet b = balasJugador.get(i);
                if (b.estaDestruido()) continue;

                for (EntidadJuego enemigo : enemigos) {
                    if (!enemigo.estaDestruido()) {
                        if (b.checkCollision(enemigo.getHitbox())) {
                            explosionSound.play(0.35f);
                            enemigo.recibirHit(1, delta); // Dañar enemigo
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


    public boolean agregarBala(Bullet bb) {
    	return balasJugador.add(bb);
    }

    public boolean agregarBalaEnemiga(Bullet bb) {
        return balasEnemigas.add(bb);
    }

	@Override
	public void show() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resize(int width, int height) {
		// TODO Auto-generated method stub

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
	}

    public void crearPowerUpEn(float x, float y, TipoPowerUp tipo) {
        PowerUp p = null;

        switch (tipo) {
            case MEJORA_ARMA:

                p = new PowerUpArma(x, y, powerUpTexture, this);
                break;
            case COMBUSTIBLE:

                p = new PowerUpCombustible(x, y, powerUpTexture);
                break;
            case NAVE_ALIADA:
                p = new PowerUpAliado(x, y, powerUpTexture);
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
        // 1. Probabilidad: 20% de soltar algo (ajusta este 20)
        if (MathUtils.random(1, 100) <= 20) {

            // 2. Elegir un tipo aleatorio
            // TipoPowerUp.values() nos da un array con todos los tipos: [MEJORA_ARMA, COMBUSTIBLE, NAVE_ALIADA]
            TipoPowerUp[] tipos = TipoPowerUp.values();
            int indiceRandom = MathUtils.random(0, tipos.length - 1);
            TipoPowerUp tipoElegido = tipos[indiceRandom];

            // 3. Crear el PowerUp
            crearPowerUpEn(x, y, tipoElegido);
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
     */
    private void iniciarRonda() {
        // 1. Configurar Factory según la ronda
        if (ronda == 1) {
            factory = new EnemigoT1(txAsteroide, txEnemigo);
        } else {
            factory = new EnemigoT2(txKamikazeS, txTank, txBalaEnemiga);
        }

        // 2. Calcular presupuesto
        Random r = new Random();
        int presupuesto = 10 + ((ronda - 1) * 5);

        // 3. Bucle de compra de enemigos (LA LÓGICA QUE FALTABA)
        while (presupuesto > 0) {
            float x = r.nextInt(Gdx.graphics.getWidth() - 64);
            float y = Gdx.graphics.getHeight() + 50;

            EntidadJuego nuevoEnemigo = null;
            int costo = 0;

            // --- Lógica copiada del constructor ---
            if (ronda == 1) {
                if (r.nextBoolean()) {
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this); // Asteroide
                    if (nuevoEnemigo instanceof Ball2) {
                        Ball2 b = (Ball2) nuevoEnemigo;
                        float velocidadLoca = 150f + r.nextInt((int) MathUtils.random(500f, 1000f));
                        float angulo = MathUtils.random(0f, 360f);
                        b.setXSpeed(MathUtils.cosDeg(angulo) * velocidadLoca);
                        b.setySpeed(MathUtils.sinDeg(angulo) * velocidadLoca);
                    }
                } else {
                    nuevoEnemigo = factory.createEnemigoT2(x, y, this); // Kamikaze
                }
                costo = 1;
            } else {
                // Ronda 2+
                int dado = r.nextInt(100);
                if (dado < 40 && presupuesto >= 3) {
                    nuevoEnemigo = factory.createEnemigoT2(x, y, this); // Tanque
                    costo = 3;
                } else if (dado < 70 && presupuesto >= 2) {
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this); // Kamikaze S
                    costo = 2;
                } else {
                    nuevoEnemigo = factory.createEnemigoT1(x, y, this); // Fallback
                    costo = 2;
                    if (presupuesto < costo) break;
                }
            }
            // --------------------------------------

            if (nuevoEnemigo != null) {
                enemigosPendientes.add(nuevoEnemigo);
                presupuesto -= costo;
            } else {
                break;
            }
        }
    }




}
