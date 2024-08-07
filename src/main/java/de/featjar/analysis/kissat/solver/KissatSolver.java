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
package de.featjar.analysis.kissat.solver;

import de.featjar.analysis.ISolver;
import de.featjar.analysis.kissat.bin.KissatBinary;
import de.featjar.base.FeatJAR;
import de.featjar.base.data.Result;
import de.featjar.base.env.Process;
import de.featjar.base.env.TempFile;
import de.featjar.base.io.IO;
import de.featjar.formula.assignment.BooleanSolution;
import de.featjar.formula.io.dimacs.FormulaDimacsFormat;
import de.featjar.formula.structure.IFormula;
import java.time.Duration;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class KissatSolver implements ISolver {
    protected final IFormula formula;
    protected Duration timeout = Duration.ZERO;
    protected boolean isTimeoutOccurred;

    public KissatSolver(IFormula formula) { // todo: use boolean clause list input
        this.formula = formula;
    }

    public IFormula getFormula() {
        return formula;
    }

    public Duration getTimeout() {
        return timeout;
    }

    public void setTimeout(Duration timeout) {
        Objects.requireNonNull(timeout);
        FeatJAR.log().debug("setting timeout to " + timeout);
        this.timeout = timeout;
    }

    public boolean isTimeoutOccurred() {
        return isTimeoutOccurred;
    }

    public Result<BooleanSolution> getSolution() {
        isTimeoutOccurred = false;
        KissatBinary extension = FeatJAR.extension(KissatBinary.class);
        try (TempFile tempFile = new TempFile("kissatInput", ".dimacs")) {
            IO.save(formula, tempFile.getPath(), new FormulaDimacsFormat());
            Process process = extension.getProcess("-q", tempFile.getPath().toString());

            return process.get().map(this::parseSolution);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    private BooleanSolution parseSolution(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("Not output from solver");
        }
        String satResult = lines.get(0);
        switch (satResult) {
            case "s SATISFIABLE":
                if (lines.size() < 2) {
                    throw new RuntimeException("Solver did not provide solution");
                }
                return new BooleanSolution(lines.stream()
                        .skip(1)
                        .map(l -> l.split(" "))
                        .flatMapToInt(s -> Arrays.stream(s).skip(1).mapToInt(Integer::parseInt))
                        .filter(v -> v != 0)
                        .toArray());
            case "c UNKNOWN":
                isTimeoutOccurred = true;
                return null;
            case "s UNSATISFIABLE":
                return null;
            default:
                throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
    }

    public Result<Boolean> hasSolution() {
        isTimeoutOccurred = false;
        KissatBinary extension = FeatJAR.extension(KissatBinary.class);
        try (TempFile tempFile = new TempFile("cadiCalInput", ".dimacs")) {
            IO.save(formula, tempFile.getPath(), new FormulaDimacsFormat());
            Process process = extension.getProcess(
                    "--sat",
                    "-q",
                    "-t",
                    String.valueOf(timeout.toSeconds()),
                    tempFile.getPath().toString());

            return process.get().map(this::parseSatisfiable);
        } catch (Exception e) {
            FeatJAR.log().error(e);
            return Result.empty(e);
        }
    }

    private Boolean parseSatisfiable(List<String> lines) {
        if (lines.isEmpty()) {
            throw new RuntimeException("Not output from solver");
        }
        if (lines.size() > 2) {
            throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
        String satResult = lines.get(0);
        switch (satResult) {
            case "s SATISFIABLE":
                return Boolean.TRUE;
            case "c UNKNOWN":
                isTimeoutOccurred = true;
                return null;
            case "s UNSATISFIABLE":
                return Boolean.FALSE;
            default:
                throw new RuntimeException(String.format("Could not parse: %s", String.join("\n", lines)));
        }
    }
}
