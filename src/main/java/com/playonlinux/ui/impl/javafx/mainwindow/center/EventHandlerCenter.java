/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.ui.impl.javafx.mainwindow.center;

import com.playonlinux.common.api.services.EventHandler;
import com.playonlinux.common.api.services.RemoteAvailableInstallers;
import com.playonlinux.injection.Inject;
import com.playonlinux.injection.Scan;
import com.playonlinux.ui.api.UIEventHandler;


@Scan
public class EventHandlerCenter implements UIEventHandler {


    @Inject
    static EventHandler mainEventHandler;

    @Override
    public EventHandler getMainEventHandler() {
        return mainEventHandler;
    }

    public RemoteAvailableInstallers getRemoteAvailableInstallers() {
        return mainEventHandler.getRemoteAvailableInstallers();
    }

    public void updateAvailableInstallers() {
        getRemoteAvailableInstallers().refresh();
    }



}
