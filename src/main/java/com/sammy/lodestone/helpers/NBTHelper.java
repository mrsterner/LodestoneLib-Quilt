package com.sammy.lodestone.helpers;

import com.mojang.datafixers.types.templates.Tag;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class NBTHelper {

	public static NbtCompound filterTag(NbtCompound tag, String... filters) {
		return filterTag(tag, List.of(filters));
	}

	public static NbtCompound filterTag(NbtCompound tag, Collection<String> filters) {
		return filterTags(tag.copy(), filters);
	}

	/**
	 * Filters out any nbt from a CompoundTag with a key that doesn't match any of the filters.
	 * Nested CompoundTags are also filtered.
	 * If you want to filter a nested CompoundTag, you'll need to pass a "path" towards the nbt you want to keep.
	 * An example of this would be passing "fireEffect" and "fireEffect/duration".
	 * The CompoundTag under the name of "fireEffect" would be kept, but everything except "duration" inside it would be removed.
	 */
	public static NbtCompound filterTags(NbtCompound tag, Collection<String> filters) {
		NbtCompound newTag = new NbtCompound();
		//We look through the NBT and copy any Tags with a key that "filters" contains.
		for (String filter : filters) {
			var entry = tag.get(filter);
			if (entry != null) {
				//If the entry we copied over is a CompoundTag, we also apply our filters to the CompoundTag.
				//If that CompoundTag also contains a CompoundTag, it will also be filtered.
				if (entry instanceof NbtCompound compoundEntry) {
					Collection<String> updatedFilters = filters.stream().filter(s -> s.contains(filter+"/")).map(s -> s.substring(s.indexOf("/")+1)).collect(Collectors.toList());
					if (!updatedFilters.isEmpty()) {
						entry = filterTags(compoundEntry, updatedFilters);
					}
				}
				newTag.put(filter, entry);
			}
		}
		return newTag;
	}

	public static NbtCompound removeTags(NbtCompound tag, TagFilter filter) {
		NbtCompound newTag = new NbtCompound();
		for (String i : filter.filters) {
			if (tag.contains(i)) {
				if (filter.isWhitelist) {
					newTag.put(i, newTag);
				} else {
					tag.remove(i);
				}
			} else {
				for (String key : tag.getKeys()) {
					NbtElement value = tag.get(key);
					if (value instanceof NbtCompound ctag) {
						removeTags(ctag, filter);
					}
				}
			}
		}
		if (filter.isWhitelist) {
			tag = newTag;
		}
		return tag;
	}

	public static TagFilter create(String... filters) {
		return new TagFilter(filters);
	}

	public static class TagFilter {
		public final ArrayList<String> filters = new ArrayList<>();
		public boolean isWhitelist;

		public TagFilter(String... filters) {
			this.filters.addAll(List.of(filters));
		}

		public TagFilter setWhitelist() {
			this.isWhitelist = true;
			return this;
		}
	}
}
