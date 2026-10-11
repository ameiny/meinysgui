package dev.meiny.meinysgui.sort;

public enum HotbarMode {
	IGNORE,
	INCLUDE,
	SEPARATE;

	public static HotbarMode byId(int id) {
		HotbarMode[] values = values();
		return id >= 0 && id < values.length ? values[id] : IGNORE;
	}
}
