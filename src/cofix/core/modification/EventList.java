package cofix.core.modification;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import org.eclipse.jdt.core.dom.ASTNode;

public class EventList {
  private List<ModificationEvent> _list;

  public EventList(){
    _list = new ArrayList<>(10);
  }

  public EventList(ModificationEvent event){
    _list = new ArrayList<>(10);
    _list.add(event);
  }

  public void add(ModificationEvent event){
    _list.add(event);
  }

  public List<ModificationEvent> getList(){
    return _list;
  }

  public void clear(){
    _list.clear();
  }

  private void sortEvents(){
    _list.sort(Comparator.comparing(ModificationEvent::getPriority));
  }

  public List<Modification> tryApply(ASTNode sourceRoot, BuggyFile buggyFile, Set<String> availableVars)
      throws Exception {
    this.sortEvents();

    List<Modification> modifications = new ArrayList<>(10);

    for(ModificationEvent event : _list){
      event.tryApply(sourceRoot, buggyFile, availableVars, modifications);
    }
    return modifications;
  }

}
