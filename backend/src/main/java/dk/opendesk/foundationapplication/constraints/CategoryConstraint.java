package dk.opendesk.foundationapplication.constraints;

import java.util.Arrays;
import java.util.List;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;

/**
 *
 * @author martin
 */
public class CategoryConstraint extends ListOfValuesConstraint{
    
    @Override
    public void setAllowedValues(List allowedValues) {
    }

    @Override
    public void setCaseSensitive(boolean caseSensitive) {
    }

    @Override
   public void initialize() {
       super.setCaseSensitive(true);
       super.setAllowedValues(Arrays.asList(new String[]{"Category1", "Category2", "Category3"}));
       
    }
    
}
