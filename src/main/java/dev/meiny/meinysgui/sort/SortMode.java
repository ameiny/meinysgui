package dev.meiny.meinysgui.sort;

public enum SortMode {
	DEFAULT,
	NAME,
	COUNT;

	public static SortMode byId(int id) {
		SortMode[] values = values();
		return id >= 0 && id < values.length ? values[id] : DEFAULT;
	}
}
