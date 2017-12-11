package de.Linus122.customoregen;

public class GeneratorItem {
	String name;
	Byte damage = 0;
	double chance = 0d;
	
	GeneratorItem(String name, byte damage, double chance) {
		this.name = name;
		this.damage = damage;
		this.chance = chance;
	}
}
