package puppy.code;

public interface IDestruible {

    /**
     *Aplica una cantidad de daño al objeto
     * @param cantidad El daño a recibir.
     * **/
    void recibirHit(int cantidad , float delta);

    /**
     * Verifica si el objeto ya no tiene vidas.
     * @return true si está destruido, false en caso contrario.
     */
    boolean estaDestruido();

    /**
     * Obtiene las vidas restantes.
     * @return el número de vidas.
     */
    int getVidas();

}
