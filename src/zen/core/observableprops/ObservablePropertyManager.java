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

import android.os.*;
import android.util.*;
import zen.core.*;
import zen.utlis.*;

import java.util.*;

import static zen.utlis.IconPaths.*;

/**
 * This class works hand in hand with  {@link AppData.ID_Types#ObservableProperty} R.ids,
 * which contains all the properties for the app.
 *
 * @author Nazmul Idris
 * @version 1.0
 * @since 3/11/13, 5:27 PM
 */
public class ObservablePropertyManager {

/**
 * this map actually holds the key value pairs, where the keys are the
 * {@link AppData.ID_Types#ObservableProperty} R.ids
 */
private SparseArray<Object>                                mapOfFieldValues    =
    new SparseArray<Object>();
/**
 * this map actually holds the list of listeners for each key. the key is
 * {@link AppData.ID_Types#ObservableProperty} R.ids
 */
private SparseArray<ArrayList<ObservablePropertyListener>> mapOfFieldObservers =
    new SparseArray<ArrayList<ObservablePropertyListener>>();
private Handler handlerMainThread;

public ObservablePropertyManager(Handler handler) {
  handlerMainThread = handler;
}

/**
 * this adds the {@link ObservablePropertyListener} to the property, and it also fires the
 * {@link ObservablePropertyListener#onChange(int, Object)} method, if the
 * {@link #getValue(int, Object)} or defaultValue is NOT null.
 * <p/>
 * Notes:
 * <ol>
 * <li>instead of calling this method, consider simply adding the observer as a resource
 * to {@link LifecycleHelper#addResource(Object...)} which will not only call this method
 * but also will release the listener automagically.</li>
 * <li>there is a good reason to call this method, if you want the observer to be run
 * the first time that it's added (by making the defaultValue NOT null). There are a few
 * classes that need this complex capability.</li>
 * </ol>
 *
 * @param observer     add this observer to the list of {@link ObservablePropertyListener} for the
 *                     field
 * @param defaultValue this is the object that should be passed to observer, if there's no value set
 *                     for it yet. note that this can be null, if that's what a caller passes in here.
 *                     if this value is NOT null, then any registered observers WILL be fired.
 */
public ObservablePropertyListener addPropertyChangeObserver(ObservablePropertyListener observer,
                                                            Object defaultValue)
{

  // observer can't be null
  if (observer == null) { return observer; }

  try {
    // get the property in question from the observer
    int propertyId = observer.getPropertyId();

    // get the list if it exists, or create a new one & save it
    ArrayList<ObservablePropertyListener> observersForProperty;
    if (mapOfFieldObservers.indexOfKey(propertyId) > 0) {
      observersForProperty = mapOfFieldObservers.get(propertyId);
    }
    else {
      observersForProperty = new ArrayList<ObservablePropertyListener>();
      mapOfFieldObservers.put(propertyId, observersForProperty);
    }

    // check for dupes!
    if (observersForProperty.contains(observer)) { return observer; }

    // add the observer
    observersForProperty.add(observer);
    AndroidUtils.log(MyApp,
                     String.format("ObservableProperty.[%s] property change observer [%s] added",
                                   propertyId,
                                   observer.getName()));

    // fire the onChange method on this observer (if the value isn't null!)
    Object value = getValue(propertyId, defaultValue);
    if (value != null) {
      try {
        observer.onChange(propertyId, value);
      }
      catch (Exception e) {
        AndroidUtils.logErr(
            MyApp,
            String.format("ObservableProperty.[%s] problem running property change observer [%s]",
                          propertyId,
                          observer.getName()),
            e);
      }
      AndroidUtils.log(MyApp,
                       String.format("ObservableProperty.[%s] property change observer [%s] fired",
                                     propertyId,
                                     observer.getName())
      );
    }

    return observer;
  }
  catch (Exception e) {
    AndroidUtils.logErr(MyApp,
                        String.format("ObservableProperty.[%s] problem adding listener [%s]",
                                      observer.getPropertyId(),
                                      observer.getName())
    );
    return observer;
  }

}

/** given a observer, simply remove it from the {@link #mapOfFieldObservers} */
public void removePropertyChangeObserver(ObservablePropertyListener observer) {
  if (observer == null) { return; }
  try {
    ArrayList<ObservablePropertyListener> listOfObservers = mapOfFieldObservers.get(observer.getPropertyId());
    if (listOfObservers != null) { listOfObservers.remove(observer); }
  }
  catch (Exception e) {
    AndroidUtils.logErr(MyApp,
                        String.format("ObservableProperty.[%s] problem removing listener [%s]",
                                      observer.getPropertyId(),
                                      observer.getName())
    );
  }
}

/**
 * @return if the {@link #mapOfFieldValues} doesn't contain a value for this field,
 * just return the defaultValue
 */
public Object getValue(int propId, Object defaultValue) {
  try {
    return mapOfFieldValues.get(propId) == null ? defaultValue : mapOfFieldValues.get(propId);
  }
  catch (Exception e) {
    return null;
  }
}

/** just a refinement of {@link #getValue(int, Object)} that casts the object as boolean */
public boolean getValueAsBoolean(int propId) {
  try {
    return (Boolean) getValue(propId, Boolean.FALSE);
  }
  catch (Exception e) {
    return false;
  }
}

/**
 * @param defaultValue if this property is not set yet, then assume this defaultValue to be it's current value
 *
 * @return true if the property contains a value and it's equal to the given value
 */

public boolean valueEquals(int propId, Object compareToValue, Object defaultValue) {
  try {
    return getValue(propId, defaultValue).equals(compareToValue);
  }
  catch (Exception e) {return false;}
}

/**
 * saves the value to {@link #mapOfFieldValues},
 * then fires the {@link ObservablePropertyListener#onChange} method
 * (which is run on the main thread via the {@link AppData#handlerMainThread}).
 * <p/>
 * Note that the onChange method is only fired if:
 * <ol>
 * <li>if the value is set for the first time (was null before)</li>
 * <li>if the value is different than what's been set already</li>
 * </ol>
 *
 * @param propertyId this can't be null! will throw IllegalArgumentException if it is.
 * @param value      this can't be null! will throw IllegalArgumentException if it is.
 */
public void setValue(final int propertyId, final Object value) throws IllegalArgumentException {

  // value can't be null
  if (value == null) {
    throw new IllegalArgumentException(
        String.format("value for propertyId [%s] can't be null!",
                      propertyId));
  }

  try {
    // check to see if the value already exists and is the same
    if (mapOfFieldValues.indexOfKey(propertyId) > 0) {
      if (mapOfFieldValues.get(propertyId).equals(value)) { return; }
    }

    // value does not exist, or is not the same, so save it, and send out update event
    mapOfFieldValues.put(propertyId, value);

    if (mapOfFieldObservers.indexOfKey(propertyId) > 0) {
      ArrayList<ObservablePropertyListener> listOfObservers = mapOfFieldObservers.get(propertyId);
      for (final ObservablePropertyListener observer : listOfObservers) {
        handlerMainThread.post(new Runnable() {
          public void run() {
            try {
              observer.onChange(propertyId, value);
            }
            catch (Exception e) {
              AndroidUtils.logErr(
                  MyApp,
                  String.format("ObservableProperty.[%s] problem running propertyId change observer [%s]",
                                propertyId,
                                observer.getName()),
                  e);
            }
            AndroidUtils.log(MyApp,
                             String.format("ObservableProperty.[%s] propertyId changed to [%s], and listener [%s] " +
                                           "fired",
                                           propertyId,
                                           value.toString(),
                                           observer.getName())
            );
          }
        });
      }
    }
    else {
      AndroidUtils.log(MyApp,
                       String.format("ObservableProperty.[%s] propertyId changed to [%s], no listeners fired",
                                     propertyId,
                                     value.toString())
      );

    }
  }
  catch (Exception e) {
    AndroidUtils.logErr(MyApp,
                        String.format("ObservableProperty.[%s] problem setting value [%s]",
                                      propertyId,
                                      value.toString())
    );
  }

}

}//end class ObservablePropertyManager
