/*
 * Copyright (c) 2026, Adam <Adam@sigterm.info>
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
/*
 * ============================================================================
 * VENDORED FROM RUNELITE - KEEP IN SYNC WITH UPSTREAM, DO NOT HAND-EDIT LOGIC.
 * ----------------------------------------------------------------------------
 * Source file in the RuneLite repo:
 *     runelite-client/src/main/java/net/runelite/client/plugins/grounditems/ItemList.java
 *     (https://github.com/runelite/runelite)
 *
 * Copied at RuneLite commit:
 *     repo checkout : fc20e195e26f0ed799291d271af5be3d41c5dbaa
 *                     (tag runelite-parent-1.13.0)
 *     file version  : a38b363af96de247855295cc84d8e3d75e9a98f4
 *                     ("ground items: prioritize exact matches over wildcard matches")
 * The file-version commit is the authoritative content marker - diff against
 * upstream ItemList.java at that SHA to see exactly what we changed.
 * To re-sync: check out a newer RuneLite, re-copy the file, bump the two SHAs
 * above, and re-apply the deviations described below.
 *
 * Why vendored: Ground Item Sounds follows the Ground Items highlight and
 * hidden lists, and should match them exactly the way Ground Items does,
 * including quantity thresholds ("coins > 1000") and exact-over-wildcard
 * priority. Upstream's class is package-private to
 * net.runelite.client.plugins.grounditems, so we cannot import it - hence
 * this copy.
 *
 * Deviations from upstream (intentional, keep minimal for easy re-sync):
 *   - Package moved to com.grounditemsounds.vendor.
 *   - Made the class, its constructor, the NONE/WILDCARD/EXACT constants and
 *     matches() public so the plugin's package can use them.
 *   - matches() takes (String name, int quantity) instead of upstream's
 *     GroundItem, which is also package-private; the matching logic is
 *     unchanged.
 * ============================================================================
 */
package com.grounditemsounds.vendor;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Value;
import net.runelite.client.util.WildcardMatcher;

@Value
public class ItemList
{
	public static final int NONE = 0;
	public static final int WILDCARD = 1;
	public static final int EXACT = 2;

	List<ItemThreshold> items;

	public ItemList(List<String> items)
	{
		this.items = items.stream()
			.map(ItemThreshold::fromName)
			.filter(Objects::nonNull)
			.collect(Collectors.toList());
	}

	public int matches(String name, int quantity)
	{
		for (ItemThreshold it : items)
		{
			if (!it.isWildcard()
				&& it.getName().equalsIgnoreCase(name)
				&& it.quantityHolds(quantity))
			{
				return EXACT;
			}
		}

		for (ItemThreshold it : items)
		{
			if (it.isWildcard()
				&& WildcardMatcher.matches(it.getName(), name)
				&& it.quantityHolds(quantity))
			{
				return WILDCARD;
			}
		}

		return NONE;
	}
}
