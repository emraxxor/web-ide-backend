package hu.emraxxor.web.ide.core.web.validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import hu.emraxxor.web.ide.core.web.BasicSecureFunctions;

public class FileNameValidator implements ConstraintValidator<FileNameConstraint, String> {

  @Override
  public void initialize(FileNameConstraint field) {
  }

  @Override
  public boolean isValid(String field,ConstraintValidatorContext cxt) {
      return field != null && !BasicSecureFunctions.directoryTraversalInputCheckStartsWith(field);
  }

}
