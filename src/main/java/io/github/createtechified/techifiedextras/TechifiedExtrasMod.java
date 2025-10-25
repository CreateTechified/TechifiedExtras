package io.github.createtechified.techifiedextras;

import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.slf4j.Logger;

@Mod(TechifiedExtrasMod.MOD_ID)
public class TechifiedExtrasMod {
    public static final String MOD_ID = "techifiedextras";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TechifiedExtrasMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();
    }
}