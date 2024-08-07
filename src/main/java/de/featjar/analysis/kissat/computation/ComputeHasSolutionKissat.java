/*
 * Copyright (C) 2024 FeatJAR-Development-Team
 *
 * This file is part of FeatJAR-formula-analysis-kissat.
 *
 * formula-analysis-kissat is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3.0 of the License,
 * or (at your option) any later version.
 *
 * formula-analysis-kissat is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with formula-analysis-kissat. If not, see <https://www.gnu.org/licenses/>.
 *
 * See <https://github.com/FeatureIDE/FeatJAR-formula-analysis-cadical> for further information.
 */
package de.featjar.analysis.kissat.computation;

import de.featjar.base.computation.IComputation;
import de.featjar.base.computation.Progress;
import de.featjar.base.data.Result;
import de.featjar.formula.structure.IFormula;
import java.util.List;

public class ComputeHasSolutionKissat extends AKissatAnalysis<Boolean> {

    public ComputeHasSolutionKissat(IComputation<IFormula> cnfFormula) {
        super(cnfFormula);
    }

    public ComputeHasSolutionKissat(ComputeHasSolutionKissat other) {
        super(other);
    }

    @Override
    public Result<Boolean> compute(List<Object> dependencyList, Progress progress) {
        return initializeSolver(dependencyList).hasSolution();
    }
}
