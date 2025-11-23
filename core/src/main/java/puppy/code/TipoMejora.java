package puppy.code;

/**
 * Define todos los tipos de mejoras (buffs)
 * que el jugador puede seleccionar en la UpgradeScreen.
 * * Este 'enum' es usado por BuffManager para saber qué estadística aplicar
 * y por UpgradeCard para saber qué tipo de tarjeta es.
 */
public enum TipoMejora {
    // --- Mejoras de Jugador ---
    VELOCIDAD_JUGADOR,
    COMBUSTIBLE_MAXIMO,
    REDUCCION_CONSUMO_COMBUSTIBLE,

    // --- Mejoras de Arma ---
    CADENCIA_DISPARO,
    DAÑO_EXTRA,
    VELOCIDAD_BALA,
    EXPANSION_CANON,
    BALAS_PERFORANTES,

    // --- Mejoras de Aliados ---
    ALIADO_EXTRA, // (Para el sistema que ya tienes)
    CADENCIA_ALIADO,

    // --- Mejoras de Enemigos (Opcional) ---
    ENEMIGOS_MAS_LENTOS
}
