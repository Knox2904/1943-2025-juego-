package puppy.code;


public class BuffManager {

    private static BuffManager instance;


    private BuffManager() {
        this.playerSpeedModifier = 1.0f;
        this.fireRateModifier = 1.0f;
        this.extraDamage = 0;
    }

    //método público para OBTENER la instancia
    public static BuffManager getInstance() {
        if (instance == null) {
            instance = new BuffManager();
        }
        return instance;
    }

    // --- Variables de Buffs ---
    // Estas son las mejoras que guardaremos
    private float playerSpeedModifier;
    private float fireRateModifier;
    private int extraDamage;

    // --- Métodos "Getters" ---

    public float getPlayerSpeedModifier() {
        return playerSpeedModifier;
    }

    public float getFireRateModifier() {
        return fireRateModifier;
    }

    public int getExtraDamage() {
        return this.extraDamage;
    }

    // --- Método Setter ---

    /**
     * Aplica una mejora específica al manager.
     * Este método será llamado por la 'UpgradeScreen'
     */
    public void applyBuff(TipoMejora tipo) {
        switch (tipo) {
            case VELOCIDAD_JUGADOR:
                this.playerSpeedModifier += 50f; // +10% de velocidad
                break;
            case CADENCIA_DISPARO:
                this.fireRateModifier += 150f; // +15% de cadencia
                break;
            case DAÑO_EXTRA:
                this.extraDamage += 1; // Daño + 1
                break;
        }
    }

    /**
     * Resetea todos los buffs a sus valores por defecto.
     * Útil para cuando el jugador muere e inicia una nueva partida.
     */
    public void resetBuffs() {
        this.playerSpeedModifier = 1.0f;
        this.fireRateModifier = 1.0f;
        this.extraDamage = 0;
    }
}
