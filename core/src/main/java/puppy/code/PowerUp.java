package puppy.code;

import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;


public abstract class PowerUp extends GameObject {

    private float scrollSpeed = 100f;
    private boolean destroyed = false;
    protected TipoPowerUp tipo;
    protected Sound sonido;

    public PowerUp(float x, float y, Texture texture , Sound sonido) {
        super(x, y, texture);
        this.hitbox = new Rectangle(x, y, texture.getWidth(), texture.getHeight());
        this.sonido = sonido;
    }


    public final void recoger(Nave4 nave) {
        reproducirSonido();      //(Común)
        aplicarEfecto(nave);     //(Abstracto - Diferente en cada hijo)
        destruir();              //(Común)
    }


    protected abstract void aplicarEfecto(Nave4 nave);


    private void reproducirSonido() {
        if (sonido != null) {
            long id = sonido.play(1.0f); // Volumen máximo
            // Opcional: Variar el tono un poquito para que suene natural
            sonido.setPitch(id, MathUtils.random(0.9f, 1.1f));
        }
    }

    public void destruir() {
        this.destroyed = true;
    }

    public boolean estaDestruido() {
        return destroyed;
    }

    @Override
    public void update(float delta, PantallaJuego juego) {
        position.y -= scrollSpeed * delta;
        spr.setPosition(position.x, position.y);
        // --- CORRECCIÓN: LÍMITE DE PANTALLA ---
        // Si baja más de -100 (sale de la pantalla), se destruye automáticamente.
        // Esto evita que el EliminaBuffs persiga cosas fuera del mapa.
        if (position.y < -100) {
            destruir();
        }
    }



}
