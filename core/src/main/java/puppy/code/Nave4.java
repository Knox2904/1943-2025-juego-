package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;

import java.util.ArrayList;

public class Nave4 extends GameObject implements IDestruible {

    private boolean destruida = false;
    private float combustible;
    private final float MAX_COMBUSTIBLE = 100f; // 100% de combustible
    private final float CONSUMO_POR_SEGUNDO = 2f; // Gasto pasivo

    // Constante de daño (para usar en checkCollision si quieres)
    private final int DAÑO_POR_GOLPE = 20;

    private Sprite spr;
    private Sound sonidoHerido;
    private Sound soundBala;
    private Texture txBala;
    private Texture txAliado;

    private boolean herido = false;
    private float tiempoHeridoMax = 1f;
    private float tiempoHerido;
    private float stateTime = 0;

    private int nivelArma = 0;
    private final int MAX_NIVEL_ARMA = 2;
    private IFireStrategy fireStrategy;

    private ArrayList<SideShip> aliados = new ArrayList<>();
    private float fireRateBase = 0.2f;
    private float fireTimer = 0f;

    public Nave4(float x, float y, Texture tx, Sound soundChoque, Texture txBala, Sound soundBala, Texture txAliado) {
        super(x, y, tx);
        sonidoHerido = soundChoque;
        this.soundBala = soundBala;
        this.txBala = txBala;
        this.combustible = MAX_COMBUSTIBLE;
        spr = new Sprite(tx);
        spr.setPosition(x, y);
        spr.setBounds(x, y, 45, 45);
        this.fireStrategy = new OffsetFireStrategy(new float[]{0f});
        this.txAliado = txAliado;
    }

    // --- LÓGICA UNIFICADA DE UPDATE ---
    @Override
    public void update(float delta, PantallaJuego juego) {
        stateTime += delta;
        fireTimer -= delta;

        // 1. MOVIMIENTO (Siempre activo, incluso herido)
        // -------------------------------------------------
        float speedMod = BuffManager.getInstance().getPlayerSpeedModifier();
        final float PLAYER_SPEED = 500f * speedMod;

        float x = spr.getX();
        float y = spr.getY();

        if (Gdx.input.isKeyPressed(Input.Keys.A)) x -= PLAYER_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.D)) x += PLAYER_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.W)) y += PLAYER_SPEED * delta;
        if (Gdx.input.isKeyPressed(Input.Keys.S)) y -= PLAYER_SPEED * delta;

        // Límites de Pantalla

        float screenWidth = 1200f;
        float screenHeight = 800f;

        if (x < 0) x = 0;
        if (x > screenWidth - spr.getWidth()) x = screenWidth - spr.getWidth();
        if (y < 0) y = 0;
        if (y > screenHeight - spr.getHeight()) y = screenHeight - spr.getHeight();

        // Actualizar posición visual y lógica
        spr.setPosition(x, y);
        this.position.x = x;
        this.position.y = y;


        // 2. ESTADO Y COMBUSTIBLE
        // -------------------------------------------------
        if (!herido) {
            // Solo consume combustible pasivo si NO está herido (tiempo de gracia)
            // O puedes dejar que consuma siempre si prefieres.
            combustible -= CONSUMO_POR_SEGUNDO * delta;
        } else {

            tiempoHerido -= delta;
            if (tiempoHerido <= 0) {
                herido = false;
            }

        }

        // Muerte por falta de combustible
        if (combustible <= 0) {
            combustible = 0;
            destruida = true;
        }


        // 3. DISPARO
        // -------------------------------------------------
        if (Gdx.input.isKeyPressed(Input.Keys.SPACE)) {
            if (fireTimer <= 0f) {
                float spawnX = spr.getX() + spr.getWidth() / 2 - 5;
                float spawnY = spr.getY() + spr.getHeight() - 5;

                fireStrategy.fire(juego, txBala, spawnX, spawnY);
                soundBala.play(0.25f);

                float fireRateMod = BuffManager.getInstance().getFireRateModifier();
                float fireRateActual = fireRateBase / fireRateMod;
                fireTimer = fireRateActual;
            }
        }

        // 4. CHEATS
        // -------------------------------------------------
        if (Gdx.input.isKeyJustPressed(Input.Keys.P)) {
            juego.crearPowerUpEn(position.x, position.y + 100, TipoPowerUp.MEJORA_ARMA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.O)) {
            juego.crearPowerUpEn(position.x, position.y + 100, TipoPowerUp.COMBUSTIBLE);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_9)) {
            juego.crearPowerUpEn(position.x, position.y + 100, TipoPowerUp.NAVE_ALIADA);
        }
        if (Gdx.input.isKeyJustPressed(Input.Keys.L)) {
            juego.mostrarPantallaMejoras();
        }

        // 5. ACTUALIZAR ALIADOS
        // -------------------------------------------------
        for (SideShip aliado : aliados) {
            aliado.setTargetPosition(position.x, position.y);
            aliado.update(delta, juego);
        }
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (herido) {
            // Efecto de parpadeo visual
            float flicker = (float) Math.sin(stateTime * 30);
            float alpha = 0.5f + 0.5f * flicker;
            spr.setColor(1, 1, 1, alpha);
        } else {
            spr.setColor(1, 1, 1, 1);
        }

        spr.draw(batch);

        for (SideShip aliado : aliados) {
            aliado.draw(batch);
        }
    }

    // Método corregido para usar la constante de daño
    public boolean checkCollision(GameObject other, float delta) {
        if (!herido && this.getHitbox().overlaps(other.getHitbox())) {

            // Usamos la constante o un valor fijo
            this.recibirHit(DAÑO_POR_GOLPE, delta);
            return true;
        }
        return false;
    }

    @Override
    public void recibirHit(int cantidad, float delta) {

        if (herido) return;


        // Resta la cantidad correcta
        combustible -= cantidad;

        herido = true;
        tiempoHerido = tiempoHeridoMax;
        sonidoHerido.play();

        // Resetea poder al ser golpeado (penalización)
        nivelArma = 0;
        fireStrategy = new OffsetFireStrategy(new float[]{0f});

        if (combustible <= 0) {
            combustible = 0;
            destruida = true;
        }

    }

    @Override
    public boolean estaDestruido() {
        return destruida;
    }

    @Override
    public int getVidas() {
        return (int) this.combustible;
    }

    public boolean estaHerido() {
        return herido;
    }

    // --- Métodos de Lógica de Juego ---

    public void mejorarArma(PantallaJuego juego) {
        nivelArma++;
        switch (nivelArma) {
            case 1: // Nivel 1: Doble
                fireStrategy = new OffsetFireStrategy(new float[]{-10f, 10f});
                break;
            case 2: // Nivel 2: Triple
                fireStrategy = new OffsetFireStrategy(new float[]{-15f, 0f, 15f});
                break;
            default: // Maximo alcanzado
                nivelArma = MAX_NIVEL_ARMA;
                juego.agregarScore(100);
                break;
        }
    }

    public void agregarCombustible(float cantidad) {
        this.combustible += cantidad;
        if (this.combustible > MAX_COMBUSTIBLE) {
            this.combustible = MAX_COMBUSTIBLE;
        }
    }

    public void agregarAliado() {
        if (aliados.size() < 2) {
            float offsetX = (aliados.isEmpty()) ? -60f : 60f;
            SideShip nuevoAliado = new SideShip(
                position.x,
                position.y,
                this.txAliado,
                this.txBala,
                offsetX
            );
            aliados.add(nuevoAliado);
        }
    }

    public Rectangle getHitbox() {
        // Asegura que el hitbox siga al sprite
        spr.getBoundingRectangle().setPosition(position.x, position.y);
        return spr.getBoundingRectangle();
    }

    public int getX() { return (int) spr.getX(); }
    public int getY() { return (int) spr.getY(); }
    public float getMaxCombustible() { return this.MAX_COMBUSTIBLE; }
    public float getCombustible() { return this.combustible; }
}
