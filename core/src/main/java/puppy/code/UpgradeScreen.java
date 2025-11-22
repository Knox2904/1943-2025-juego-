package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.util.ArrayList;
import java.util.Collections;

public class UpgradeScreen implements Screen {

    private SpaceNavigation game;
    private Screen pantallaJuegoAnterior;
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    // --- CÁMARA Y VIEWPORT ---
    private OrthographicCamera camera;
    private Viewport viewport;

    private ArrayList<UpgradeCard> todasLasMejoras;
    private UpgradeCard[] opcionesActuales = new UpgradeCard[3];
    private Rectangle[] opcionesHitbox = new Rectangle[3];

    public UpgradeScreen(SpaceNavigation game, Screen pantallaJuego) {
        this.game = game;
        this.pantallaJuegoAnterior = pantallaJuego;
        this.batch = game.getBatch();
        this.font = game.getFont();
        this.shapeRenderer = new ShapeRenderer();

        // 1. Configurar cámara virtual (Igual que PantallaJuego)
        camera = new OrthographicCamera();
        viewport = new FitViewport(1200, 800, camera); // MUNDO DE 1200x800
        viewport.apply();

        todasLasMejoras = new ArrayList<>();
        inicializarBaraja();

        Collections.shuffle(todasLasMejoras);
        opcionesActuales[0] = todasLasMejoras.get(0);
        opcionesActuales[1] = todasLasMejoras.get(1);
        opcionesActuales[2] = todasLasMejoras.get(2);

        // 2. Definir zonas de clic usando el tamaño FIJO (1200)
        int anchoTarjeta = 140;
        int altoTarjeta = 200;
        int posY = 300; // Subí un poco las tarjetas para que se vean mejor

        // Usamos 1200 en lugar de Gdx.graphics.getWidth()
        int anchoMundo = 1200;
        int anchoTotalTarjetas = anchoTarjeta * 3;
        int espacioSobrante = anchoMundo - anchoTotalTarjetas;
        int espacio = espacioSobrante / 4;

        opcionesHitbox[0] = new Rectangle(espacio, posY, anchoTarjeta, altoTarjeta);
        opcionesHitbox[1] = new Rectangle(espacio * 2 + anchoTarjeta, posY, anchoTarjeta, altoTarjeta);
        opcionesHitbox[2] = new Rectangle(espacio * 3 + (anchoTarjeta * 2), posY, anchoTarjeta, altoTarjeta);
    }

    private void inicializarBaraja() {
        todasLasMejoras.add(new UpgradeCard("Motor Mejorado", "+10% Velocidad", TipoMejora.VELOCIDAD_JUGADOR));
        todasLasMejoras.add(new UpgradeCard("Cadencia Aumentada", "+15% Disparo", TipoMejora.CADENCIA_DISPARO));
        todasLasMejoras.add(new UpgradeCard("Munición Potenciada", "+1 Daño", TipoMejora.DAÑO_EXTRA));
        // Agrega más si tienes...
    }

    @Override
    public void render(float delta) {
        // Limpiar pantalla (aunque dibujaremos encima)
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Aplicar cámara al shapeRenderer (para el fondo negro transparente)
        shapeRenderer.setProjectionMatrix(camera.combined);

        // --- FONDO SEMI-TRANSPARENTE ---
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.85f);
        // Dibujamos el rectángulo negro del tamaño del MUNDO
        shapeRenderer.rect(0, 0, viewport.getWorldWidth(), viewport.getWorldHeight());
        shapeRenderer.end();
        Gdx.gl.glDisable(GL20.GL_BLEND);

        // --- LÓGICA DE CLIC (CORREGIDA) ---
        if (Gdx.input.isButtonJustPressed(0)) {
            // TRADUCCIÓN DE COORDENADAS:
            // Convertimos el clic de la pantalla (ej: pixel 1500) a coordenadas del juego (ej: pixel 900)
            Vector3 touchPoint = new Vector3();
            viewport.unproject(touchPoint.set(Gdx.input.getX(), Gdx.input.getY(), 0));

            for (int i = 0; i < 3; i++) {
                // Verificamos colisión con el punto traducido (touchPoint.x, touchPoint.y)
                if (opcionesHitbox[i].contains(touchPoint.x, touchPoint.y)) {
                    aplicarMejoraYVolver(opcionesActuales[i]);
                    return;
                }
            }
        }

        // --- DIBUJAR TEXTOS ---
        batch.setProjectionMatrix(camera.combined); // IMPORTANTE: Aplicar cámara al batch
        batch.begin();

        font.getData().setScale(1.8f);
        font.draw(batch, "¡NIVEL ALCANZADO! Elige una mejora:", 0, 700, 1200, 1, true);

        font.getData().setScale(1.2f);
        for (int i = 0; i < 3; i++) {
            Rectangle box = opcionesHitbox[i];
            UpgradeCard card = opcionesActuales[i];

            // Dibujar un borde simple (opcional, ayuda a ver dónde hacer clic)
            // batch.draw(texturaTarjeta, box.x, box.y, box.width, box.height);

            font.setColor(Color.CYAN);
            font.draw(batch, card.getTitulo(), box.x, box.y + box.height - 20, box.width, 1, true);

            font.setColor(Color.WHITE);
            font.draw(batch, card.getDescripcion(), box.x, box.y + box.height / 2, box.width, 1, true);
        }

        // Advertencia
        font.setColor(Color.RED);
        font.getData().setScale(1.0f);
        font.draw(batch, "ADVERTENCIA: La flota enemiga aumentará su agresividad.",
            0, 150, 1200, 1, true);
        font.setColor(Color.WHITE);

        batch.end();
    }

    private void aplicarMejoraYVolver(UpgradeCard card) {
        BuffManager.getInstance().applyBuff(card.getTipo());
        game.setScreen(pantallaJuegoAnterior);
        this.dispose();
    }

    @Override
    public void resize(int width, int height) {
        // Actualizar viewport al cambiar tamaño de ventana
        viewport.update(width, height, true);
    }

    // ... Otros métodos vacíos (show, pause, resume, hide) ...
    @Override public void show() {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
