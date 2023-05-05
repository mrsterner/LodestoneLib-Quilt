package com.sammy.lodestone.mixin;

import com.sammy.lodestone.forge.INBTSerializableCompound;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BlockEntity.class)
public abstract class BlockEntityMixin implements INBTSerializableCompound {
	@Unique
	private NbtCompound lodestone$extraData = null;

	@Shadow
	public abstract void readNbt(NbtCompound tag);

	@Shadow
	public abstract NbtCompound toIdentifiedLocatedNbt();

	@Inject(at = @At("RETURN"), method = "writeIdentifyingData(Lnet/minecraft/nbt/NbtCompound;)V")
	private void port_lib$saveMetadata(NbtCompound nbt, CallbackInfo ci) {
		if (lodestone$extraData != null && !lodestone$extraData.isEmpty()) {
			nbt.put("ForgeData", lodestone$extraData);
		}
	}

	@Inject(at = @At("RETURN"), method = "readNbt")
	private void port_lib$load(NbtCompound tag, CallbackInfo ci) {
		if (tag.contains("ForgeData")) {
			lodestone$extraData = tag.getCompound("ForgeData");
		} else if (tag.contains("create_ExtraEntityData")) {
			lodestone$extraData = tag.getCompound("create_ExtraEntityData");
		}
	}


	@Override
	public NbtCompound serializeNBT() {
		return this.toIdentifiedLocatedNbt();
	}

	@Override
	public void deserializeNBT(NbtCompound nbt) {
		deserializeNBT(null, nbt);
	}

	public void deserializeNBT(BlockState state, NbtCompound nbt) {
		this.readNbt(nbt);
	}
}
