package xyz.spaceio.customoregen;

public class Utils {
	public static enum Material113{
		STATIONARY_LAVA(10),
		STATIONARY_WATER(9),
		WATER(8),
		LAVA(11),
		AIR(0);
		
		int id;
		
		Material113(int id) {
			this.id = id;
		}
		int getID() {
			return this.id;
		}
	}
}
