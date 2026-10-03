// ==================================================================
// This file is part of Smart Render.
//
// Smart Render is free software: you can redistribute it and/or
// modify it under the terms of the GNU General Public License as
// published by the Free Software Foundation, either version 3 of the
// License, or (at your option) any later version.
//
// Smart Render is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Smart Render. If not, see <http://www.gnu.org/licenses/>.
// ==================================================================

package net.smart.moving.model;

import java.util.List;
import java.util.Random;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.model.ModelBiped.ArmPose;
import net.minecraft.client.model.ModelBox;
import net.minecraft.client.model.ModelPlayer;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.MathHelper;
import net.smart.moving.util.MovingAngles;
import net.smart.moving.render.MovingRenderData;
import net.smart.moving.mixin.client.ModelPlayerAccessor;

public class MovingModelCore {
	private final SMModel animator;
	public ModelBiped mp;

	private boolean isModelPlayer;
	private boolean smallArms;

	public boolean isInventory;

	public float totalVerticalDistance;
	public float currentVerticalSpeed;
	public float totalDistance;
	public float currentSpeed;

	public double distance;
	public double verticalDistance;
	public double horizontalDistance;
	public float currentCameraAngle;
	public float currentVerticalAngle;
	public float currentHorizontalAngle;

	public float actualRotation;
	public float forwardRotation;
	public float workingAngle;

	public MovingRotationRenderer bipedOuter;
	public MovingRotationRenderer bipedTorso;
	public MovingRotationRenderer bipedBody;
	public MovingRotationRenderer bipedBreast;
	public MovingRotationRenderer bipedNeck;
	public MovingRotationRenderer bipedHead;
	public MovingRotationRenderer bipedRightShoulder;
	public MovingRotationRenderer bipedRightArm;
	public MovingRotationRenderer bipedLeftShoulder;
	public MovingRotationRenderer bipedLeftArm;
	public MovingRotationRenderer bipedPelvic;
	public MovingRotationRenderer bipedRightLeg;
	public MovingRotationRenderer bipedLeftLeg;

	public MovingRotationRenderer bipedBodywear;
	public MovingRotationRenderer bipedHeadwear;
	public MovingRotationRenderer bipedRightArmwear;
	public MovingRotationRenderer bipedLeftArmwear;
	public MovingRotationRenderer bipedRightLegwear;
	public MovingRotationRenderer bipedLeftLegwear;

	public MovingEarsRenderer bipedEars;
	public MovingCapeRenderer bipedCloak;

	public MovingRenderData prevOuterRenderData;
	public boolean isSleeping;
	private boolean anglesPrepared;
	private float preparedTime;
	private Entity preparedEntity;

	public MovingModelCore(ModelBiped mb, SMModel animator) {
		this.animator = animator;
		this.mp = mb;
		isModelPlayer = mb instanceof ModelPlayer;
		smallArms = isModelPlayer && ((ModelPlayerAccessor) mb).smartMoving$isSmallArms();

		ModelRenderer body = mb.bipedBody, head = mb.bipedHead, headwear = mb.bipedHeadwear;
		ModelRenderer rightArm = mb.bipedRightArm, leftArm = mb.bipedLeftArm;
		ModelRenderer rightLeg = mb.bipedRightLeg, leftLeg = mb.bipedLeftLeg;
		ModelRenderer bodywear = null, rightArmwear = null, leftArmwear = null;
		ModelRenderer rightLegwear = null, leftLegwear = null, cape = null, ears = null;
		if (isModelPlayer) {
			ModelPlayer player = (ModelPlayer) mb;
			bodywear = player.bipedBodyWear;
			rightArmwear = player.bipedRightArmwear;
			leftArmwear = player.bipedLeftArmwear;
			rightLegwear = player.bipedRightLegwear;
			leftLegwear = player.bipedLeftLegwear;
			cape = ((ModelPlayerAccessor) player).smartMoving$getCape();
			ears = ((ModelPlayerAccessor) player).smartMoving$getDeadmau5Head();
		}
		mb.boxList.clear();
		bipedOuter = create(null);
		bipedOuter.fadeEnabled = true;
		bipedTorso = create(bipedOuter);
		bipedBody = create(bipedTorso, body);
		bipedBreast = create(bipedTorso);
		bipedNeck = create(bipedBreast);
		bipedHead = create(bipedNeck, head);
		bipedRightShoulder = create(bipedBreast);
		bipedRightArm = create(bipedRightShoulder, rightArm);
		bipedLeftShoulder = create(bipedBreast);
		bipedLeftShoulder.mirror = true;
		bipedLeftArm = create(bipedLeftShoulder, leftArm);
		bipedPelvic = create(bipedTorso);
		bipedRightLeg = create(bipedPelvic, rightLeg);
		bipedLeftLeg = create(bipedPelvic, leftLeg);
		bipedBodywear = create(bipedBody, bodywear);
		bipedHeadwear = create(bipedHead, headwear);
		bipedRightArmwear = create(bipedRightArm, rightArmwear);
		bipedLeftArmwear = create(bipedLeftArm, leftArmwear);
		bipedRightLegwear = create(bipedRightLeg, rightLegwear);
		bipedLeftLegwear = create(bipedLeftLeg, leftLegwear);
		if (cape != null) {
			bipedCloak = new MovingCapeRenderer(mb, 0, 0, bipedBreast, bipedOuter);
			copy(bipedCloak, cape);
		}
		if (ears != null) {
			bipedEars = new MovingEarsRenderer(mb, 24, 0, bipedHead);
			copy(bipedEars, ears);
		}
		reset();
		mb.bipedBody = bipedBody;
		mb.bipedHead = bipedHead;
		mb.bipedHeadwear = bipedHeadwear;
		mb.bipedRightArm = bipedRightArm;
		mb.bipedLeftArm = bipedLeftArm;
		mb.bipedRightLeg = bipedRightLeg;
		mb.bipedLeftLeg = bipedLeftLeg;
		if (isModelPlayer) {
			ModelPlayer player = (ModelPlayer) mb;
			player.bipedBodyWear = bipedBodywear;
			player.bipedRightArmwear = bipedRightArmwear;
			player.bipedLeftArmwear = bipedLeftArmwear;
			player.bipedRightLegwear = bipedRightLegwear;
			player.bipedLeftLegwear = bipedLeftLegwear;
			ModelPlayerAccessor accessor = (ModelPlayerAccessor) player;
			accessor.smartMoving$setCape(bipedCloak);
			accessor.smartMoving$setDeadmau5Head(bipedEars);
		}
	}

	private MovingRotationRenderer create(MovingRotationRenderer base) {
		return new MovingRotationRenderer(mp, -1, -1, base);
	}

	private MovingRotationRenderer create(MovingRotationRenderer base, ModelRenderer original) {
		if (original == null)
			return null;

		MovingRotationRenderer local = new MovingRotationRenderer(mp, 0, 0, base);
		copy(local, original);
		return local;
	}

	private static void copy(MovingRotationRenderer local, ModelRenderer original) {
		if (original.childModels != null)
			for (Object childModel : original.childModels)
				local.addChild((ModelRenderer) childModel);
		if (original.cubeList != null)
			for (Object cube : original.cubeList)
				local.cubeList.add((ModelBox) cube);
		local.mirror = original.mirror;
		local.isHidden = original.isHidden;
		local.showModel = original.showModel;
	}

	public void render(Entity entity, float totalHorizontalDistance, float currentHorizontalSpeed,
			float totalTime, float viewHorizontalAngle, float viewVerticalAngle, float factor) {
		// Vanilla ModelBiped.render calls setRotationAngles before drawing. The model
		// mixin replaces that render path, so prepare angles here when the armor
		// layer has not already done it for this frame.
		if (!anglesPrepared || preparedEntity != entity || preparedTime != totalTime)
			setRotationAngles(totalHorizontalDistance, currentHorizontalSpeed, totalTime,
					viewHorizontalAngle, viewVerticalAngle, factor, entity);
		anglesPrepared = false;
		GL11.glPushMatrix();
		if (entity.isSneaking())
			GL11.glTranslatef(0.0F, 0.2F, 0.0F);
		bipedOuter.render(factor);
		bipedOuter.renderIgnoreBase(factor);
		bipedTorso.renderIgnoreBase(factor);
		bipedBody.renderIgnoreBase(factor);
		bipedBreast.renderIgnoreBase(factor);
		bipedNeck.renderIgnoreBase(factor);
		bipedHead.renderIgnoreBase(factor);
		bipedRightShoulder.renderIgnoreBase(factor);
		bipedRightArm.renderIgnoreBase(factor);
		bipedLeftShoulder.renderIgnoreBase(factor);
		bipedLeftArm.renderIgnoreBase(factor);
		bipedPelvic.renderIgnoreBase(factor);
		bipedRightLeg.renderIgnoreBase(factor);
		bipedLeftLeg.renderIgnoreBase(factor);
		if (isModelPlayer) {
			bipedBodywear.renderIgnoreBase(factor);
			bipedHeadwear.renderIgnoreBase(factor);
			bipedRightArmwear.renderIgnoreBase(factor);
			bipedLeftArmwear.renderIgnoreBase(factor);
			bipedRightLegwear.renderIgnoreBase(factor);
			bipedLeftLegwear.renderIgnoreBase(factor);
		}
		GL11.glPopMatrix();
	}

	public void setRotationAngles(float totalHorizontalDistance, float currentHorizontalSpeed, float totalTime,
			float viewHorizontalAngelOffset, float viewVerticalAngelOffset, float factor, Entity entity) {
		anglesPrepared = true;
		preparedEntity = entity;
		preparedTime = totalTime;
		reset();

		if (isSleeping) {
			prevOuterRenderData.rotateAngleX = 0;
			prevOuterRenderData.rotateAngleY = 0;
			prevOuterRenderData.rotateAngleZ = 0;
		}

		bipedOuter.previous = prevOuterRenderData;

		bipedOuter.rotateAngleY = actualRotation / MovingAngles.RadiantToAngle;
		bipedOuter.fadeRotateAngleY = !entity.isRiding();

		animator.animateHeadRotation(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
				viewVerticalAngelOffset, factor);

		if (isSleeping)
			animator.animateSleeping(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
					viewVerticalAngelOffset, factor);

		animator.animateArmSwinging(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
				viewVerticalAngelOffset, factor);

		if (mp.isRiding)
			animator.animateRiding(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
					viewVerticalAngelOffset, factor);

		if (mp.leftArmPose != ArmPose.EMPTY)
			animator.animateLeftArmItemHolding(totalHorizontalDistance, currentHorizontalSpeed, totalTime,
					viewHorizontalAngelOffset, viewVerticalAngelOffset, factor);

		if (mp.rightArmPose != ArmPose.EMPTY)
			animator.animateRightArmItemHolding(totalHorizontalDistance, currentHorizontalSpeed, totalTime,
					viewHorizontalAngelOffset, viewVerticalAngelOffset, factor);

		if (mp.swingProgress > -9990F) {
			animator.animateWorkingBody(totalHorizontalDistance, currentHorizontalSpeed, totalTime,
					viewHorizontalAngelOffset, viewVerticalAngelOffset, factor);
			animator.animateWorkingArms(totalHorizontalDistance, currentHorizontalSpeed, totalTime,
					viewHorizontalAngelOffset, viewVerticalAngelOffset, factor);
		}

		if (mp.isSneak)
			animator.animateSneaking(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
					viewVerticalAngelOffset, factor);

		animator.animateArms(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
				viewVerticalAngelOffset, factor);

		if (mp.rightArmPose == ArmPose.BOW_AND_ARROW || mp.leftArmPose == ArmPose.BOW_AND_ARROW)
			animator.animateBowAiming(totalHorizontalDistance, currentHorizontalSpeed, totalTime, viewHorizontalAngelOffset,
					viewVerticalAngelOffset, factor);

		if (bipedOuter.previous != null && !bipedOuter.fadeRotateAngleX)
			bipedOuter.previous.rotateAngleX = bipedOuter.rotateAngleX;

		if (bipedOuter.previous != null && !bipedOuter.fadeRotateAngleY)
			bipedOuter.previous.rotateAngleY = bipedOuter.rotateAngleY;

		bipedOuter.fadeIntermediate(totalTime);
		bipedOuter.fadeStore(totalTime);

		if (isModelPlayer) {
			bipedCloak.ignoreBase = false;
			bipedCloak.rotateAngleX = MovingAngles.Sixtyfourth;
		}
	}

	public void animateHeadRotation(float viewHorizontalAngelOffset, float viewVerticalAngelOffset) {
		bipedNeck.ignoreBase = true;
		bipedHead.rotateAngleY = (actualRotation + viewHorizontalAngelOffset) / MovingAngles.RadiantToAngle;
		bipedHead.rotateAngleX = viewVerticalAngelOffset / MovingAngles.RadiantToAngle;
	}

	public void animateSleeping() {
		bipedNeck.ignoreBase = false;
		bipedHead.rotateAngleY = 0F;
		bipedHead.rotateAngleX = MovingAngles.Eighth;
		bipedTorso.rotationPointZ = -17F;
	}

	public void animateArmSwinging(float totalHorizontalDistance, float currentHorizontalSpeed) {
		bipedRightArm.rotateAngleX = MathHelper.cos(totalHorizontalDistance * 0.6662F + MovingAngles.Half) * 2.0F
				* currentHorizontalSpeed * 0.5F;
		bipedLeftArm.rotateAngleX = MathHelper.cos(totalHorizontalDistance * 0.6662F) * 2.0F * currentHorizontalSpeed
				* 0.5F;

		bipedRightLeg.rotateAngleX = MathHelper.cos(totalHorizontalDistance * 0.6662F) * 1.4F * currentHorizontalSpeed;
		bipedLeftLeg.rotateAngleX = MathHelper.cos(totalHorizontalDistance * 0.6662F + MovingAngles.Half) * 1.4F
				* currentHorizontalSpeed;
	}

	public void animateRiding() {
		bipedRightArm.rotateAngleX += -0.6283185F;
		bipedLeftArm.rotateAngleX += -0.6283185F;
		bipedRightLeg.rotateAngleX = -1.256637F;
		bipedLeftLeg.rotateAngleX = -1.256637F;
		bipedRightLeg.rotateAngleY = 0.3141593F;
		bipedLeftLeg.rotateAngleY = -0.3141593F;
	}

	public void animateLeftArmItemHolding() {
		bipedLeftArm.rotateAngleX = bipedLeftArm.rotateAngleX * 0.5F - 0.3141593F;
	}

	public void animateRightArmItemHolding() {
		bipedRightArm.rotateAngleX = bipedRightArm.rotateAngleX * 0.5F - 0.3141593F;
	}

	public void animateWorkingBody() {
		float angle = MathHelper.sin(MathHelper.sqrt(mp.swingProgress) * MovingAngles.Whole) * 0.2F;
		bipedBreast.rotateAngleY = bipedBody.rotateAngleY += angle;
		bipedBreast.rotationOrder = bipedBody.rotationOrder = MovingRotationRenderer.YXZ;
		bipedLeftArm.rotateAngleX += angle;
	}

	public void animateWorkingArms() {
		float f6 = 1.0F - mp.swingProgress;
		f6 = 1.0F - f6 * f6 * f6;
		float f7 = MathHelper.sin(f6 * MovingAngles.Half);
		float f8 = MathHelper.sin(mp.swingProgress * MovingAngles.Half) * -(bipedHead.rotateAngleX - 0.7F) * 0.75F;
		bipedRightArm.rotateAngleX -= f7 * 1.2D + f8;
		bipedRightArm.rotateAngleY += MathHelper.sin(MathHelper.sqrt(mp.swingProgress) * MovingAngles.Whole) * 0.4F;
		bipedRightArm.rotateAngleZ -= MathHelper.sin(mp.swingProgress * MovingAngles.Half) * 0.4F;
	}

	public void animateSneaking() {
		bipedTorso.rotateAngleX += 0.5F;
		bipedRightLeg.rotateAngleX += -0.5F;
		bipedLeftLeg.rotateAngleX += -0.5F;
		bipedRightArm.rotateAngleX += -0.1F;
		bipedLeftArm.rotateAngleX += -0.1F;

		bipedPelvic.offsetY = -0.13652F;
		bipedPelvic.offsetZ = -0.05652F;

		bipedBreast.offsetY = -0.01872F;
		bipedBreast.offsetZ = -0.07502F;

		bipedNeck.offsetY = 0.0621F;
	}

	public void animateArms(float totalTime) {
		bipedRightArm.rotateAngleZ += MathHelper.cos(totalTime * 0.09F) * 0.05F + 0.05F;
		bipedLeftArm.rotateAngleZ -= MathHelper.cos(totalTime * 0.09F) * 0.05F + 0.05F;
		bipedRightArm.rotateAngleX += MathHelper.sin(totalTime * 0.067F) * 0.05F;
		bipedLeftArm.rotateAngleX -= MathHelper.sin(totalTime * 0.067F) * 0.05F;
	}

	public void animateBowAiming(float totalTime) {
		bipedRightArm.rotateAngleZ = 0.0F;
		bipedLeftArm.rotateAngleZ = 0.0F;
		bipedRightArm.rotateAngleY = -0.1F + bipedHead.rotateAngleY - bipedOuter.rotateAngleY;
		bipedLeftArm.rotateAngleY = 0.1F + bipedHead.rotateAngleY + 0.4F - bipedOuter.rotateAngleY;
		bipedRightArm.rotateAngleX = -1.570796F + bipedHead.rotateAngleX;
		bipedLeftArm.rotateAngleX = -1.570796F + bipedHead.rotateAngleX;
		bipedRightArm.rotateAngleZ += MathHelper.cos(totalTime * 0.09F) * 0.05F + 0.05F;
		bipedLeftArm.rotateAngleZ -= MathHelper.cos(totalTime * 0.09F) * 0.05F + 0.05F;
		bipedRightArm.rotateAngleX += MathHelper.sin(totalTime * 0.067F) * 0.05F;
		bipedLeftArm.rotateAngleX -= MathHelper.sin(totalTime * 0.067F) * 0.05F;
	}

	public void reset() {
		bipedOuter.reset();
		bipedTorso.reset();
		bipedBody.reset();
		bipedBreast.reset();
		bipedNeck.reset();
		bipedHead.reset();
		bipedRightShoulder.reset();
		bipedRightArm.reset();
		bipedLeftShoulder.reset();
		bipedLeftArm.reset();
		bipedPelvic.reset();
		bipedRightLeg.reset();
		bipedLeftLeg.reset();

		if (isModelPlayer) {
			bipedBodywear.reset();
			bipedHeadwear.reset();
			bipedRightArmwear.reset();
			bipedLeftArmwear.reset();
			bipedRightLegwear.reset();
			bipedLeftLegwear.reset();
			bipedEars.reset();
			bipedCloak.reset();
		}

		/*
		 * Rotation of extremities is on joints to torso; Also accounts for small arms
		 * setting
		 */
		bipedRightShoulder.setRotationPoint(-5F, isModelPlayer && smallArms ? 2.5F : 2.0F, 0.0F);
		bipedLeftShoulder.setRotationPoint(5F, isModelPlayer && smallArms ? 2.5F : 2.0F, 0.0F);
		bipedPelvic.setRotationPoint(0.0F, 12.0F, 0.1F);
		bipedRightLeg.setRotationPoint(-1.9F, 0.0F, 0.0F);
		bipedLeftLeg.setRotationPoint(1.9F, 0.0F, 0.0F);

		if (isModelPlayer)
			bipedCloak.setRotationPoint(0.0F, 0.0F, 2.0F);
	}

	public void renderCloak(float f) {
		if (bipedCloak != null) bipedCloak.render(f);
	}

	public ModelRenderer getRandomBox(Random par1Random) {
		List<?> boxList = mp.boxList;
		int size = boxList.size();
		int renderersWithBoxes = 0;

		for (int i = 0; i < size; i++) {
			ModelRenderer renderer = (ModelRenderer) boxList.get(i);
			if (canBeRandomBoxSource(renderer))
				renderersWithBoxes++;
		}

		if (renderersWithBoxes != 0) {
			int random = par1Random.nextInt(renderersWithBoxes);
			renderersWithBoxes = -1;

			for (int i = 0; i < size; i++) {
				ModelRenderer renderer = (ModelRenderer) boxList.get(i);
				if (canBeRandomBoxSource(renderer))
					renderersWithBoxes++;
				if (renderersWithBoxes == random)
					return renderer;
			}
		}

		return null;
	}

	private static boolean canBeRandomBoxSource(ModelRenderer renderer) {
		return renderer.cubeList != null && renderer.cubeList.size() > 0
				&& (!(renderer instanceof MovingRotationRenderer)
						|| ((MovingRotationRenderer) renderer).canBeRandomBoxSource());
	}
}
