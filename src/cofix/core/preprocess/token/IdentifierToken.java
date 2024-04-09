package cofix.core.preprocess.token;

public class IdentifierToken extends AbstractToken{

  public IdentifierToken(String name){
    super();
    _name = name;
    _type = name;
  }

}
