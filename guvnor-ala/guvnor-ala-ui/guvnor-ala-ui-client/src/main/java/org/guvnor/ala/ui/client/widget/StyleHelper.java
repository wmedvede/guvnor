/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates.
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

package org.guvnor.ala.ui.client.widget;

import com.google.gwt.dom.client.Style;
import org.gwtbootstrap3.client.ui.constants.ValidationState;
import org.jboss.errai.common.client.dom.HTMLElement;

/**
 * TODO: update me
 */
public class StyleHelper {

    public static <E extends Style.HasCssName, F extends Enum<? extends Style.HasCssName>> void addUniqueEnumStyleName(final HTMLElement element,
                                                                                                                       final Class<F> enumClass,
                                                                                                                       final E style) {
        removeEnumStyleNames(element,
                             enumClass);
        addEnumStyleName(element,
                         style);
    }

    public static <E extends Enum<? extends Style.HasCssName>> void removeEnumStyleNames(final HTMLElement element,
                                                                                         final Class<E> enumClass) {

        for (final Enum<? extends Style.HasCssName> constant : enumClass.getEnumConstants()) {
            final String cssClass = ((Style.HasCssName) constant).getCssName();

            if (cssClass != null && !cssClass.isEmpty()) {
                element.getClassList().remove(cssClass);
            }
        }
    }

    public static <E extends Style.HasCssName> void addEnumStyleName(final HTMLElement element,
                                                                     final E style) {

        if (style != null && style.getCssName() != null && !style.getCssName().isEmpty()) {
            element.getClassList().add(style.getCssName());
        }
    }

    public static <E extends Style.HasCssName> void removeEnumStyleName(final HTMLElement element,
                                                                        final E style) {

        if (style != null && style.getCssName() != null && !style.getCssName().isEmpty()) {
            element.getClassList().remove(style.getCssName());
        }
    }

    public static void setFormStatus(final HTMLElement form,
                                     final FormStatus status) {
        if (status.equals(FormStatus.ERROR)) {
            addUniqueEnumStyleName(form,
                                   ValidationState.class,
                                   ValidationState.ERROR);
        } else {
            addUniqueEnumStyleName(form,
                                   ValidationState.class,
                                   ValidationState.NONE);
        }
    }
}
