# Dependency-removal migration

This is the source audit and implementation checklist for removing PlayerAPI,
RenderPlayerAPI, and SmartRender from Smart Moving 1.12.2. A row is complete only
when its replacement is wired, its old call sites are gone, and it has been
checked in both development and an obfuscated Forge launch.

| Smart Moving owner | External use and behavior | Kind | Replacement |
| --- | --- | --- | --- |
| `SMPlayerBase`, `SMFactory` | `ClientPlayerBase` callbacks and `IClientPlayerAPI.getClientPlayerBase`; the base constructs `SMSelf` | Hook; attached state | `EntityPlayerSP` Mixin implementing a Smart Moving player interface; controller stored on that player |
| `SMPlayerBase`, `SMSelf`, `IEntityPlayerSP` | `localMoveEntity`, `localTrySleep`, `localGetBrightness`, `localGetBrightnessForRender`, `localUpdateEntityActionState`, `localIsInsideOfMaterial`, `localWriteEntityToNBT`, `localIsSneaking`, `localGetFovModifier` | Vanilla fallback | Explicit per-operation vanilla paths. Avoid calling an intercepted method from its own controller without a bypass or invoker |
| `SMPlayerBase` | PlayerAPI getters/setters for `sleeping`, `isInWeb`, `isJumping`, and `mc` | Field accessor | Accessor Mixins on the actual declaring classes; ordinary direct access for public fields |
| `SMServerPlayerBase`, packet handlers | `ServerPlayerBase` callbacks and `IServerPlayerAPI.getServerPlayerBase`; the base constructs `SMServer` | Hook; attached state | `EntityPlayerMP` Mixin implementing a server player interface; retain the packet protocol |
| `SMServerPlayerBase`, `SMServer` | `localIsEntityInsideOpaqueBlock`, `localAddMovementStat`, `localAddExhaustion`, `localIsSneaking`, and connection field | Vanilla fallback; accessor | Explicit guarded vanilla paths and `NetHandlerPlayServer` accessor for `floatingTickCount` |
| `SMRenderPlayerBase`, `SMRender` | `RenderPlayerBase.doRender`, `rotateCorpse`, `renderLivingAt`, `renderName` | Renderer hooks | Inject around the declaring vanilla renderer methods, preserving the vanilla call and layers |
| `SMRenderPlayerBase` | Render manager, layer list, armor model reflection, `ModelPlayerAPI.getAllInstances()` | Renderer field access; model registry | Accessors and renderer-owned references to its main and armor models |
| `SMModelPlayerBase`, `SMModel` | `ModelPlayerBase`, `SRContext`, eleven `dynamic` animation keys | Model extension; dispatch | Model-owned controller with explicit animation methods in the same call order |
| `SMModel`, `SMModelBiped`, `SMModelPlayer` | `SRModel` and `SRModelRotationRenderer` | Model hierarchy | Internal model preserving outer, torso, breast, neck, shoulder, pelvis, limbs, armor/wear transforms, rotation order, and fade state |
| `SMRenderPlayer`, `SMRender`, `SMBase` | `SRRenderPlayer`, `SRRenderer.getPreviousRendererData` | Renderer extension; per-player state | Smart Moving-owned renderer state attached to the player |
| `SMRender`, `SMSelf`, `SMContext` | `SmartStatisticsFactory`, `SmartStatisticsContext`, local and remote statistics | Utility; hooks | Smart Moving-owned per-player speed/statistics sampler, updated at existing local and remote tick points |
| `SMBase`, `SMSelf`, `SMModel` | `SRUtilities` angles and constants | Utility | Small Smart Moving math utility |
| `SMMod`, Gradle, `mcmod.info` | Registration/sorting, `fml.coreMods.load`, subprojects, declared mod requirements | Registration; build | Mixin configuration and bootstrap; remove obsolete registration and dependency declarations after their call sites are migrated |
| `SMMod`, `SMInstall`, `SMOrientation`, `SMSelf`, `SMServerPlayerBase` | `net.smart.utilities.Name` and `Reflect` supplied by SmartRender | Utility | Smart Moving-owned reflection only for optional mod integration; accessors for Minecraft internals |

## Verified dispatch details

* `ClientPlayerBase.superXxx()` and `ServerPlayerBase.superXxx()` walk to the
  next registered base, then call `localXxx()` when no lower base exists. A
  normal call to the same intercepted player method can recurse.
* `ModelPlayerAPI.dynamic()` invokes the last registered override, then walks
  earlier overrides when a base calls `super.dynamic()`, and finally invokes
  SmartRender's virtual implementation. The eleven Smart Moving animation
  overrides must call the corresponding SmartRender behavior explicitly after
  their own changes.
* SmartRender creates `outer -> torso -> {body, breast -> {neck -> head,
  shoulders -> arms}, pelvis -> legs}`. Wear parts follow their corresponding
  parent. Armor models use the same hierarchy. `SRModelRotationRenderer` adds
  rotation order, scaling, parent transforms, and animation fades.
* SmartRender statistics are updated after local `travel` and ridden updates,
  and once per client tick for remote players. The current Smart Moving code
  uses flattened horizontal speed for animations and climbing decisions.
* SmartRender also supplies `Name` and `Reflect` classes to this project.
  Removing only `net.smart.render.*` imports would leave an undeclared runtime
  dependency.

## Proposed Minecraft targets

The actual method declaration controls the Mixin target. The 1.12.2 mapped
class inspection found `EntityPlayerSP.move`, `onUpdate`, `onLivingUpdate`,
`isSneaking`, `updateEntityActionState`, and `pushOutOfBlocks` declared on
`EntityPlayerSP`; server `onUpdate` and `trySleep` on `EntityPlayerMP`;
`addMovementStat`, `addExhaustion`, `getSleepTimer`, and player sleep on
`EntityPlayer`; and brightness, material, position, and base sneaking on
`Entity`. Renderer `doRender`, `applyRotations`, and `renderLivingAt` are
declared on `RenderPlayer`; `renderName` is inherited from `RenderLivingBase`.
`ModelPlayer` declares its own `render` and `setRotationAngles`.

The target signatures and SRG mappings still need validation against the exact
Forge 14.23.5.2854 development and production artifacts before applying each
Mixin. Inherited calls must be handled at their declaring class or a specific
call site; a Mixin targeting a subclass cannot inject into an inherited method.

## Phase gates

1. Add Mixin build/runtime setup and narrowly scoped accessors. Verify the
   existing mod still compiles and launches with the three dependencies.
2. Replace client and server PlayerAPI hooks and player state. Check movement,
   packet handling, disconnect/reconnect, and dedicated-server classloading.
3. Replace RenderPlayerAPI hooks. Check normal render flow, names, armor,
   held items, and other renderer layers.
4. Replace ModelPlayerAPI dynamic dispatch with explicit internal methods.
5. Rebuild the required extended model hierarchy, renderer state, and
   statistics within Smart Moving. Check all movement animations locally and
   for remote players.
6. Remove all three subprojects, old adapters, registrations, and metadata
   dependencies. Build from a fresh clone and run client, dedicated-server,
   join, and gameplay regression checks.

No launch or gameplay result should be inferred from a successful compilation.

## Related ports reviewed

These are design references, not drop-in replacements. Their Minecraft versions,
feature coverage, and interception choices differ from this project.

| Source | Observed implementation | Applicable lesson and limit |
| --- | --- | --- |
| [SmartMovingReloaded](https://github.com/Tommsy64/SmartMovingReloaded) | Forge 1.12.2 Mixins target `EntityPlayerSP`, `EntityPlayerMP`, `RenderPlayer`, and `ModelPlayer`. It attaches player state to mixed-in objects and has separate client/server code. Several callbacks are empty; `isSneaking` and player movement use `@Overwrite`, while renderer construction replaces armor and Elytra layers. | Useful confirmation of class and method locations and of client/server split. Its incomplete hooks and broad replacements cannot establish parity or be copied as the migration. Examine each injection against this project's current PlayerAPI dispatch before adopting it. |
| [View-Studio SmartMoving](https://github.com/View-Studio/SmartMoving) | Fabric 1.21.1 port separates common/server and client Mixins and explicitly handles server movement and network state. It uses a UUID-to-server-state map with join/disconnect cleanup; the modern pose and dimensions system differs from 1.12.2. | Useful checklist for server authority, state sync, and lifecycle. Keep player-attached state here unless a specific lifecycle or Mixin conflict justifies a map. Yarn targets, signatures, and pose logic do not transfer directly. |
| [SmartMovingReboot Elytra fork](https://github.com/LimerenceCantCode/SmartMovingReboot/commit/2a8a8ffed0def24be9186d419111cdb6d6e1dfdf) | Adds an `isElytraFlying()` branch to `SMSelf.moveEntityWithHeading` by copying vanilla flight movement. Its commit describes the fix as a workaround and says rendering remains incomplete. | Preserve the intended behavior without duplicating vanilla physics: when Elytra flight is active, keep the vanilla `travel` path and suppress only Smart Moving's conflicting movement interception. Validate the exact PlayerAPI call chain first. Handle player and Elytra rendering as a separate regression case. |
| [IPECTER SmartMoving](https://github.com/IPECTER/SmartMoving) | Paper plugin for Minecraft 1.14.x–1.19.x, centered on crawling and wall jumping. It creates player state on join and removes it on quit, using Bukkit events and newer swimming behavior. | Its player lifecycle is a useful server test case. Bukkit/Paper mechanics do not replace Forge 1.12.2 `SMServer`, packet handling, or client-side animation. |

The Elytra fork exposes an additional regression target: active Elytra flight must
remain vanilla-driven on both sides, and wings must still render correctly with
the extended body hierarchy. The Forge Mixin port's custom Elytra layer may be
informative, but it should only be used if the existing vanilla layer cannot
follow the Smart Moving model transforms after the model migration.

The PlayerAPI bases have been removed. `EntityPlayer.travel` now passes active
Elytra flight through vanilla and sends other local-player movement to the
existing `SMSelf` controller. The `EntityPlayerSP.move` before/after hooks also
skip Smart Moving bookkeeping during active flight. This addresses movement;
Elytra and player model rendering remain unverified.

`NetHandlerPlayServerAccessor` resets `floatingTickCount` without reflection.
Client accessors now read/write `sleeping`, `isInWeb`, `isJumping`, and
`EntityPlayerSP.mc` on their declaring Minecraft classes.

## PlayerAPI hook migration

`Compiled` means Java 8 `compileJava` and Mixin annotation processing passed
against Forge 14.23.5.2854 without any legacy API on the classpath. No row has
passed a game launch or behavior test.

| Old PlayerAPI behavior | New declaring target and interception | Vanilla fallback and call order | Compiled | Runtime |
| --- | --- | --- | --- | --- |
| `SMPlayerBase` owns `SMSelf`; base lookup in `SMFactory` | `EntityPlayerSP` constructor RETURN, `IEntityPlayerSP` on player | Controller exists on that player; no base registry | Yes | No |
| `beforeMoveEntity` / `afterMoveEntity` | `EntityPlayerSP.move` HEAD / RETURN | Vanilla `move` executes between callbacks; active Elytra skips Smart Moving callbacks | Yes | Basic movement user-verified; Elytra untested |
| Override `moveEntityWithHeading`; base `super` dispatches to lower base or vanilla | `EntityPlayer.travel` HEAD, cancellable for local player | `SMSelf` replaces travel except during active Elytra flight, when vanilla handles it; no reentrant call to `travel` | Yes | Walking/jumping/sprinting user-verified; Elytra untested |
| `beforeOnUpdate` / `afterOnUpdate`; `beforeOnLivingUpdate` / `afterOnLivingUpdate` | `EntityPlayerSP.onUpdate` and `onLivingUpdate` HEAD / RETURN | Vanilla executes once between callbacks | Yes | No |
| Override `updateEntityActionState`; `localUpdateEntityActionState` reaches SP vanilla | `EntityPlayerSP.updateEntityActionState` HEAD / RETURN | `SMSelf` prework, then original SP method, then `SMSelf` postwork. Sleep-specific update still runs controller pre/post without vanilla | Yes | No |
| `localIsSneaking` bypasses Smart Moving override to original SP method | `EntityPlayerSP.isSneaking` HEAD, cancellable, plus explicit fallback on attached interface | Fallback matches SP vanilla: `movementInput.sneak && !sleeping`, with null input check; superclass flag is not equivalent | Yes | Sneaking user-verified |
| Replace `pushOutOfBlocks`, `isOnLadder`, `canTriggerWalking`, `jump` | SP `pushOutOfBlocks`; `EntityLivingBase.isOnLadder`; `EntityPlayer.canTriggerWalking` and `jump`; HEAD, cancellable | Controller replaces each for local player; other entities retain vanilla | Yes | Jump/climb user-verified; other cases untested |
| `beforeTrySleep`, `beforeGetSleepTimer`, `beforeSetPositionAndRotation`, `writeEntityToNBT` wrapper | `EntityPlayer.trySleep` HEAD; `getSleepTimer` HEAD; `Entity.setPositionAndRotation` HEAD; `EntityPlayer.writeEntityToNBT` RETURN | Vanilla sleep, position, timer, and NBT methods remain; NBT ability correction happens after vanilla | Yes | No |
| Brightness wrappers temporarily shift `posY` | `Entity.getBrightness` / `getBrightnessForRender` HEAD / RETURN | Vanilla light calculation executes between shift and restore for local player | Yes | No |
| Finite-liquid material override, otherwise `localIsInsideOfMaterial` | `Entity.isInsideOfMaterial` HEAD, cancellable only for finite liquid water | Vanilla handles all other materials and players | Yes | No |
| FOV wrapper substitutes movement-speed attribute value during vanilla calculation | `AbstractClientPlayer.getFovModifier` redirect of `IAttributeInstance.getAttributeValue` | Vanilla FOV formula and bow/flight adjustments stay intact; only the speed input changes | Yes | No |
| `SMServerPlayerBase` owns `SMServer`; packet handlers use `IServerPlayerAPI` lookup | `EntityPlayerMP` constructor RETURN, `IEntityPlayerMP` on player | Packet handlers cast the packet's player to the attached interface | Yes | No |
| Server `beforeOnUpdate`, `afterOnUpdate`, `afterOnLivingUpdate` | MP `onUpdate` HEAD / RETURN; `EntityPlayer.onLivingUpdate` RETURN | Vanilla runs once; inherited living-update hook filters to MP | Yes | No |
| Server `afterSetPosition`, `beforeIsPlayerSleeping` | `Entity.setPosition` RETURN; `EntityPlayer.isPlayerSleeping` HEAD | Hooks filter to MP; position hook waits for controller construction | Yes | No |
| Server opaque-block, sneaking, eye-height overrides | `EntityPlayer.isEntityInsideOpaqueBlock` and `getEyeHeight`; `Entity.isSneaking`; conditional HEAD returns | Cooldown suppresses opaque-block check; item use forces sneaking; eye height uses current player height. Other cases keep vanilla | Yes | No |
| Server `addExhaustion` directly updates `FoodStats`; `addMovementStat` delegates | `EntityPlayer.addExhaustion` HEAD, cancellable for MP; no movement-stat hook | Preserves direct exhaustion behavior; vanilla movement statistics run unchanged | Yes | No |
| PlayerAPI internal field getters and NetHandler reflection | Accessor Mixins on `Entity`, `EntityLivingBase`, `EntityPlayer`, SP, NetHandler | No PlayerAPI field wrapper or reflection for these fields | Yes | No |

## Renderer and model migration

The old renderer bases used an override chain. `super` delegated to the next
registered base, then vanilla; the Smart Moving base ran before Smart Render
because of explicit sorting. The new path executes Smart Moving's state update
and the required Smart Render-derived transforms in explicit order within its
own renderer/model controllers. No generic base registry remains.

| Old API and hook | Old call-chain semantics | Minecraft declaration and Mixin | Smart Moving implementation and vanilla fallback | Compiled | Runtime |
| --- | --- | --- | --- | --- | --- |
| `SMRenderPlayerBase.doRender` | WRAPS vanilla; flags before render, levitation fix after; crawl changes Y | `RenderPlayer.doRender`, HEAD/RETURN and `@ModifyArg` of its `RenderLivingBase.doRender` Y argument | `SMRender.beforeDoRender` updates owned main/armor/leggings models; vanilla Forge Pre/Post and layers remain; `afterDoRender` stores levitation angle | Yes | Third-person body and tested crawl/climb/flying poses user-verified; armor untested |
| `rotateCorpse` through Smart Render | WRAPS vanilla; Smart Moving changes body yaw, Smart Render records rotation and supplies zero yaw to vanilla | `RenderPlayer.applyRotations`, `@ModifyVariable` of body yaw at HEAD | `SMRender.beforeRotateCorpse` records actual/forward/working angles; sleeping and inventory retain old conditions; active Elytra keeps vanilla angle | Yes | No |
| `renderLivingAt` | MODIFIES ARGUMENT Y for a remote player's height offset | `RenderPlayer.renderLivingAt`, `@ModifyVariable` Y argument | `SMRender.modifyLivingY`; vanilla sleeping offset and translation still run | Yes | No |
| `renderName` | WRAPS vanilla, temporarily changes sneaking and shifts label height | `RenderLivingBase.renderName`, HEAD/RETURN and `@ModifyArg` at `renderEntityName` | `SMRender` restores sneak state after vanilla; inherited Forge name event remains | Yes | No |
| Smart Render `renderSpecials` | WRAPS vanilla layer render with cape/ear preparation | `RenderLivingBase.renderLayers`, HEAD/RETURN | `SMRender.beforeLayers`/`afterLayers`; vanilla armor, held item and other layers still run | Yes | No |
| Smart Render `handleRotationFloat` | MODIFIES RESULT by ridden tick count | `RenderLivingBase.handleRotationFloat`, RETURN, cancellable | Adds per-player ridden ticks to vanilla result without changing `ticksExisted` | Yes | No |
| `ModelPlayerAPI.dynamic` animation names | Smart Moving override called the next Smart Render virtual animation | `ModelBiped` / `ModelPlayer` render and setRotationAngles, HEAD, cancellable for renderer-owned models | `SMModel` calls explicit `MovingModelCore.animate*` methods; vanilla remains for unrelated models, inventory and Elytra | Yes | Crawl/climb/flying user-verified; other animations untested |
| `SRContext` and `ModelPlayerAPI.getAllInstances()` | Registry lookup of model bases, including renderer and armor models | `ModelBiped` instance Mixin implements `IModelPlayer`; renderer obtains its own main/armor/leggings models | State is attached to each model; no global model registry | Yes | No |
| Smart Render transform renderer and model | Hierarchical body model with ordered Euler rotations, offset/scale, fade and parent transforms | `ModelRenderer` accessor/invoker for display list; `ModelPlayer` accessor for small arms, cape and ears | `MovingModelCore`, `MovingRotationRenderer`, cape/ear renderers and per-player `MovingRenderData` preserve the required hierarchy and interpolation | Yes | No |
| Smart Render statistics and `SRRenderer` state | Local samples after PlayerAPI's dispatched `moveEntityWithHeading`; riding samples after `updateRidden`; remote tick samples | Smart Moving's cancellable `EntityPlayer.travel` path samples immediately after `SMSelf.moveEntityWithHeading`; vanilla Elytra travel samples at `EntityLivingBase.travel` RETURN; ridden and remote hooks remain at RETURN | `MovingStatistics` and `ISmartMovingRenderState`; no global player map | Yes | Local crawl/climb/flying animation user-verified; remote/riding untested |

The hierarchy is `outer → torso → {body, breast → {neck → head,
shoulders → arms}, pelvic → legs}`. Wear parts parent to their corresponding
body part. Skin, armor, leggings, cape and ears each retain their own model
parts. The controller keeps rotation order, pivot, offset, scale, previous
render data and partial-tick calculations from the source that these
animations actually used. There is no `ModelPlayerAPI` dynamic string dispatch.

The model draw injection replaces vanilla drawing for the renderer-owned
models because vanilla `ModelBiped.render` cannot draw the added parent
hierarchy. It is an `@Inject` with conditional cancellation, not a method
overwrite. Runtime parity of armor, held items, capes and Elytra is still
unverified.

## Verification status

The standard root `compileJava` and `build` tasks pass under Java 8 with Forge
14.23.5.2854 and Mixin 0.8.5, without any PlayerAPI, RenderPlayerAPI or
SmartRender compile dependency. `settings.gradle` includes no subprojects;
the three Git submodule gitlinks and `.gitmodules` have been removed. The
normal build no longer runs PlayerAPI's `LayerHeldItem` binary patch task.

Before the version and branding update, `build` produced one
`SmartMoving-1.12.2-17.0-RC4.jar`. Inspection
found `mixins.smartmoving.json`, its refmap, the `MixinConfigs` manifest entry,
and embedded Mixin runtime classes. It found no `api/player/` or
`net/smart/render/` classes and no references to those packages in Smart
Moving class files. Mixin's five Java 16 ModLauncher classes are omitted from
the embedded runtime because Forge 1.12 uses LaunchWrapper and ForgeGradle's
Java 8 reobfuscator cannot parse them.

This confirms the build/dependency milestone. It does not confirm a fresh
clone with an empty Gradle cache. A Java 8 Forge development client completed
mod construction and Forge reported all five mods loaded. That run exposed an
invalid `@ModifyArg` handler signature in the new `RenderPlayer` Mixin, which
was corrected and verified by a second successful mod load. The development
cache has an unrelated modified Forge `Side` enum containing `BUKKIT`; Forge's
network registry initializes only `CLIENT` and `SERVER`, so startup otherwise
fails inside FML. The official Forge `Side.class` was used as a temporary
development-classpath override for the successful launch and removed before
the final build. The shipped JAR never contains that class.

The October 3 plain Forge client log confirms that a singleplayer world loaded,
the integrated server accepted the client, a player spawned, and Smart Moving
enabled. There were no gameplay exceptions in that session. The user's visual
test found missing legs in a climbing pose, an invisible crawling player, and
poses that stopped animating. Those are failures, not successful render tests.
The local animation-statistics hook was the confirmed cause: Smart Moving
cancels `EntityPlayer.travel` at HEAD, so the migrated `EntityLivingBase.travel`
RETURN sampler never ran for ordinary local movement. The local sample now runs
once after `SMSelf.moveEntityWithHeading`; active Elytra retains the inherited
vanilla sampler. The new source builds, and the user retested crawling,
climbing legs and flying animation in the patched client and reported that
they now run properly. Minecraft's window capture timed out twice during
automated visual inspection, so this is a user-reported visual result, not an
agent screenshot review.

The dedicated server loaded the common Mixins and reached Minecraft's EULA
gate, then stopped. The basic plain Forge milestone is now reached:

| Check | Result | Evidence |
| --- | --- | --- |
| Main menu | Passed | Forge client completed loading before the world was opened |
| Singleplayer world load | Passed | Integrated-server dimension load in client log |
| Player spawn | Passed | Player login and entity ID in client log |
| Walking and jumping | Passed | User retest after statistics fix |
| Sneaking and sprinting | Passed | User retest after statistics fix |
| First-person and third-person view | Passed | User retest after statistics fix |
| Crawl, climb and flying animation | Passed for tested cases | User retest after statistics fix; previous missing/invisible parts no longer observed |
| Major gameplay exceptions | None observed | Client log through world exit; Forge development class-scan warnings remain |

This does not individually prove every injected player hook or every advanced
movement state. Armor/held items, dedicated-server join,
disconnect/reconnect, multiplayer remote poses, Elytra and Cleanroom
compatibility remain explicit runtime validation tasks.

## Smarter Moving 1.0.0 packaging and release automation

`build.properties` now sets version `1.0.0`. The Gradle archive base includes
the Minecraft version, so a Java 8 `clean build` produces exactly one
`SmarterMoving-1.12.2-1.0.0.jar`. The Forge annotation, `mcmod.info`, and JAR
manifest all report version `1.0.0`; the display name is `Smarter Moving`.
The existing `smartmoving` mod ID and `SmartMoving 2.4` packet channel remain
stable to avoid making a branding change into a save/network identifier change.

The local Java 8 build passed on October 3. Inspection confirmed the Mixin
config and refmap, the Mixin manifest entry, and no packaged `api/player/` or
`net/smart/render/` classes. ForgeGradle is pinned to `3.0.197`, and the
nonexistent `config.forge.version` resource input was removed.

`.github/workflows/build-and-release.yml` builds on branch pushes, pull
requests, and manual dispatch. A pushed `v{version}` tag publishes a GitHub
Release only after the build succeeds and the tag matches `build.properties`.
The first GitHub-hosted branch build passed on October 3
([run 37130986432](https://github.com/DamianQualshy/SmarterMoving/actions/runs/37130986432)).
The same source was fast-forwarded to `master`; its GitHub-hosted push build
also passed ([run 37131555873](https://github.com/DamianQualshy/SmarterMoving/actions/runs/37131555873)).
Release publication remains unverified. The `v1.0.0` tag has not been created.
