package dk.opendesk.foundationapplication.constraints;

import dk.opendesk.foundationapplication.enums.StateCategory;
import java.util.ArrayList;
import java.util.List;
import org.alfresco.repo.dictionary.constraint.ListOfValuesConstraint;

/**
 *
 * @author martin
 */
public class StateCategoryConstrant extends ListOfValuesConstraint{
    
    @Override
    public void setAllowedValues(List allowedValues) {
    }

    @Override
    public void setCaseSensitive(boolean caseSensitive) {
    }

    @Override
   public void initialize() {
       super.setCaseSensitive(true);
       List<String> categories = new ArrayList<>();
       for(StateCategory category : StateCategory.values()){
           categories.add(category.getCategoryName());
       }
       super.setAllowedValues(categories);
       
    }
    
}
