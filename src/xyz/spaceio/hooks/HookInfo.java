package xyz.spaceio.hooks;

/**
 * Stores all existing hooks for sky block plugins
 * 
 * @author MasterCake
 *
 */
public enum HookInfo {
	
	AcidIsland(HookAcidIsland.class), ASkyBlock(HookASkyBlock.class), BentoBox(HookBentoBox.class), IslandWorld(HookIslandWorld.class), PlotSquared(HookPlotSquared.class),
	SpaceSkyblock(HookSpaceSkyblock.class), SuperiorSkyblock2(HookSuperiorSkyblock.class), uSkyBlock(HookuSkyBlock.class);
	
	private Class<?> hookClass;
	
	HookInfo(Class<?> hookClass) {
		this.hookClass = hookClass;
	}
	
	public Class<?> getHookClass(){
		return this.hookClass;
	}
}
