/*
 * Copyright (c) 2017-2018 Aion foundation.
 *
 *     This file is part of the aion network project.
 *
 *     The aion network project is free software: you can redistribute it
 *     and/or modify it under the terms of the GNU General Public License
 *     as published by the Free Software Foundation, either version 3 of
 *     the License, or any later version.
 *
 *     The aion network project is distributed in the hope that it will
 *     be useful, but WITHOUT ANY WARRANTY; without even the implied
 *     warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *     See the GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with the aion network project source files.
 *     If not, see <https://www.gnu.org/licenses/>.
 *
 * Contributors:
 *     Aion foundation.
 */

package org.aion.mcf.trie;

/**
 * Possible results after importing a trie node.
 *
 * @author Alexandra Roatis
 */
public enum TrieNodeResult {
    /** Successful import. */
    IMPORTED,
    /** The key-value pair is already stored. */
    KNOWN,
    /** The key is already stored but the value does not match the given one. */
    INCONSISTENT,
    /** The key is not valid. */
    INVALID_KEY,
    /** The value is not valid. */
    INVALID_VALUE;

    public boolean isSuccessful() {
        return equals(IMPORTED) || equals(KNOWN);
    }
}