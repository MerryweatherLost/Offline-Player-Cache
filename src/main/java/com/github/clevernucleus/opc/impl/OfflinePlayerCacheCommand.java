package com.github.clevernucleus.opc.impl;


import static net.minecraft.server.command.CommandManager.*;

import java.util.UUID;
import java.util.function.Function;

import com.github.clevernucleus.opc.CacheInitializer;
import com.github.clevernucleus.opc.api.CacheableValue;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.IdentifierArgumentType;
import net.minecraft.command.argument.UuidArgumentType;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;

public final class OfflinePlayerCacheCommand {
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_KEYS = (context, builder) -> CommandSource.suggestIdentifiers(OfflinePlayerCacheImpl.keys(), builder);
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_NAMES = (ctx, builder) -> {
		final MinecraftServer server = ctx.getSource().getServer();
		OfflinePlayerCacheImpl impl = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());
		impl.playerNames(server).forEach(builder::suggest);
		return builder.buildFuture();
	};
	private static final SuggestionProvider<ServerCommandSource> SUGGEST_UUIDS = (ctx, builder) -> {
		final MinecraftServer server = ctx.getSource().getServer();
		OfflinePlayerCacheImpl impl = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());
		impl.playerIds(server).forEach(id -> builder.suggest(String.valueOf(id)));
		return builder.buildFuture();
	};

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
		dispatcher.register(literal("opc")
			.requires(serverCommandSource -> serverCommandSource.hasPermissionLevel(2))
				.then(literal("get")
					.then(literal("name")
						.then(argument("name", StringArgumentType.string())
							.suggests(SUGGEST_NAMES)
								.then(argument("key", IdentifierArgumentType.identifier())
									.suggests(SUGGEST_KEYS)
										.executes(context -> executeGetKey(context, ctx -> StringArgumentType.getString(ctx, "name"))))))
					.then(literal("uuid")
						.then(argument("uuid", StringArgumentType.string())
							.suggests(SUGGEST_UUIDS)
								.then(argument("key", IdentifierArgumentType.identifier())
									.suggests(SUGGEST_KEYS)
										.executes(context -> executeGetKey(context, ctx -> UuidArgumentType.getUuid(ctx, "uuid")))))))
				.then(literal("remove")
					.then(literal("name")
						.then(argument("name", StringArgumentType.string())
							.suggests(SUGGEST_NAMES)
								.executes(context -> executeRemoveAllCachedTo(context, ctx -> StringArgumentType.getString(ctx, "name")))
									.then(argument("key", IdentifierArgumentType.identifier())
										.suggests(SUGGEST_KEYS)
											.executes(context -> executeRemoveKey(context, ctx -> StringArgumentType.getString(ctx, "name"))))))
					.then(literal("uuid")
						.then(argument("uuid", UuidArgumentType.uuid())
							.suggests(SUGGEST_UUIDS)
								.executes(context -> executeRemoveAllCachedTo(context, ctx -> UuidArgumentType.getUuid(ctx, "uuid")))
									.then(argument("key", IdentifierArgumentType.identifier())
										.suggests(SUGGEST_KEYS)
											.executes(context -> executeRemoveKey(context, ctx -> UuidArgumentType.getUuid(ctx, "uuid")))))))
				/*.then(literal("list") //TODO fix listing of cache values
					.then(literal("name")
						.then(argument("name", StringArgumentType.string())
							.suggests(SUGGEST_NAMES)
								.executes(context -> executeListKeys(context, ctx -> StringArgumentType.getString(ctx, "name")))))
					.then(literal("uuid")
						.then(argument("uuid", UuidArgumentType.uuid())
							.suggests(SUGGEST_UUIDS)
								.executes(context -> executeListKeys(context, ctx -> UuidArgumentType.getUuid(ctx, "uuid")))
								))));
				 */);


	}

	/*private static <T> int executeListKeys(CommandContext<ServerCommandSource> context, Function<CommandContext<ServerCommandSource>, T> input) {
		T id = input.apply(context);

		MinecraftServer server = context.getSource().getServer();
		OfflinePlayerCacheImpl opc = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());

		Map<CacheableValue<?>, ?> playerCache = (id instanceof String str ? opc.getPlayerCache(str) : (id instanceof UUID uuid ? opc.getPlayerCache(uuid) : null));

		if (playerCache == null) {
			return -1;
		}

		playerCache.forEach((cacheableValue, o) -> {
			context.getSource().sendFeedback(() -> (Text.literal(id + " -> " + cacheableValue.id() + "=" + o)).formatted(Formatting.GRAY), false);
		});

		return 1;
	}

	 */

	private static <T> int executeRemoveKey(CommandContext<ServerCommandSource> ctx, Function<CommandContext<ServerCommandSource>, T> input) {
		T id = input.apply(ctx);
		Identifier identifier = IdentifierArgumentType.getIdentifier(ctx, "key");
		CacheableValue<?> value = OfflinePlayerCacheImpl.getKey(identifier);

		if(value == null) {
			ctx.getSource().sendFeedback(() -> (Text.literal(id + " -> null key")).formatted(Formatting.RED), false);
			return -1;
		}

		MinecraftServer server = ctx.getSource().getServer();

		OfflinePlayerCacheImpl opc = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());

		if(id instanceof String str) {
			opc.uncache(str, value);
		} else if(id instanceof UUID uuid) {
			opc.uncache(uuid, value);
		}

		ctx.getSource().sendFeedback(() -> (Text.literal("-" + id + " -" + identifier)).formatted(Formatting.GRAY), false);

		return 1;
	}

	private static <T> int executeRemoveAllCachedTo(CommandContext<ServerCommandSource> context, Function<CommandContext<ServerCommandSource>, T> input) {
		T uuidOrPlayer = input.apply(context);
		MinecraftServer server = context.getSource().getServer();
		OfflinePlayerCacheImpl opc = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());
		boolean executed = (uuidOrPlayer instanceof String str ? opc.uncache(str) : (uuidOrPlayer instanceof UUID uuid && opc.uncache(uuid)));
		context.getSource().sendFeedback(() -> (Text.literal("-" + uuidOrPlayer + " -*")).formatted(Formatting.GRAY), false);
		return executed ? 1 : -1;
	}

	private static <T> int executeGetKey(CommandContext<ServerCommandSource> ctx, Function<CommandContext<ServerCommandSource>, T> input) {
		T id = input.apply(ctx);
		Identifier identifier = IdentifierArgumentType.getIdentifier(ctx, "key");
		CacheableValue<?> value = OfflinePlayerCacheImpl.getKey(identifier);

		if(value == null) {
			ctx.getSource().sendFeedback(() -> (Text.literal(id + " -> null key")).formatted(Formatting.RED), false);
			return -1;
		}

		MinecraftServer server = ctx.getSource().getServer();

		OfflinePlayerCacheImpl opc = CacheInitializer.CACHE.get(server.getOverworld().getLevelProperties());

		Object obj = (id instanceof String str ? opc.get(server, str, value) : (id instanceof UUID uuid? opc.get(server, uuid, value) : null));
		ctx.getSource().sendFeedback(() -> (Text.literal(id + " -> " + identifier + " = " + obj)).formatted(Formatting.GRAY), false);

		if(obj instanceof Number) {
			int number = (int)(Integer)obj;
			return Math.abs(number) % 16;
		}

		return 1;
	}


}
