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

    // --- Variables de Enemigos ---
    private float enemySpeedMultiplier = 1.0f;
    private float enemyHealthMultiplier = 1.0f;
    private float enemySpawnRateMultiplier = 1.0f; // Para que salgan más rápido

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

    public float getEnemySpeedMultiplier() { return enemySpeedMultiplier; }

    public float getEnemySpawnRateMultiplier() { return enemySpawnRateMultiplier; }

    // --- Método Setter ---

    /**
     * Aplica una mejora específica al manager.
     * Este método será llamado por la 'UpgradeScreen'
     */
    public void applyBuff(TipoMejora tipo) {
        switch (tipo) {
            case VELOCIDAD_JUGADOR:
                this.playerSpeedModifier += 0.10f; // +10% de velocidad
                break;
            case CADENCIA_DISPARO:
                this.fireRateModifier += 0.15f; // +15% de cadencia
                break;
            case DAÑO_EXTRA:
                this.extraDamage += 1; // Daño + 1
                break;
        }
        increaseDifficulty();
    }

    public void increaseDifficulty() {
        // Cada mejora del jugador hace a los enemigos un 5% más rápidos
        this.enemySpeedMultiplier += 0.05f;

        // Y hace que aparezcan un 2% más seguido
        this.enemySpawnRateMultiplier += 2.00f;
    }



    /**
     * Resetea todos los buffs a sus valores por defecto.
     * Útil para cuando el jugador muere e inicia una nueva partida.
     */
    public void resetBuffs() {
        // --- Resetea Jugador ---
        this.playerSpeedModifier = 1.0f;
        this.fireRateModifier = 1.0f;
        this.extraDamage = 0;

        // --- Resetea Enemigos  ---
        this.enemySpeedMultiplier = 1.0f;
        this.enemySpawnRateMultiplier = 1.0f;


    }
}
