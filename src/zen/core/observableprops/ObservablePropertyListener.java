/*
 * Copyright [2013] [Nazmul Idris]
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package zen.core.observableprops;

import zen.core.*;

/**
 * Extend this class to observe values for the fields in
 * {@link AppData.ID_Types#ObservableProperty} R.ids
 */
public abstract class ObservablePropertyListener {

/**
 * this is what's used to automagically bind this observer to
 * the {@link AppData.ID_Types#ObservableProperty} R.ids.
 */
public abstract int getPropertyId();

/** this identifies this listener */
public abstract String getName();

/**
 * this method is run on the main thread
 *
 * @param propertyId the {@link AppData.ID_Types#ObservableProperty} R.id
 * @param value      this may be null, if it's not been set yet
 */
public abstract void onChange(int propertyId, Object value);

}
