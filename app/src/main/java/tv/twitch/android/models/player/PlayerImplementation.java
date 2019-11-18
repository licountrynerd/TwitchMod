package tv.twitch.android.models.player;

import h.v.d.j;
import tv.twitch.android.mod.settings.PrefManager;

public enum PlayerImplementation {
    Core("playercore", "c"),
    Exo2("exoplayer_2", "e2");

    public static final Companion Companion = null;
    public final String mName;
    private final String mTag;

    private PlayerImplementation(String str, String str2) {
        this.mName = str;
        this.mTag = str2;
    }

    public static final class Companion {
        public final PlayerImplementation getProviderForName(String str) {
            if (PrefManager.isExoPlayerOn())
                return PlayerImplementation.Exo2;
            else
                return org(str);
        }

        public final PlayerImplementation org(String str) {
            return null;
        }
    }
}
