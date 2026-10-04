/*
 * Copyright (c) 2020, dekvall <https://github.com/dekvall>
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
 *     runelite-client/src/main/java/net/runelite/client/plugins/grounditems/ItemThreshold.java
 *     (https://github.com/runelite/runelite)
 *
 * Copied at RuneLite commit:
 *     repo checkout : fc20e195e26f0ed799291d271af5be3d41c5dbaa
 *                     (tag runelite-parent-1.13.0)
 *     file version  : a38b363af96de247855295cc84d8e3d75e9a98f4
 *                     ("ground items: prioritize exact matches over wildcard matches")
 * The file-version commit is the authoritative content marker - diff against
 * upstream ItemThreshold.java at that SHA to see exactly what we changed.
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
 *   - No other changes; it stays package-private and is only used by the
 *     vendored ItemList.
 * ============================================================================
 */
package com.grounditemsounds.vendor;

import com.google.common.base.Strings;
import lombok.Value;

@Value
class ItemThreshold
{
	enum Inequality
	{
		LESS_THAN,
		MORE_THAN
	}

	String name;
	int quantity;
	Inequality inequality;
	boolean wildcard;

	static ItemThreshold fromName(String entry)
	{
		if (Strings.isNullOrEmpty(entry))
		{
			return null;
		}

		Inequality operator = Inequality.MORE_THAN;
		int qty = 0;
		boolean wildcard = entry.contains("*");

		for (int i = entry.length() - 1; i >= 0; i--)
		{
			char c = entry.charAt(i);
			if (c >= '0' && c <= '9' || Character.isWhitespace(c))
			{
				continue;
			}
			switch (c)
			{
				case '<':
					operator = Inequality.LESS_THAN;
					// fallthrough
				case '>':
					if (i + 1 < entry.length())
					{
						try
						{
							qty = Integer.parseInt(entry.substring(i + 1).trim());
						}
						catch (NumberFormatException e)
						{
							qty = 0;
							operator = Inequality.MORE_THAN;
						}
						entry = entry.substring(0, i);
					}
			}
			break;
		}

		return new ItemThreshold(entry.trim(), qty, operator, wildcard);
	}

	boolean quantityHolds(int itemCount)
	{
		if (inequality == Inequality.LESS_THAN)
		{
			return itemCount < quantity;
		}
		else
		{
			return itemCount > quantity;
		}
	}
}
