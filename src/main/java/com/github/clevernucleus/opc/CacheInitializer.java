package com.github.clevernucleus.opc;

import com.github.clevernucleus.opc.api.CacheableValue;
import com.github.clevernucleus.opc.api.OfflinePlayerCache;
import com.github.clevernucleus.opc.impl.OfflinePlayerCacheCommand;
import com.github.clevernucleus.opc.impl.OfflinePlayerCacheImpl;
import com.github.clevernucleus.opc.test.LevelValue;
import dev.onyxstudios.cca.api.v3.component.ComponentKey;
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentFactoryRegistry;
import dev.onyxstudios.cca.api.v3.level.LevelComponentInitializer;

import net.minecraft.util.Identifier;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

public class CacheInitializer implements ModInitializer, LevelComponentInitializer {
	/**
	 * Runs the mod initializer.
	 */

	public static final ComponentKey<OfflinePlayerCacheImpl> CACHE = ComponentRegistry
		.getOrCreate(new Identifier("opc", "cache"), OfflinePlayerCacheImpl.class);

	public static final CacheableValue<Integer> LEVEL_VALUE = OfflinePlayerCache
		.register(new LevelValue());

	@Override
	public void onInitialize() {
		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> OfflinePlayerCacheCommand.register(dispatcher));
	}

	/**
	 * Called to register component factories for statically declared component types.
	 *
	 * <p><strong>The passed registry must not be held onto!</strong> Static component factories
	 * must not be registered outside of this method.
	 *
	 * @param registry a {@link LevelComponentFactoryRegistry} for <em>statically declared</em> components
	 */
	@Override
	public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
		registry.register(CACHE, worldProperties -> new OfflinePlayerCacheImpl());
	}
}
