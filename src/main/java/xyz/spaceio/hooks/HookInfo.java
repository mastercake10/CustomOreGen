package xyz.spaceio.hooks;

/**
 * Stores all existing hooks for sky block plugins
 * 
 * @author MasterCake
 *
 */
public enum HookInfo {

	ASkyBlock(HookASkyBlock.class), BentoBox(HookBentoBox.class),
	IslandWorld(HookIslandWorld.class), SpaceSkyblock(HookSpaceSkyblock.class),
	SuperiorSkyblock2(HookSuperiorSkyblock.class), uSkyBlock(HookuSkyBlock.class),
	FabledSkyBlock(HookFabledSkyblock.class), PlotSquared(HookPlotSquared.class), PlotSquaredLegacy(HookPlotSquaredLegacy.class), Vanilla(HookVanilla.class);

	private Class<?> hookClass;

	HookInfo(Class<?> hookClass) {
		this.hookClass = hookClass;
	}

	public Class<?> getHookClass() {
		return this.hookClass;
	}
}
