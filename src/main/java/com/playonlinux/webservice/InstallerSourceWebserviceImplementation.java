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

package com.playonlinux.webservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.playonlinux.common.api.services.BackgroundService;
import com.playonlinux.common.api.webservice.InstallerSource;
import com.playonlinux.common.dto.web.CategoryDTO;
import com.playonlinux.common.dto.web.DownloadEnvelopeDTO;
import com.playonlinux.common.dto.web.ProgressStateDTO;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Observable;
import java.util.concurrent.Semaphore;

/**
 * This class download scripts from a playonlinux web service and converts it into DTOs
 */
public class InstallerSourceWebserviceImplementation extends Observable
        implements BackgroundService, InstallerSource {

    private final URL url;
    private ProgressStateDTO.State state = ProgressStateDTO.State.READY;
    private Semaphore updateSemaphore = new Semaphore(1);
    private final static Logger logger = Logger.getLogger(InstallerSourceWebserviceImplementation.class);
    private List<CategoryDTO> categories;

    public InstallerSourceWebserviceImplementation(URL url) {
        this.url = url;
    }

    synchronized public void populate() {
        try {
            categories = null;

            updateSemaphore.acquire();
            this.state = ProgressStateDTO.State.PROGRESSING;
            this.setChanged();
            this.update();

            try {
                ObjectMapper mapper = new ObjectMapper();
                HTTPDownloader httpDownloader = new HTTPDownloader(this.url);
                String result = httpDownloader.get();
                categories = mapper.readValue(result, new TypeReference<List<CategoryDTO>>() {});
                this.state = ProgressStateDTO.State.SUCCESS;
            } catch(DownloadException e) {
                logger.warn(String.format("Error while downloading %s", url), e);
                this.state = ProgressStateDTO.State.FAILED;
            } catch (IOException e) {
                logger.warn(String.format("IO error while downloading %s", url), e);
                this.state = ProgressStateDTO.State.FAILED;
            } finally {
                this.update();
            }
        } catch (InterruptedException ignored) {
            logger.info(String.format("The download was interrupted: %s", url), ignored);
        } finally {
            updateSemaphore.release();
        }
    }


    private synchronized void update() {
        DownloadEnvelopeDTO<List<CategoryDTO>> envelopeDTO = new DownloadEnvelopeDTO<>();
        ProgressStateDTO progressStateDTO = new ProgressStateDTO.Builder().withState(state).build();

        envelopeDTO.setDownloadState(progressStateDTO);
        envelopeDTO.setEnvelopeContent(categories);

        this.setChanged();
        this.notifyObservers(envelopeDTO);
    }

    @Override
    public void shutdown() {
        // Nothing to do to shutdown this service
    }

    @Override
    synchronized public void start() {
        new Thread() {
            @Override
            public void run() {
                populate();
            }
        }.start();
    }
}
