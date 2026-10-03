// ==================================================================
// This file is part of Smart Moving.
//
// Smart Moving is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Smart Moving is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Smart Moving. If not, see <http://www.gnu.org/licenses/>.
// ==================================================================

package net.smart.moving.render;

import org.lwjgl.opengl.GL11;

import net.minecraft.block.*;
import net.minecraft.block.material.*;
import net.minecraft.client.*;
import net.minecraft.client.entity.*;
import net.minecraft.client.gui.*;
import net.minecraft.client.gui.inventory.*;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.entity.RenderPlayer;
import net.minecraft.client.renderer.entity.layers.LayerArmorBase;
import net.minecraft.util.*;
import net.smart.moving.*;
import net.smart.moving.mixin.client.LayerArmorBaseAccessor;
import net.smart.moving.mixin.client.RenderLivingBaseAccessor;
import net.smart.moving.model.IModelPlayer;
import net.smart.moving.model.SMModel;
import net.smart.moving.model.MovingModelCore;
import net.smart.moving.util.MovingAngles;

public class SMRender {
	public static final int Scale = 0;
	public static final int NoScaleStart = 1;
	public static final int NoScaleEnd = 2;

	private final RenderPlayer renderer;
	private AbstractClientPlayer currentPlayer;
	private float currentYaw;
	private float currentPartialTicks;
	public final SMModel modelBipedMain;
	private IModelPlayer[] models;

	private static int _iOffset, _jOffset;
	private static Minecraft _minecraft;

	public SMRender(RenderPlayer renderer) {
		this.renderer = renderer;
		IModelPlayer[] ownedModels = getModels();
		modelBipedMain = ownedModels[0].getMovingModel();

		modelBipedMain.scaleArmType = Scale;
		modelBipedMain.scaleLegType = Scale;
		for (int i = 1; i < ownedModels.length; i++) {
			SMModel armor = ownedModels[i].getMovingModel();
			armor.scaleArmType = NoScaleStart;
			armor.scaleLegType = Scale;
		}
	}

	private IModelPlayer[] getModels() {
		if (models == null) {
			java.util.List<IModelPlayer> owned = new java.util.ArrayList<IModelPlayer>();
			owned.add(modelFor(renderer.getMainModel()));
			for (Object layer : ((RenderLivingBaseAccessor) renderer).smartMoving$getLayerRenderers()) {
				if (layer instanceof LayerArmorBase) {
					LayerArmorBaseAccessor armor = (LayerArmorBaseAccessor) layer;
					addModel(owned, (ModelBiped) armor.smartMoving$getModelArmor());
					addModel(owned, (ModelBiped) armor.smartMoving$getModelLeggings());
				}
			}
			models = owned.toArray(new IModelPlayer[owned.size()]);
		}
		return models;
	}

	private static void addModel(java.util.List<IModelPlayer> owned, ModelBiped model) {
		if (model != null) {
			IModelPlayer moving = modelFor(model);
			if (moving != null && !owned.contains(moving))
				owned.add(moving);
		}
	}

	private static IModelPlayer modelFor(ModelBiped model) {
		return (IModelPlayer) model;
	}

	public void beforeDoRender(AbstractClientPlayer entityplayer, double d, double d1, double d2, float f,
			float renderPartialTicks) {
		currentPlayer = entityplayer;
		currentYaw = f;
		currentPartialTicks = renderPartialTicks;
		IModelPlayer[] modelPlayers = getModels();
		boolean isInventory = d == 0.0D && d1 == 0.0D && d2 == 0.0D && f == 0.0F
				&& renderPartialTicks == 1.0F;
		updateMotionModels(entityplayer, renderPartialTicks, isInventory, modelPlayers);
		SMBase moving = SMFactory.getInstance(entityplayer);
		if (moving != null) {

			boolean isClimb = moving.isClimbing && !moving.isCrawling && !moving.isCrawlClimbing
					&& !moving.isClimbJumping;
			boolean isClimbJump = moving.isClimbJumping;
			int handsClimbType = moving.actualHandsClimbType;
			int feetClimbType = moving.actualFeetClimbType;
			boolean isHandsVineClimbing = moving.isHandsVineClimbing;
			boolean isFeetVineClimbing = moving.isFeetVineClimbing;
			boolean isCeilingClimb = moving.isCeilingClimbing;
			boolean isSwim = moving.isSwimming && !moving.isDipping;
			boolean isDive = moving.isDiving;
			boolean isLevitate = moving.isLevitating;
			boolean isCrawl = moving.isCrawling && !moving.isClimbing;
			boolean isCrawlClimb = moving.isCrawlClimbing || (moving.isClimbing && moving.isCrawling);
			boolean isJump = moving.isJumping();
			boolean isHeadJump = moving.isHeadJumping;
			boolean isFlying = moving.doFlyingAnimation();
			boolean isSlide = moving.isSliding;
			boolean isFalling = moving.doFallingAnimation();
			boolean isGenericSneaking = moving.isSlow;
			boolean isAngleJumping = moving.isAngleJumping();
			int angleJumpType = moving.angleJumpType;
			boolean isRopeSliding = moving.isRopeSliding;

			MovingStatistics statistics = ((ISmartMovingRenderState) entityplayer).smartMoving$getStatistics();
			float currentHorizontalSpeedFlattened = statistics.getCurrentHorizontalSpeedFlattened(renderPartialTicks);
			float smallOverGroundHeight = isCrawlClimb || isHeadJump ? (float) moving.getOverGroundHeight(5D) : 0F;
			Block overGroundBlock = isHeadJump && smallOverGroundHeight < 5F
					? moving.getOverGroundBlockId(smallOverGroundHeight)
					: null;

			for (int i = 0; i < modelPlayers.length; i++) {
				SMModel modelPlayer = modelPlayers[i].getMovingModel();
				modelPlayer.isClimb = isClimb;
				modelPlayer.isClimbJump = isClimbJump;
				modelPlayer.handsClimbType = handsClimbType;
				modelPlayer.feetClimbType = feetClimbType;
				modelPlayer.isHandsVineClimbing = isHandsVineClimbing;
				modelPlayer.isFeetVineClimbing = isFeetVineClimbing;
				modelPlayer.isCeilingClimb = isCeilingClimb;
				modelPlayer.isSwim = isSwim;
				modelPlayer.isDive = isDive;
				modelPlayer.isCrawl = isCrawl;
				modelPlayer.isCrawlClimb = isCrawlClimb;
				modelPlayer.isJump = isJump;
				modelPlayer.isHeadJump = isHeadJump;
				modelPlayer.isSlide = isSlide;
				modelPlayer.isFlying = isFlying;
				modelPlayer.isLevitate = isLevitate;
				modelPlayer.isFalling = isFalling;
				modelPlayer.isGenericSneaking = isGenericSneaking;
				modelPlayer.isAngleJumping = isAngleJumping;
				modelPlayer.angleJumpType = angleJumpType;
				modelPlayer.isRopeSliding = isRopeSliding;

				modelPlayer.currentHorizontalSpeedFlattened = currentHorizontalSpeedFlattened;
				modelPlayer.smallOverGroundHeight = smallOverGroundHeight;
				modelPlayer.overGroundBlock = overGroundBlock;
			}

		} else {
			for (IModelPlayer model : modelPlayers)
				model.getMovingModel().clearMovementFlags();
		}
	}

	private void updateMotionModels(AbstractClientPlayer player, float partialTicks, boolean inventory,
			IModelPlayer[] models) {
		MovingStatistics stats = ((ISmartMovingRenderState) player).smartMoving$getStatistics();
		double dx = 0, dy = 0, dz = 0;
		float cameraAngle = 0, verticalAngle = 0, horizontalAngle = 0;
		if (!inventory) {
			dx = player.posX - player.prevPosX;
			dy = player.posY - player.prevPosY;
			dz = player.posZ - player.prevPosZ;
			cameraAngle = player.rotationYaw / MovingAngles.RadiantToAngle;
			verticalAngle = (float) Math.atan(dy / Math.sqrt(dx * dx + dz * dz));
			if (Float.isNaN(verticalAngle)) verticalAngle = MovingAngles.Quarter;
			horizontalAngle = (float) -Math.atan(dx / dz);
			if (Float.isNaN(horizontalAngle))
				horizontalAngle = Float.isNaN(stats.prevHorizontalAngle)
						? cameraAngle : stats.prevHorizontalAngle;
			else if (dz < 0)
				horizontalAngle += MovingAngles.Half;
			stats.prevHorizontalAngle = horizontalAngle;
		}
		double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
		double verticalDistance = Math.abs(dy);
		double distance = Math.sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance);
		MovingRenderData previous = ((ISmartMovingRenderState) player).smartMoving$getRenderData();
		for (IModelPlayer model : models) {
			MovingModelCore core = model.getMovingModel().md;
			core.isInventory = inventory;
			core.isSleeping = player.isPlayerSleeping();
			core.totalVerticalDistance = stats.getTotalVerticalDistance(partialTicks);
			core.currentVerticalSpeed = stats.getCurrentVerticalSpeed(partialTicks);
			core.totalDistance = stats.getTotalDistance(partialTicks);
			core.currentSpeed = stats.getCurrentSpeed(partialTicks);
			core.distance = distance;
			core.verticalDistance = verticalDistance;
			core.horizontalDistance = horizontalDistance;
			core.currentCameraAngle = cameraAngle;
			core.currentVerticalAngle = verticalAngle;
			core.currentHorizontalAngle = horizontalAngle;
			core.prevOuterRenderData = previous;
		}
	}

	public double modifyRenderY(double y) {
		AbstractClientPlayer player = currentPlayer;
		if (player == null)
			return y;
		SMBase moving = SMFactory.getInstance(player);
		if (moving != null && !(player instanceof EntityPlayerSP) && moving.isCrawling
				&& !moving.isClimbing && player.isSneaking()
				&& !(y == 0.0D && currentYaw == 0.0F && currentPartialTicks == 1.0F))
			return y + 0.125D;
		return y;
	}

	public void afterDoRender(AbstractClientPlayer entityplayer) {
		currentPlayer = null;
		SMBase moving = SMFactory.getInstance(entityplayer);
		if (moving != null && moving.isLevitating)
			for (IModelPlayer modelPlayer : getModels())
				modelPlayer.getMovingModel().md.currentHorizontalAngle = modelPlayer.getMovingModel().md.currentCameraAngle;
	}

	public float beforeRotateCorpse(AbstractClientPlayer entityplayer, float totalTime, float actualRotation, float f2) {
		SMBase moving = SMFactory.getInstance(entityplayer);
		boolean inventory = f2 == 1.0F && entityplayer instanceof EntityPlayerSP
				&& Minecraft.getMinecraft().currentScreen instanceof GuiInventory;
		if (moving != null) {
			if (!inventory) {
				float forwardRotation = entityplayer.prevRotationYaw
						+ (entityplayer.rotationYaw - entityplayer.prevRotationYaw) * f2;
				if (moving.isClimbing || moving.isClimbCrawling || moving.isCrawlClimbing || moving.isFlying
						|| moving.isSwimming || moving.isDiving || moving.isCeilingClimbing || moving.isHeadJumping
						|| moving.isSliding || moving.isAngleJumping())
					entityplayer.renderYawOffset = forwardRotation;
			}
		}
		if (inventory || entityplayer.isElytraFlying())
			return actualRotation;
		float forwardRotation = entityplayer.prevRotationYaw
				+ (entityplayer.rotationYaw - entityplayer.prevRotationYaw) * f2;
		if (entityplayer.isPlayerSleeping()) {
			actualRotation = 0;
			forwardRotation = 0;
		}
		Minecraft minecraft = Minecraft.getMinecraft();
		float workingAngle;
		if (!(entityplayer instanceof EntityPlayerSP))
			workingAngle = -entityplayer.rotationYaw + minecraft.getRenderViewEntity().rotationYaw;
		else
			workingAngle = actualRotation - ((ISmartMovingRenderState) entityplayer)
					.smartMoving$getRenderData().rotateAngleY * MovingAngles.RadiantToAngle;
		if (minecraft.gameSettings.thirdPersonView == 2
				&& minecraft.getRenderViewEntity() instanceof net.minecraft.entity.player.EntityPlayer
				&& !((net.minecraft.entity.player.EntityPlayer) minecraft.getRenderViewEntity()).isPlayerSleeping())
			workingAngle += 180F;
		for (IModelPlayer model : getModels()) {
			MovingModelCore core = model.getMovingModel().md;
			core.actualRotation = actualRotation;
			core.forwardRotation = forwardRotation;
			core.workingAngle = workingAngle;
		}
		return 0;
	}

	public double modifyLivingY(AbstractClientPlayer entityplayer, double y) {
		if (entityplayer instanceof EntityOtherPlayerMP) {
			SMBase moving = SMFactory.getOtherSmartMoving(entityplayer.getEntityId());
			if (moving != null && moving.heightOffset != 0)
				return y + moving.heightOffset;
		}
		return y;
	}

	public boolean beforeRenderName(AbstractClientPlayer entityPlayer) {
		boolean originalIsSneaking = entityPlayer.isSneaking();
		if (Minecraft.isGuiEnabled() && entityPlayer != renderer.getRenderManager().pointedEntity) {
			SMBase moving = SMFactory.getInstance(entityPlayer);
			if (moving != null) {
				boolean temporaryIsSneaking = originalIsSneaking;
				if (moving.isCrawling && !moving.isClimbing)
					temporaryIsSneaking = !SMContext.Config._crawlNameTag.value;
				else if (originalIsSneaking)
					temporaryIsSneaking = !SMContext.Config._sneakNameTag.value;
				if (temporaryIsSneaking != originalIsSneaking)
					entityPlayer.setSneaking(temporaryIsSneaking);
			}
		}
		return originalIsSneaking;
	}

	public double modifyNameY(AbstractClientPlayer entityPlayer, double y, boolean originalIsSneaking) {
		if (Minecraft.isGuiEnabled() && entityPlayer != renderer.getRenderManager().pointedEntity) {
			SMBase moving = SMFactory.getInstance(entityPlayer);
			if (moving != null) {
				if (moving.heightOffset == -1)
					return y - 0.2F;
				if (originalIsSneaking && !entityPlayer.isSneaking())
					return y - 0.05F;
			}
		}
		return y;
	}

	public void afterRenderName(AbstractClientPlayer player, boolean originalIsSneaking) {
		if (player.isSneaking() != originalIsSneaking)
			player.setSneaking(originalIsSneaking);
	}

	public void beforeLayers(AbstractClientPlayer player, float partialTicks) {
		if (modelBipedMain.md.bipedEars != null)
			modelBipedMain.md.bipedEars.beforeRender(player);
		if (modelBipedMain.md.bipedCloak != null)
			modelBipedMain.md.bipedCloak.beforeRender(player, partialTicks);
	}

	public void afterLayers() {
		if (modelBipedMain.md.bipedCloak != null)
			modelBipedMain.md.bipedCloak.afterRender();
		if (modelBipedMain.md.bipedEars != null)
			modelBipedMain.md.bipedEars.afterRender();
	}

	public static void renderGuiIngame(Minecraft minecraft) {
		if (!SMContext.Client.getNativeUserInterfaceDrawing())
			return;

		if (!GL11.glGetBoolean(GL11.GL_ALPHA_TEST))
			return;

		SMSelf moving = (SMSelf) SMFactory.getInstance(minecraft.player);
		if (moving != null && SMContext.Config.enabled
				&& (SMContext.Options._displayExhaustionBar.value || SMContext.Options._displayJumpChargeBar.value)) {
			ScaledResolution scaledresolution = new ScaledResolution(minecraft);
			int width = scaledresolution.getScaledWidth();
			int height = scaledresolution.getScaledHeight();

			if (minecraft.playerController.shouldDrawHUD()) {
				float maxExhaustion = SMContext.Client.getMaximumExhaustion();
				float exhaustion = Math.min(moving.exhaustion, maxExhaustion);
				boolean drawExhaustion = exhaustion > 0 && exhaustion <= maxExhaustion;

				float maxStillJumpCharge = SMContext.Config._jumpChargeMaximum.value;
				float stillJumpCharge = Math.min(moving.jumpCharge, maxStillJumpCharge);

				float maxRunJumpCharge = SMContext.Config._headJumpChargeMaximum.value;
				float runJumpCharge = Math.min(moving.headJumpCharge, maxRunJumpCharge);

				boolean drawJumpCharge = stillJumpCharge > 0 || runJumpCharge > 0;
				float maxJumpCharge = stillJumpCharge > runJumpCharge ? maxStillJumpCharge : maxRunJumpCharge;
				float jumpCharge = Math.max(stillJumpCharge, runJumpCharge);

				if (drawExhaustion || drawJumpCharge) {
					GL11.glPushAttrib(GL11.GL_TEXTURE_BIT);
					minecraft.getTextureManager().bindTexture(new ResourceLocation("smartmoving", "gui/icons.png"));
					GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
					_minecraft = minecraft;
				}

				if (drawExhaustion) {
					float maxExhaustionForAction = Math.min(moving.maxExhaustionForAction, maxExhaustion);
					float maxExhaustionToStartAction = Math.min(moving.maxExhaustionToStartAction, maxExhaustion);

					float fitness = maxExhaustion - exhaustion;
					float minFitnessForAction = Float.isNaN(maxExhaustionForAction) ? 0
							: maxExhaustion - maxExhaustionForAction;
					float minFitnessToStartAction = Float.isNaN(maxExhaustionToStartAction) ? 0
							: maxExhaustion - maxExhaustionToStartAction;

					float maxFitnessDrawn = Math.max(Math.max(minFitnessToStartAction, fitness), minFitnessForAction);

					int halfs = (int) Math.floor(maxFitnessDrawn / maxExhaustion * 21F);
					int fulls = halfs / 2;
					int half = halfs % 2;

					int fitnessHalfs = (int) Math.floor(fitness / maxExhaustion * 21F);
					int fitnessFulls = fitnessHalfs / 2;
					int fitnessHalf = fitnessHalfs % 2;

					int minFitnessForActionHalfs = (int) Math.floor(minFitnessForAction / maxExhaustion * 21F);
					int minFitnessForActionFulls = minFitnessForActionHalfs / 2;
					int minFitnessForActionHalf = minFitnessForActionHalfs % 2;

					int minFitnessToStartActionHalfs = (int) Math.floor(minFitnessToStartAction / maxExhaustion * 21F);
					int minFitnessToStartActionFulls = minFitnessToStartActionHalfs / 2;

					_jOffset = height - 39 - 10 - (minecraft.player.isInsideOfMaterial(Material.WATER) ? 10 : 0);
					for (int i = 0; i < Math.min(fulls + half, 10); i++) {
						_iOffset = (width / 2 + 90) - (i + 1) * 8;
						if (i < fitnessFulls) {
							if (i < minFitnessForActionFulls)
								drawIcon(2, 2);
							else if (i == minFitnessForActionFulls && minFitnessForActionHalf > 0)
								drawIcon(3, 2);
							else
								drawIcon(0, 0);
						} else if (i == fitnessFulls && fitnessHalf > 0) {
							if (i < minFitnessForActionFulls)
								drawIcon(1, 2);
							else if (i == minFitnessForActionFulls && minFitnessForActionHalf > 0)
								if (i < minFitnessToStartActionFulls)
									drawIcon(3, 1);
								else
									drawIcon(4, 2);
							else if (i < minFitnessToStartActionFulls)
								drawIcon(1, 1);
							else
								drawIcon(1, 0);
						} else {
							if (i < minFitnessForActionFulls)
								drawIcon(0, 2);
							else if (i == minFitnessForActionFulls && minFitnessForActionHalf > 0)
								if (i < minFitnessToStartActionFulls)
									drawIcon(2, 1);
								else
									drawIcon(5, 2);
							else if (i < minFitnessToStartActionFulls)
								drawIcon(0, 1);
							else
								drawIcon(4, 1);
						}
					}
				}

				if (drawJumpCharge) {
					boolean max = jumpCharge == maxJumpCharge;
					int fulls = max ? 10 : (int) Math.ceil(((jumpCharge - 2) * 10D) / maxJumpCharge);
					int half = max ? 0 : (int) Math.ceil((jumpCharge * 10D) / maxJumpCharge) - fulls;

					_jOffset = height - 39 - 10 - (minecraft.player.getTotalArmorValue() > 0 ? 10 : 0);
					for (int i = 0; i < fulls + half; i++) {
						_iOffset = (width / 2 - 91) + i * 8;
						drawIcon(i < fulls ? 2 : 3, 0);
					}
				}

				if (drawExhaustion || drawJumpCharge)
					GL11.glPopAttrib();
			}
		}
	}

	private static void drawIcon(int x, int y) {
		_minecraft.ingameGUI.drawTexturedModalRect(_iOffset, _jOffset, x * 9, y * 9, 9, 9);
	}
}
