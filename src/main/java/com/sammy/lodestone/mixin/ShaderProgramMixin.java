package com.sammy.lodestone.mixin;

import com.sammy.lodestone.systems.rendering.ExtendedShader;
import net.minecraft.client.render.ShaderProgram;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
@Mixin(ShaderProgram.class)
abstract class ShaderProgramMixin {
	@Shadow
	@Final
	private String name;

	@ModifyArg(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Identifier;<init>(Ljava/lang/String;)V"), allow = 1)
	private String modifyProgramId(String id) {
		ShaderProgram program = ShaderProgram.class.cast(this);
		if (program instanceof ExtendedShader) {
			return ExtendedShader.rewriteAsId(id, name);
		}

		return id;
	}
}
