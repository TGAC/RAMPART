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
package uk.ac.tgac.rampart.conan.tool.proc.external.asm;

import uk.ac.tgac.rampart.conan.tool.proc.external.asm.abyss.AbyssV134Process;

/**
 * User: maplesod
 * Date: 30/01/13
 * Time: 18:49
 */
public enum AssemblerFactory {

    ABYSS_V1_3_4 {
        @Override
        public String getToolName() {
            return "ABYSS";
        }

        @Override
        public Assembler createAsm() {
            return new AbyssV134Process();
        }
    };

    public abstract String getToolName();

    public abstract Assembler createAsm();

    public static Assembler createAssembler() {
        return ABYSS_V1_3_4.createAsm();
    }

    public static Assembler create(String toolType) {

        for (AssemblerFactory inst : AssemblerFactory.values()) {

            if (inst.getToolName().equalsIgnoreCase(toolType)) {

                return inst.createAsm();
            }

        }
        return null;
    }
}
