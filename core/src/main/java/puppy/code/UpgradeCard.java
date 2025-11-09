package puppy.code;

import com.badlogic.gdx.graphics.Texture;

public class UpgradeCard {

    private String titulo;
    private String descripcion;
    private Texture icono; // (Podemos añadir esto más tarde)
    private TipoMejora tipo; // El 'buff' que aplica

    public UpgradeCard(String titulo, String descripcion, TipoMejora tipo) {
        this.titulo = titulo;
        this.descripcion = descripcion;
        this.tipo = tipo;
        // this.icono = new Texture(...);
    }

    // --- Getters ---

    public String getTitulo() {
        return titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public TipoMejora getTipo() {
        return tipo;
    }
}
