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
import uk.ac.tgac.rampart.conan.conanx.process.ProcessArgs;
import uk.ac.tgac.rampart.conan.tool.external.asm.abyss.AbyssV134Params;
import uk.ac.tgac.rampart.conan.tool.external.qt.QualityTrimmerArgs;

import java.util.HashMap;
import java.util.Map;

/**
 * User: maplesod
 * Date: 23/01/13
 * Time: 14:59
 */
public abstract class SickleV11Args implements ProcessArgs, QualityTrimmerArgs {

    private Integer qualThreshold;
    private Integer lengthThreshold;
    private Boolean discardN;
    private SickleV11QualityTypeParameter.SickleQualityTypeOptions qualType;

    public SickleV11Args() {
        this.qualThreshold = 20;
        this.lengthThreshold = 20;
        this.discardN = false;
        this.qualType = null;
    }

    public Integer getQualThreshold() {
        return qualThreshold;
    }

    public Integer getLengthThreshold() {
        return lengthThreshold;
    }

    public Boolean isDiscardN() {
        return discardN;
    }

    public void setQualThreshold(Integer qualThreshold) {
        this.qualThreshold = qualThreshold;
    }

    public void setLengthThreshold(Integer lengthThreshold) {
        this.lengthThreshold = lengthThreshold;
    }

    public void setDiscardN(Boolean discardN) {
        this.discardN = discardN;
    }

    public void setQualType(SickleV11QualityTypeParameter.SickleQualityTypeOptions qualType) {
        this.qualType = qualType;
    }

    public SickleV11QualityTypeParameter.SickleQualityTypeOptions getQualType() {
        return qualType;
    }


    @Override
    public Map<ConanParameter, String> getParameterValuePairs() {

        // Can't instantiate a SickleV11Params object because it's abstract, so we'll get
        // what we need from SicklePeV11Params instead.
        SicklePeV11Params params = new SicklePeV11Params();

        Map<ConanParameter, String> pvp = new HashMap<ConanParameter, String>();

		if (this.qualThreshold != null)
			pvp.put(params.getQualityThreshold(), this.qualThreshold.toString());

		if (this.lengthThreshold != null)
			pvp.put(params.getLengthThreshold(), this.lengthThreshold.toString());

		if (this.discardN != null)
			pvp.put(params.getDiscardN(), this.discardN.toString());

		if (this.qualType != null)
			pvp.put(params.getQualityType(), this.qualType.toString().toLowerCase());

        return pvp;
    }

}
