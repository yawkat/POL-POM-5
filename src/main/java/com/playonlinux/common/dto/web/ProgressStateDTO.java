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

package com.playonlinux.common.dto.web;

import com.playonlinux.common.api.dto.AbstractDTO;

public class ProgressStateDTO implements AbstractDTO {
    private final State state;
    private final double percent;

    public ProgressStateDTO(Builder builder) {
        this.state = builder.state;
        this.percent = builder.percent;
    }

    public State getState() {
        return state;
    }

    public double getPercent() {
        return percent;
    }

    public enum State {
        READY,
        PROGRESSING,
        SUCCESS,
        FAILED
    }

    @Override
    public String toString() {
        return this.state.name();
    }

    public static class Builder {
        private State state;
        private double percent;

        public Builder(ProgressStateDTO other) {
            state = other.state;
            percent = other.percent;
        }

        public Builder() {

        }

        public Builder withState(State state) {
            this.state = state;
            return this;
        }

        public Builder withPercent(double percent) {
            this.percent = percent;
            return this;
        }

        public ProgressStateDTO build() {
            return new ProgressStateDTO(this);
        }
    }
}
