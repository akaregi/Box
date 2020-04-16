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

package net.okocraft.box.command.boxadmin;

import net.okocraft.box.command.BaseCommand;

public abstract class BaseAdminCommand extends BaseCommand {
    
    protected BaseAdminCommand(String name, String permissionNode, int leastArgLength, boolean isPlayerOnly,
            String usage, String[] alias) {
        super(name, permissionNode, leastArgLength, isPlayerOnly, usage, alias);
    }

    @Override
    public String getDescription() {
        return messages.getMessage("command.admin-command-description." + getName());
    }
}