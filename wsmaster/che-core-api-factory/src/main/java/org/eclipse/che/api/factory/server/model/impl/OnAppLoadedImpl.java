/*******************************************************************************
 * Copyright (c) 2012-2017 Red Hat, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Red Hat, Inc. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.api.factory.server.model.impl;

import org.eclipse.che.api.core.model.factory.Action;
import org.eclipse.che.api.core.model.factory.OnAppLoaded;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;
import static javax.persistence.CascadeType.ALL;

/**
 * Data object for {@link OnAppLoaded}.
 *
 * @author Anton Korneta
 */
@Entity(name = "OnAppLoaded")
@Table(name = "che_factory_on_app_loaded_action")
public class OnAppLoadedImpl implements OnAppLoaded {

    @Id
    @GeneratedValue
    @Column(name = "id")
    private Long id;

    @OneToMany(cascade = ALL, orphanRemoval = true)
    @JoinTable(name = "che_factory_on_app_loaded_action_value",
               joinColumns = @JoinColumn(name = "on_app_loaded_id"),
               inverseJoinColumns = @JoinColumn(name = "action_entity_id"))
    private List<ActionImpl> actions;

    public OnAppLoadedImpl() {}

    public OnAppLoadedImpl(List<? extends Action> actions) {
        if (actions != null) {
            this.actions = actions.stream()
                                  .map(ActionImpl::new)
                                  .collect(toList());
        }
    }

    public OnAppLoadedImpl(OnAppLoaded onAppLoaded) {
        this(onAppLoaded.getActions());
    }

    @Override
    public List<ActionImpl> getActions() {
        if (actions == null) {
            return new ArrayList<>();
        }
        return actions;
    }

    public void setActions(List<ActionImpl> actions) {
        this.actions = actions;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof OnAppLoadedImpl)) {
            return false;
        }
        final OnAppLoadedImpl that = (OnAppLoadedImpl)obj;
        return Objects.equals(id, that.id)
               && getActions().equals(that.getActions());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + Objects.hashCode(id);
        hash = 31 * hash + getActions().hashCode();
        return hash;
    }

    @Override
    public String toString() {
        return "OnAppLoadedImpl{" +
               "id=" + id +
               ", actions=" + actions +
               '}';
    }
}
