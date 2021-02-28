package ua.hazelcast.cluster.deployment.validation;

import org.springframework.beans.factory.annotation.Autowired;
import ua.hazelcast.cluster.deployment.repository.DeploymentRepository;

import javax.validation.*;
import javax.validation.constraints.NotNull;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;

@NotNull
@ReportAsSingleViolation
@Target({ElementType.FIELD, ANNOTATION_TYPE, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = DeploymentUniqueName.Validator.class)
public @interface DeploymentUniqueName {

    String message() default "Deployment with the name ${validatedValue} already exists";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<DeploymentUniqueName, String> {

        @Autowired
        private DeploymentRepository deploymentRepository;

        @Override
        public boolean isValid(final String deploymentName, final ConstraintValidatorContext context) {
            if (deploymentName == null) {
                return true;
            }
            return deploymentRepository.findByName(deploymentName).isEmpty();
        }
    }
}
