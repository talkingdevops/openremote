package org.openremote.manager.shared.device;

import org.openremote.model.attribute.Attribute;

import java.util.List;

// TODO Not used
public interface CapabilityDefinition {
    String getType();

    String getDescription();

    List<Attribute> getResources();
}
