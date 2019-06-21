/*
 * Box
 * Copyright (C) 2019 OKOCRAFT
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package net.okocraft.box.util;

import javax.annotation.Nonnull;

/**
 * メッセージを取り扱うツール群。
 *
 * @author akaregi
 * @since v1.1.0
 */
class MessageUtil {
    /**
     * & 形式カラーコードのメッセージを § 形式カラーコードのメッセージに変換する。
     *
     * @author akaregi
     * @since v1.1.0
     *
     * @param message メッセージ
     *
     * @return カラーコードが変換されたメッセージ
     */
    @Nonnull
    static String convertColorCode(@Nonnull String message) {
        return message.replaceAll("&([a-f0-9])", "§$1");
    }
}
