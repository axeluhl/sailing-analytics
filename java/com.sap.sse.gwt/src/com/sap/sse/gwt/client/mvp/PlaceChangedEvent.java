/*
 * Copyright 2010 Google Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.sap.sse.gwt.client.mvp;

import com.google.gwt.event.shared.EventHandler;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.place.shared.Place;

/**
 * Event thrown after the place changed.
 */
public class PlaceChangedEvent extends GwtEvent<PlaceChangedEvent.Handler> {

  /**
   * Implemented by handlers of PlaceChangedEvent.
   */
  public interface Handler extends EventHandler {
    /**
     * Called when a {@link PlaceChangedEvent} is fired.
     *
     * @param event the {@link PlaceChangedEvent}
     */
    void onPlaceChanged(PlaceChangedEvent event);
  }

  /**
   * A singleton instance of Type&lt;Handler&gt;.
   */
  public static final Type<Handler> TYPE = new Type<Handler>();

  private final Place newPlace;

  /**
   * Constructs a PlaceChangedEvent for the given {@link Place}.
   *
   * @param newPlace a {@link Place} instance
   */
  public PlaceChangedEvent(Place newPlace) {
    this.newPlace = newPlace;
  }

  @Override
  public Type<Handler> getAssociatedType() {
    return TYPE;
  }

  /**
   * Return the new {@link Place}.
   *
   * @return a {@link Place} instance
   */
  public Place getNewPlace() {
    return newPlace;
  }

  @Override
  protected void dispatch(Handler handler) {
    handler.onPlaceChanged(this);
  }
}
