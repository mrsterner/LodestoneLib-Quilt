package com.sammy.lodestone.systems.block.sign;

import com.sammy.lodestone.systems.blockentity.LodestoneSignBlockEntity;
import net.minecraft.block.BlockEntityProvider;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallSignBlock;
import net.minecraft.block.WoodType;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.util.math.BlockPos;

public class LodestoneWallSignBlock extends WallSignBlock implements BlockEntityProvider {
	public LodestoneWallSignBlock(Settings settings, WoodType signType) {
		super(settings, signType);
	}

	@Override

	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new LodestoneSignBlockEntity(pos, state);
	}
}
