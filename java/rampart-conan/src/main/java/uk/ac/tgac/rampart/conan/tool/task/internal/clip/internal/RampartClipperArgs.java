/**
 * RAMPART - Robust Automatic MultiPle AssembleR Toolkit
 * Copyright (C) 2013  Daniel Mapleson - TGAC
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
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 **/
package uk.ac.tgac.rampart.conan.tool.task.internal.clip.internal;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.task.internal.clip.ClipperArgs;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 01/02/13
 * Time: 09:58
 */
public class RampartClipperArgs implements ClipperArgs {

    private RampartClipperParams params = new RampartClipperParams();

    private File in;
    private File out;
    private int minLen;

    public RampartClipperArgs() {
        this.in = null;
        this.out = null;
        this.minLen = 1000;
    }

    public File getIn() {
        return in;
    }

    public void setIn(File in) {
        this.in = in;
    }

    public File getOut() {
        return out;
    }

    public void setOut(File out) {
        this.out = out;
    }

    public int getMinLen() {
        return minLen;
    }

    public void setMinLen(int minLen) {
        this.minLen = minLen;
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {
        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

        if (this.in != null)
            pvp.put(params.getInputFile(), this.in.getPath());

        if (this.out != null)
            pvp.put(params.getOutputFile(), this.out.getPath());

        if (this.minLen > 1)
            pvp.put(params.getMinLen(), String.valueOf(this.minLen));

        return pvp;
    }

    @Override
    public void setFromArgMap(Map<ConanParameter, String> pvp) {

        for(Map.Entry<ConanParameter, String> entry : pvp.entrySet()) {

            if (!entry.getKey().validateParameterValue(entry.getValue())) {
                throw new IllegalArgumentException("Parameter invalid: " + entry.getKey() + " : " + entry.getValue());
            }

            String param = entry.getKey().getName();

            if (param.equals(this.params.getInputFile().getName())) {
                this.in = new File(entry.getValue());
            }
            else if (param.equals(this.params.getOutputFile().getName())) {
                this.out = new File(entry.getValue());
            }
            else if (param.equals(this.params.getMinLen().getName())) {
                this.minLen = Integer.parseInt(entry.getValue());
            }
            else {
                throw new IllegalArgumentException("Unknown param found: " + param);
            }
        }
    }
}
