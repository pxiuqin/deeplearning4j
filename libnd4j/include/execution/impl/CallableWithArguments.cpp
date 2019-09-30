/*******************************************************************************
 * Copyright (c) 2015-2018 Skymind, Inc.
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

//
// @author raver119@gmail.com
//

#include <execution/CallableWithArguments.h>

namespace samediff {
    CallableWithArguments::CallableWithArguments(FUNC_DO &func, uint64_t thread_id) {
        _function_do = func;
        _finished = false;
        _threadId = thread_id;
    }

    CallableWithArguments::CallableWithArguments(FUNC_3D &func, uint64_t thread_id, uint64_t start_x, uint64_t stop_x, uint64_t increment_x, uint64_t start_y, uint64_t stop_y, uint64_t increment_y, uint64_t start_z, uint64_t stop_z, uint64_t increment_z) {
        _function_3d = func;
        _arguments = {start_x, stop_x, increment_x, start_y, stop_y, increment_y, start_z, stop_z, increment_z};
        _finished = false;
        _threadId = thread_id;
    }

    CallableWithArguments::CallableWithArguments(FUNC_1D &func, uint64_t thread_id, uint64_t start_x,
                                                 uint64_t stop_x, uint64_t increment_x) {
        _function_1d = func;
        _arguments = {start_x, stop_x, increment_x};
        _finished = false;
        _threadId = thread_id;
    }

    CallableWithArguments::CallableWithArguments(
            FUNC_2D &func, uint64_t thread_id, uint64_t start_x,
            uint64_t stop_x, uint64_t increment_x, uint64_t start_y, uint64_t stop_y, uint64_t increment_y) {
        _function_2d = func;
        _arguments = {start_x, stop_x, increment_x, start_y, stop_y, increment_y};
        _finished = false;
        _threadId = thread_id;
    }

    int CallableWithArguments::dimensions() {
        return _arguments.size() / 3;
    }

    std::vector<uint64_t>& CallableWithArguments::arguments() {
        return _arguments;
    }

    bool CallableWithArguments::finished() {
        return _finished.load();
    }

    void CallableWithArguments::finish() {
        _finished = true;
        _condition.notify_one();
    }

    void CallableWithArguments::waitUntilFinished() {
        std::unique_lock<std::mutex> lock(_lock);
        _condition.wait(lock, [=]{ return _finished.load(); });
    }


    FUNC_1D CallableWithArguments::function_1d() {
        return _function_1d;
    }

    FUNC_2D CallableWithArguments::function_2d() {
        return _function_2d;
    }

    FUNC_DO CallableWithArguments::function_do() {
        return _function_do;
    }

    FUNC_3D CallableWithArguments::function_3d() {
        return _function_3d;
    }

    uint64_t CallableWithArguments::threadId() {
        return _threadId;
    }
}