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
package uk.ac.tgac.rampart.conan.tool.external.qt.sickle;

import uk.ac.ebi.fgpt.conan.model.ConanParameter;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmerArgs;
import uk.ac.tgac.rampart.core.utils.StringJoiner;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 14:59
 */
public abstract class SickleV11Args implements QualityTrimmerArgs {

    private int qualThreshold;
    private int lengthThreshold;
    private boolean discardN;
    private SickleV11QualityTypeParameter.SickleQualityTypeOptions qualType;

    public SickleV11Args() {
        this.qualThreshold = 20;
        this.lengthThreshold = 20;
        this.discardN = false;
        this.qualType = SickleV11QualityTypeParameter.SickleQualityTypeOptions.SANGER;
    }

    @Override
    public int getQualityThreshold() {
        return qualThreshold;
    }

    @Override
    public void setQualityThreshold(int qualityThreshold) {
        this.qualThreshold = qualityThreshold;
    }

    @Override
    public int getMinLength() {
        return lengthThreshold;
    }

    @Override
    public void setMinLength(int minLength) {
        this.lengthThreshold = minLength;
    }

    public Boolean isDiscardN() {
        return discardN;
    }

    public void setDiscardN(Boolean discardN) {
        this.discardN = discardN;
    }

    public SickleV11QualityTypeParameter.SickleQualityTypeOptions getQualType() {
        return qualType;
    }

    public void setQualType(SickleV11QualityTypeParameter.SickleQualityTypeOptions qualType) {
        this.qualType = qualType;
    }

    @Override
    public Map<ConanParameter, String> getArgMap() {

        // Can't instantiate a SickleV11Params object because it's abstract, so we'll get
        // what we need from SicklePeV11Params instead.
        SicklePeV11Params params = new SicklePeV11Params();

        Map<ConanParameter, String> pvp = new LinkedHashMap<ConanParameter, String>();

		if (this.qualThreshold != 20) {
            pvp.put(params.getQualityThreshold(), "--" + params.getQualityThreshold().getName() + "=" + String.valueOf(this.qualThreshold));
        }

		if (this.lengthThreshold != 20) {
            pvp.put(params.getLengthThreshold(),  "--" + params.getLengthThreshold().getName() + "=" + String.valueOf(this.lengthThreshold));
        }

		if (this.discardN) {
            pvp.put(params.getDiscardN(), "--" + params.getDiscardN().getName());
        }

		if (this.qualType != null)
			pvp.put(params.getQualityType(), "--" + params.getQualityType() + " " + this.qualType.toString().toLowerCase());

        return pvp;
    }

}
