/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dk.opendesk.foundationapplication.DAO;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 *
 * @author martin
 */
public class ApplicationPropertiesContainer {
    private String id;
    private String label;
    private String layout;
    private List<ApplicationPropertyValue> properties = new ArrayList<>();

    public ApplicationPropertiesContainer() {
    }
    
    public ApplicationPropertiesContainer(String id, String label, String layout) {
        this.id = id;
        this.label = label;
        this.layout = layout;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getLayout() {
        return layout;
    }

    public void setLayout(String layout) {
        this.layout = layout;
    }

    
    public List<ApplicationPropertyValue> getProperties() {
        return properties;
    }

    public void setProperties(List<ApplicationPropertyValue> properties) {
        this.properties = properties;
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE);
        builder.append("id", id).append("label", label).append("layout", layout).append("properties", properties);
        return builder.toString();
    }
    
    
}
