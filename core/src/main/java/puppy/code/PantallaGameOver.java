package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.ScreenUtils;

public class PantallaGameOver implements Screen {

    private SpaceNavigation game;
    private OrthographicCamera camera;
    private Texture backgroundTexture; // Nueva variable para el fondo

    public PantallaGameOver(SpaceNavigation game) {
        this.game = game;

        camera = new OrthographicCamera();
        // CAMBIO: Usamos la misma resolución vertical que el juego
        camera.setToOrtho(false, 1200, 800);

        // Cargar la imagen de fondo (usamos la misma del juego para consistencia)
        backgroundTexture = new Texture(Gdx.files.internal("fin.jpg"));
    }

    @Override
    public void render(float delta) {
        ScreenUtils.clear(0, 0, 0.2f, 1);

        camera.update();
        game.getBatch().setProjectionMatrix(camera.combined);

        game.getBatch().begin();

        // 1. DIBUJAR FONDO (Estirado para cubrir la pantalla)
        game.getBatch().setColor(1, 1, 1, 1); // Asegurar color normal
        game.getBatch().draw(backgroundTexture, 0, 0, 1200, 800);

        // 2. DIBUJAR TEXTO "GAME OVER"
        game.getFont().setColor(Color.RED); // Color Rojo Dramático
        game.getFont().getData().setScale(2.0f); // Letra Grande
        game.getFont().draw(game.getBatch(), "GAME OVER !!!",
            0, 400, 480, Align.center, false);

        // 3. DIBUJAR INSTRUCCIÓN
        game.getFont().setColor(Color.WHITE); // Volver a Blanco
        game.getFont().getData().setScale(1.0f); // Letra Normal
        game.getFont().draw(game.getBatch(), "Pincha en cualquier lado para reiniciar ...",
            20, 300, 440, Align.center, true); // Con wrap (salto de línea) por si acaso

        game.getBatch().end();

        // Lógica de Reinicio
        if (Gdx.input.isTouched() || Gdx.input.isKeyJustPressed(Input.Keys.ANY_KEY)) {

            // ¡IMPORTANTE! Resetear los buffs solo al reiniciar
            BuffManager.getInstance().resetBuffs();

            // Iniciar nuevo juego con los parámetros correctos (7 params)
            Screen ss = new PantallaJuego(game, 1, 100f, 0, 10, 10, 10);
            ss.resize(480, 640);
            game.setScreen(ss);
            dispose();
        }
    }

    @Override
    public void dispose() {
        // Liberar la memoria de la textura
        backgroundTexture.dispose();
    }

    // Métodos vacíos requeridos por Screen
    @Override public void show() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
}
