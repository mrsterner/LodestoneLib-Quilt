package com.sammy.lodestone.mixin;

import com.sammy.lodestone.systems.rendering.shader.ExtendedShader;
import net.fabricmc.fabric.impl.client.rendering.FabricShaderProgram;
import net.minecraft.client.gl.ShaderProgram;
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
			return FabricShaderProgram.rewriteAsId(id, name);
		}

		return id;
	}
}
