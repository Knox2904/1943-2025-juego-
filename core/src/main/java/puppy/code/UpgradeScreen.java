package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import java.util.ArrayList;
import java.util.Collections;



public class UpgradeScreen implements Screen {

    private SpaceNavigation game;
    private Screen pantallaJuegoAnterior; // Para saber a dónde volver
    private SpriteBatch batch;
    private BitmapFont font;
    private ShapeRenderer shapeRenderer;

    private ArrayList<UpgradeCard> todasLasMejoras;
    private UpgradeCard[] opcionesActuales = new UpgradeCard[3];

    // Hitboxes para hacer clic en las tarjetas
    private Rectangle[] opcionesHitbox = new Rectangle[3];

    public UpgradeScreen(SpaceNavigation game, Screen pantallaJuego) {
        this.game = game;
        this.pantallaJuegoAnterior = pantallaJuego;
        this.batch = game.getBatch();
        this.font = game.getFont();
        this.shapeRenderer = new ShapeRenderer();

        // Crear la baraja de todas las mejoras posibles
        todasLasMejoras = new ArrayList<>();
        inicializarBaraja();

        //Barajar y tomar 3
        Collections.shuffle(todasLasMejoras);
        opcionesActuales[0] = todasLasMejoras.get(0);
        opcionesActuales[1] = todasLasMejoras.get(1);
        opcionesActuales[2] = todasLasMejoras.get(2);

        // Definir las zonas de clic
        int anchoTarjeta = 140; // Ancho de cada tarjeta
        int altoTarjeta = 200; // Alto de cada tarjeta
        int posY = 200;      // Altura Y

        // Calcula las posiciones X para 3 tarjetas centradas
        int espacio = (480 - (anchoTarjeta * 3)) / 4; // Espacio entre tarjetas

        opcionesHitbox[0] = new Rectangle(espacio, posY, anchoTarjeta, altoTarjeta);
        opcionesHitbox[1] = new Rectangle(espacio * 2 + anchoTarjeta, posY, anchoTarjeta, altoTarjeta);
        opcionesHitbox[2] = new Rectangle(espacio * 3 + (anchoTarjeta * 2), posY, anchoTarjeta, altoTarjeta);
    }

    private void inicializarBaraja() {
        // Aquí añadimos todas las mejoras que se pueden elegir
        todasLasMejoras.add(new UpgradeCard(
            "Motor Mejorado",
            "+10% Velocidad de Movimiento",
            TipoMejora.VELOCIDAD_JUGADOR));

        todasLasMejoras.add(new UpgradeCard(
            "Cadencia Aumentada",
            "+15% Velocidad de Disparo",
            TipoMejora.CADENCIA_DISPARO));

        todasLasMejoras.add(new UpgradeCard(
            "Munición Potenciada",
            "+1 Daño por bala",
            TipoMejora.DAÑO_EXTRA));
        // ... (Añade más mejoras aquí)
    }

    @Override
    public void render(float delta) {
        // 1. Detectar Clics (Esto estaba bien)
        if (Gdx.input.isButtonJustPressed(0)) {
            float mouseX = Gdx.input.getX();
            float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY();

            for (int i = 0; i < 3; i++) {
                if (opcionesHitbox[i].contains(mouseX, mouseY)) {
                    aplicarMejoraYVolver(opcionesActuales[i]);
                    return;
                }
            }
        }

        // --- ARREGLO DE TRANSPARENCIA ---
        // Esto deja el último frame del juego "pausado" en el fondo.

        // Habilita el blending (para la opacidad)
        Gdx.gl.glEnable(GL20.GL_BLEND);
        Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // Dibuja un "velo" negro semi-transparente SOBRE el juego
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.8f); // 80% de opacidad
        shapeRenderer.rect(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        shapeRenderer.end();

        // Deshabilita el blending para que la fuente se vea nítida
        Gdx.gl.glDisable(GL20.GL_BLEND);


        // --- DIBUJAR TEXTO (CON TAMAÑO Y CENTRADO) ---
        batch.begin();

        // Título de la pantalla (letra más grande)
        font.getData().setScale(1.8f);
        font.draw(batch, "¡NIVEL ALCANZADO! Elige una mejora:",
            0,                                  // X (0)
            Gdx.graphics.getHeight() - 50,      // Y (50px desde arriba)
            Gdx.graphics.getWidth(),            // Ancho (toda la pantalla)
            com.badlogic.gdx.utils.Align.center, // <-- ARREGLO DE CENTRADO
            true);

        // Dibuja las 3 tarjetas (letra un poco más pequeña)
        font.getData().setScale(1.2f);
        for (int i = 0; i < 3; i++) {
            Rectangle box = opcionesHitbox[i];
            UpgradeCard card = opcionesActuales[i];

            // (Aquí podrías dibujar un borde de tarjeta si tuvieras la textura)
            // batch.draw(texturaBordeTarjeta, box.x, box.y, box.width, box.height);

            // Dibuja el Título de la tarjeta
            font.draw(batch, card.getTitulo(),
                box.x,                      // X
                box.y + box.height - 20,    // Y (20px desde arriba de la caja)
                box.width,                  // Ancho
                com.badlogic.gdx.utils.Align.center, // <-- ARREGLO DE CENTRADO
                true);

            // Dibuja la Descripción de la tarjeta
            font.draw(batch, card.getDescripcion(),
                box.x,
                box.y + box.height / 2,     // Y (A la mitad de la caja)
                box.width,
                com.badlogic.gdx.utils.Align.center, // <-- ARREGLO DE CENTRADO
                true);
        }

        batch.end();
    }

    private void aplicarMejoraYVolver(UpgradeCard card) {
        // 1. Llama al Singleton para guardar la mejora
        BuffManager.getInstance().applyBuff(card.getTipo());

        // 2. Vuelve a la pantalla de juego
        game.setScreen(pantallaJuegoAnterior);

        // 3. Libera recursos de esta pantalla
        this.dispose();
    }

    // --- Otros métodos de Screen (puedes dejarlos vacíos) ---
    @Override
    public void show() {
        // (Aquí podrías pausar la música del juego si quisieras)
    }

    @Override
    public void resize(int width, int height) { }

    @Override
    public void pause() { }

    @Override
    public void resume() { }

    @Override
    public void hide() {
        // (Aquí podrías reanudar la música del juego)
    }

    @Override
    public void dispose() {
        shapeRenderer.dispose();
    }
}
