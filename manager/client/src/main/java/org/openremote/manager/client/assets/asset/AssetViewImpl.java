/*
 * Copyright 2016, OpenRemote Inc.
 *
 * See the CONTRIBUTORS.txt file in the distribution for a
 * full listing of individual contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openremote.manager.client.assets.asset;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.InsertPanel;
import com.google.inject.Provider;
import org.openremote.manager.client.app.dialog.JsonEditor;
import org.openremote.manager.client.assets.attributes.AttributesBrowser;
import org.openremote.manager.client.assets.browser.AssetBrowser;
import org.openremote.manager.client.assets.browser.AssetSelector;
import org.openremote.manager.client.assets.browser.BrowserTreeNode;
import org.openremote.manager.client.i18n.ManagerMessages;
import org.openremote.manager.client.style.WidgetStyle;
import org.openremote.manager.client.widget.*;
import org.openremote.model.Constants;
import org.openremote.model.asset.AssetType;
import org.openremote.model.geo.GeoJSON;
import org.openremote.model.value.ObjectValue;

import javax.inject.Inject;
import java.util.Date;

public class AssetViewImpl extends Composite implements AssetView {

    interface UI extends UiBinder<FlexSplitPanel, AssetViewImpl> {
    }

    interface Style extends CssResource {

        String navItem();

        String mapWidget();
    }

    interface AttributesBrowserStyle extends CssResource, AttributesBrowser.Style {

        String numberEditor();

        String stringEditor();

        String booleanEditor();

        String regularAttribute();

        String highlightAttribute();
    }

    @UiField
    public WidgetStyle widgetStyle;

    @UiField
    public ManagerMessages managerMessages;

    @UiField
    Style style;

    @UiField
    AttributesBrowserStyle attributesBrowserStyle;

    @UiField
    FlexSplitPanel splitPanel;

    @UiField
    HTMLPanel sidebarContainer;

    @UiField
    Headline headline;

    @UiField
    Hyperlink editAssetLink;

    /* ############################################################################ */

    @UiField
    public Form form;

    @UiField
    FormGroup createdOnGroup;
    @UiField
    FormOutputText createdOnOutput;
    @UiField
    FormButton showHistoryButton;

    @UiField
    FormGroup parentGroup;
    @UiField
    FormOutputText tenantDisplayName;
    @UiField
    FormInputText parentAssetName;

    @UiField
    FormGroup locationGroup;
    @UiField
    FormOutputLocation locationOutput;
    @UiField
    FormButton centerMapButton;

    @UiField
    MapWidget mapWidget;

    /* ############################################################################ */

    @UiField
    PushButton refreshButton;

    @UiField
    PushButton liveUpdatesOnButton;
    @UiField
    PushButton liveUpdatesOffButton;

    @UiField
    FlowPanel attributesBrowserContainer;

    /* ############################################################################ */

    final AssetBrowser assetBrowser;
    final Provider<JsonEditor> jsonEditorProvider;
    Presenter presenter;
    AttributesBrowser attributesBrowser;

    @Inject
    public AssetViewImpl(AssetBrowser assetBrowser,
                         Provider<JsonEditor> jsonEditorProvider) {
        this.assetBrowser = assetBrowser;
        this.jsonEditorProvider = jsonEditorProvider;

        UI ui = GWT.create(UI.class);
        initWidget(ui.createAndBindUi(this));

        splitPanel.setOnResize(() -> mapWidget.resize());

        setFormBusy(true);
    }

    @Override
    public void setPresenter(Presenter presenter) {
        this.presenter = presenter;

        // Restore initial state of view
        sidebarContainer.clear();
        setFormBusy(true);
        headline.setText(null);
        headline.setSub(null);
        editAssetLink.setVisible(false);
        createdOnOutput.setText(null);
        tenantDisplayName.setText(null);
        parentAssetName.setText(null);
        locationGroup.setVisible(false);
        locationOutput.setCoordinates(null, null);
        mapWidget.setVisible(false);
        showDroppedPin(GeoJSON.EMPTY_FEATURE_COLLECTION);
        attributesBrowserContainer.clear();
        attributesBrowser = null;

        if (presenter != null) {
            assetBrowser.asWidget().removeFromParent();
            sidebarContainer.add(assetBrowser.asWidget());
        }
    }

    @Override
    public void setFormBusy(boolean busy) {
        headline.setVisible(!busy);
        form.setBusy(busy);
        if (!busy && locationGroup.isVisible()) {
            mapWidget.setVisible(true);
            mapWidget.resize();
        } else {
            mapWidget.setVisible(false);
        }
        editAssetLink.setVisible(!busy);
    }

    /* ############################################################################ */

    @Override
    public void setAssetEditHistoryToken(String token) {
        editAssetLink.setTargetHistoryToken(token);
    }

    @Override
    public void setName(String name) {
        headline.setText(name);
    }

    @Override
    public void setCreatedOn(Date createdOn) {
        createdOnOutput.setText(
            createdOn != null ? DateTimeFormat.getFormat(Constants.DEFAULT_DATETIME_FORMAT).format(createdOn) : ""
        );
    }

    @Override
    public void setParentNode(BrowserTreeNode treeNode) {
        AssetSelector.renderTreeNode(managerMessages, treeNode, tenantDisplayName, parentAssetName);
    }

    /* ############################################################################ */

    @Override
    public void setLocation(double[] coordinates) {
        if (locationOutput.setCoordinates(managerMessages.selectLocation(), coordinates)) {
            locationGroup.setVisible(true);
            mapWidget.setVisible(true);
            mapWidget.resize();
        } else {
            locationGroup.setVisible(false);
            mapWidget.setVisible(false);
        }
    }

    @Override
    public void initialiseMap(ObjectValue mapOptions) {
        mapWidget.initialise(mapOptions, () -> {
            mapWidget.addNavigationControl();
            if (presenter != null)
                presenter.onMapReady();
        });
    }

    @Override
    public boolean isMapInitialised() {
        return mapWidget.isInitialised();
    }

    @Override
    public void showDroppedPin(GeoJSON geoFeature) {
        if (mapWidget.isMapReady()) {
            mapWidget.showFeature(MapWidget.FEATURE_SOURCE_DROPPED_PIN, geoFeature);
        }
    }

    @Override
    public void flyTo(double[] coordinates) {
        if (mapWidget.isMapReady()) {
            mapWidget.flyTo(coordinates);
        }
    }

    @UiHandler("centerMapButton")
    void centerMapClicked(ClickEvent e) {
        if (presenter != null)
            presenter.centerMap();
    }

    /* ############################################################################ */

    @Override
    public void setIconAndType(String icon, String type) {
        headline.setIcon(icon);
        // TODO: Should unknown/undefined asset type default to custom
        AssetType assetType = AssetType.getByValue(type).orElse(AssetType.CUSTOM);
        if (assetType == AssetType.CUSTOM) {
            headline.setSub(type);
        } else {
            headline.setSub(managerMessages.assetTypeLabel(assetType.name()));
        }
    }

    @Override
    public AttributesBrowser.Container getAttributesBrowserContainer() {
        return new AttributesBrowser.Container() {

            @Override
            public AttributesBrowser.Style getStyle() {
                return attributesBrowserStyle;
            }

            @Override
            public InsertPanel getPanel() {
                return attributesBrowserContainer;
            }

            @Override
            public JsonEditor getJsonEditor() {
                return jsonEditorProvider.get();
            }

            @Override
            public ManagerMessages getMessages() {
                return managerMessages;
            }
        };
    }

    @Override
    public void setAttributesBrowser(AttributesBrowser browser) {
        this.attributesBrowser = browser;
        attributesBrowserContainer.clear();
    }

    @UiHandler("refreshButton")
    public void onRefreshButtonClicked(ClickEvent e) {
        if (presenter != null)
            presenter.refresh();
    }

    @Override
    public boolean isLiveUpdatesEnabled() {
        return liveUpdatesOnButton.isVisible();
    }

    @UiHandler("liveUpdatesOnButton")
    public void onLiveUpdatesOnButtonClicked(ClickEvent e) {
        if (presenter != null)
            presenter.enableLiveUpdates(false);
        liveUpdatesOnButton.setVisible(false);
        liveUpdatesOffButton.setVisible(true);
    }

    @UiHandler("liveUpdatesOffButton")
    public void onLiveUpdatesOffButtonClicked(ClickEvent e) {
        if (presenter != null)
            presenter.enableLiveUpdates(true);
        liveUpdatesOnButton.setVisible(true);
        liveUpdatesOffButton.setVisible(false);
    }
}
