package puppy.code;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;

public class Configuracion {
    private static Configuracion instance;
    private Preferences prefs;

    // Nombres de las claves para guardar
    private static final String PREF_MUSIC_VOLUME = "volume_music";
    private static final String PREF_SOUND_VOLUME = "volume_sound";

    private float musicVolume;
    private float soundVolume;

    private Configuracion() {
        // "SpaceNavConfig" es el nombre del archivo de guardado
        prefs = Gdx.app.getPreferences("SpaceNavConfig");

        // Cargamos los valores. Si no existen, usamos 0.5 (50%) por defecto
        musicVolume = prefs.getFloat(PREF_MUSIC_VOLUME, 0.5f);
        soundVolume = prefs.getFloat(PREF_SOUND_VOLUME, 0.5f);
    }

    public static Configuracion getInstance() {
        if (instance == null) {
            instance = new Configuracion();
        }
        return instance;
    }

    public float getMusicVolume() {
        return musicVolume;
    }

    public void setMusicVolume(float volume) {
        this.musicVolume = Math.max(0, Math.min(1, volume)); // Limitar entre 0 y 1
        prefs.putFloat(PREF_MUSIC_VOLUME, this.musicVolume);
        prefs.flush(); // Guardar en disco inmediatamente
    }

    public float getSoundVolume() {
        return soundVolume;
    }

    public void setSoundVolume(float volume) {
        this.soundVolume = Math.max(0, Math.min(1, volume)); // Limitar entre 0 y 1
        prefs.putFloat(PREF_SOUND_VOLUME, this.soundVolume);
        prefs.flush();
    }
}
