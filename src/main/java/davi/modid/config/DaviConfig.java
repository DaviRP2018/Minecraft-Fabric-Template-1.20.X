package davi.modid.config;

import davi.modid.Davi;
import mod.azure.azurelib.config.Config;
import mod.azure.azurelib.config.Configurable;

@Config(id = Davi.MOD_ID)
public class DaviConfig {

    @Configurable
    public boolean bullets_disable_iframes_on_players = false;

    @Configurable
    public float pistol_damage = 5F;
}
