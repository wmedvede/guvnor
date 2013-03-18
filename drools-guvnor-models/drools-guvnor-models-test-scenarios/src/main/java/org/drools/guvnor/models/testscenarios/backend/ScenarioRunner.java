/*
 * Copyright 2010 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.drools.guvnor.models.testscenarios.backend;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.drools.core.base.ClassTypeResolver;
import org.drools.core.base.TypeResolver;
import org.drools.base.ClassTypeResolver;
import org.drools.base.TypeResolver;
import org.drools.common.InternalRuleBase;
import org.drools.common.InternalWorkingMemory;
import org.drools.core.common.InternalRuleBase;
import org.drools.core.impl.KnowledgeBaseImpl;
import org.drools.core.base.ClassTypeResolver;
import org.drools.core.common.InternalRuleBase;
import org.drools.guvnor.models.testscenarios.backend.populators.FactPopulator;
import org.drools.guvnor.models.testscenarios.backend.populators.FactPopulatorFactory;
import org.drools.guvnor.models.testscenarios.shared.ActivateRuleFlowGroup;
import org.drools.guvnor.models.testscenarios.shared.CallMethod;
import org.drools.guvnor.models.testscenarios.shared.ExecutionTrace;
import org.drools.guvnor.models.testscenarios.shared.Expectation;
import org.drools.guvnor.models.testscenarios.shared.FactData;
import org.drools.guvnor.models.testscenarios.shared.Fixture;
import org.drools.guvnor.models.testscenarios.shared.RetractFact;
import org.drools.guvnor.models.testscenarios.shared.Scenario;
import org.kie.api.runtime.KieSession;
import org.drools.guvnor.models.testscenarios.shared.Expectation;
import org.drools.impl.KnowledgeBaseImpl;
import org.kie.runtime.KieSession;
import org.mvel2.MVEL;

/**
 * This actually runs the test scenarios.
 */
public class ScenarioRunner {

    private final KieSession ksession;
    private TestScenarioKSessionWrapper workingMemoryWrapper;
    private FactPopulatorFactory factPopulatorFactory;
    private FactPopulator factPopulator;

    /**
     * This constructor is normally used by Guvnor for running tests on a users
     * request.
     * @param ksession A populated type resolved to be used to resolve the types in
     * the scenario.
     * <p/>
     * For info on how to invoke this, see
     * ContentPackageAssemblerTest.testPackageWithRuleflow in
     * guvnor-webapp This requires that the classloader for the
     * thread context be set appropriately. The PackageBuilder can
     * provide a suitable TypeResolver for a given package header,
     * and the Package config can provide a classloader.
     * @param resolver This is used by MVEL to instantiate classes in expressions, in
     * particular enum field values. See EnumFieldPopulator and
     * FactFieldValueVerifier
     */
    public ScenarioRunner( final KieSession ksession ) throws ClassNotFoundException {
        this.ksession = ksession;
    }

    public void run( Scenario scenario )
            throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException {

        Map<String, Object> populatedData = new HashMap<String, Object>();
        Map<String, Object> globalData = new HashMap<String, Object>();


        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        // This looks safe!
        ClassLoader classloader2 = ((InternalRuleBase) ((KnowledgeBaseImpl) ksession.getKieBase()).getRuleBase()).getRootClassLoader();

        ClassTypeResolver resolver = new ClassTypeResolver(
                scenario.getImports().getImportStrings(),
                classloader2 );

        this.workingMemoryWrapper = new TestScenarioKSessionWrapper(ksession,
                resolver,
                classloader,
                populatedData,
                globalData);
        this.factPopulatorFactory = new FactPopulatorFactory(populatedData,
                globalData,
                resolver,
                classloader);
        this.factPopulator = new FactPopulator(ksession,
                populatedData);

        MVEL.COMPILER_OPT_ALLOW_NAKED_METH_CALL = true;
        scenario.setLastRunResult( new Date() );

        populateGlobals( scenario.getGlobals() );

        applyFixtures( scenario.getFixtures(),
                       createScenarioSettings( scenario ) );
    }

    private ScenarioSettings createScenarioSettings( Scenario scenario ) {
        ScenarioSettings scenarioSettings = new ScenarioSettings();
        scenarioSettings.setRuleList( scenario.getRules() );
        scenarioSettings.setInclusive( scenario.isInclusive() );
        scenarioSettings.setMaxRuleFirings( getMaxRuleFirings( scenario ) );
        return scenarioSettings;
    }

    private int getMaxRuleFirings( Scenario scenario ) {
        String property = System.getProperty( "guvnor.testscenario.maxrulefirings" );
        if ( property == null ) {
            return scenario.getMaxRuleFirings();
        } else {
            return Integer.parseInt( property );
        }
    }

    private void applyFixtures( List<Fixture> fixtures,
                                ScenarioSettings scenarioSettings )
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        for ( Iterator<Fixture> iterator = fixtures.iterator(); iterator.hasNext(); ) {
            Fixture fixture = iterator.next();

            if ( fixture instanceof FactData ) {

                factPopulator.add( factPopulatorFactory.createFactPopulator( (FactData) fixture ) );

            } else if ( fixture instanceof RetractFact ) {

                factPopulator.retractFact( ( (RetractFact) fixture ).getName() );

            } else if ( fixture instanceof CallMethod ) {

                workingMemoryWrapper.executeMethod( (CallMethod) fixture );

            } else if ( fixture instanceof ActivateRuleFlowGroup ) {

                workingMemoryWrapper.activateRuleFlowGroup( ( (ActivateRuleFlowGroup) fixture ).getName() );

            } else if ( fixture instanceof ExecutionTrace ) {

                factPopulator.populate();

                workingMemoryWrapper.executeSubScenario( (ExecutionTrace) fixture,
                                                         scenarioSettings );

            } else if ( fixture instanceof Expectation ) {

                factPopulator.populate();

                workingMemoryWrapper.verifyExpectation( (Expectation) fixture );
            } else {
                throw new IllegalArgumentException( "Not sure what to do with " + fixture );
            }

        }

        factPopulator.populate();
    }

    private void populateGlobals( List<FactData> globals )
            throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {

        for ( final FactData fact : globals ) {
            factPopulator.add(
                    factPopulatorFactory.createGlobalFactPopulator( fact ) );
        }

        factPopulator.populate();
    }
}