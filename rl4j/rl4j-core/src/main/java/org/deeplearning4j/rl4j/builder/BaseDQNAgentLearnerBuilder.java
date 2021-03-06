/*******************************************************************************
 * Copyright (c) 2020 Konduit K.K.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Apache License, Version 2.0 which is available at
 * https://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 ******************************************************************************/
package org.deeplearning4j.rl4j.builder;

import lombok.AccessLevel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.experimental.SuperBuilder;
import org.apache.commons.lang3.builder.Builder;
import org.deeplearning4j.rl4j.agent.IAgentLearner;
import org.deeplearning4j.rl4j.agent.learning.algorithm.dqn.BaseTransitionTDAlgorithm;
import org.deeplearning4j.rl4j.agent.learning.update.FeaturesLabels;
import org.deeplearning4j.rl4j.agent.learning.update.updater.INeuralNetUpdater;
import org.deeplearning4j.rl4j.agent.learning.update.updater.LabelsNeuralNetUpdater;
import org.deeplearning4j.rl4j.environment.Environment;
import org.deeplearning4j.rl4j.environment.IActionSchema;
import org.deeplearning4j.rl4j.experience.ExperienceHandler;
import org.deeplearning4j.rl4j.experience.ReplayMemoryExperienceHandler;
import org.deeplearning4j.rl4j.learning.sync.Transition;
import org.deeplearning4j.rl4j.network.ITrainableNeuralNet;
import org.deeplearning4j.rl4j.observation.transform.TransformProcess;
import org.deeplearning4j.rl4j.policy.DQNPolicy;
import org.deeplearning4j.rl4j.policy.EpsGreedy;
import org.deeplearning4j.rl4j.policy.INeuralNetPolicy;
import org.deeplearning4j.rl4j.policy.IPolicy;
import org.nd4j.linalg.api.rng.Random;

/**
 * A base {@link IAgentLearner} builder that will setup these:
 * <li>a epsilon-greedy policy</li>
 * <li>a replay-memory experience handler</li>
 * <li>a neural net updater that expects feature-labels update data</li>
 *
 * Used as the base of DQN builders.
 */
public abstract class BaseDQNAgentLearnerBuilder extends BaseAgentLearnerBuilder<Integer, Transition<Integer>, FeaturesLabels> {

    @Getter(AccessLevel.PROTECTED)
    private final Configuration configuration;

    private final Random rnd;

    public BaseDQNAgentLearnerBuilder(Configuration configuration,
                                      ITrainableNeuralNet neuralNet,
                                      Builder<Environment<Integer>> environmentBuilder,
                                      Builder<TransformProcess> transformProcessBuilder,
                                      Random rnd) {
        super(configuration, neuralNet, environmentBuilder, transformProcessBuilder);
        this.configuration = configuration;
        this.rnd = rnd;
    }

    @Override
    protected IPolicy<Integer> buildPolicy() {
        INeuralNetPolicy<Integer> greedyPolicy = new DQNPolicy<Integer>(networks.getThreadCurrentNetwork());
        IActionSchema<Integer> actionSchema = getEnvironment().getSchema().getActionSchema();
        return new EpsGreedy(greedyPolicy, actionSchema, configuration.getPolicyConfiguration(), rnd);
    }

    @Override
    protected ExperienceHandler<Integer, Transition<Integer>> buildExperienceHandler() {
        return new ReplayMemoryExperienceHandler(configuration.getExperienceHandlerConfiguration(), rnd);
    }

    @Override
    protected INeuralNetUpdater<FeaturesLabels> buildNeuralNetUpdater() {
        return new LabelsNeuralNetUpdater(networks.getThreadCurrentNetwork(), networks.getTargetNetwork(), configuration.getNeuralNetUpdaterConfiguration());
    }

    @EqualsAndHashCode(callSuper = true)
    @SuperBuilder
    @Data
    public static class Configuration extends BaseAgentLearnerBuilder.Configuration<Integer> {
        EpsGreedy.Configuration policyConfiguration;
        ReplayMemoryExperienceHandler.Configuration experienceHandlerConfiguration;
        LabelsNeuralNetUpdater.Configuration neuralNetUpdaterConfiguration;
        BaseTransitionTDAlgorithm.Configuration updateAlgorithmConfiguration;
    }
}
