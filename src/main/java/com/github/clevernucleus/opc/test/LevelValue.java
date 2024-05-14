package com.github.clevernucleus.opc.test;

import java.util.Random;

import com.github.clevernucleus.opc.api.CacheableValue;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;

public final class LevelValue implements CacheableValue<Integer> {
	private final Identifier id;

	public LevelValue() {
		this.id = new Identifier("opc", "level");
	}

	@Override
	public Integer get(final ServerPlayerEntity player) {
		return new Random().nextInt(100);
	}

	@Override
	public Integer readFromNbt(final NbtCompound tag) {
		return tag.getInt("level");
	}

	@Override
	public void writeToNbt(final NbtCompound tag, final Object value) {
		tag.putInt("level", (Integer) value);
	}

	@Override
	public Identifier id() {
		return this.id;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof LevelValue levelValue))
			return false;

		return this.id.equals(levelValue.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}

	@Override
	public String toString() {
		return this.id.toString();
	}
}
