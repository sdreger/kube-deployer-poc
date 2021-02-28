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
@Constraint(validatedBy = DeploymentExists.Validator.class)
public @interface DeploymentExists {

    String message() default "Deployment with ID ${validatedValue} doesn't exist";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    class Validator implements ConstraintValidator<DeploymentExists, Long> {

        @Autowired
        private DeploymentRepository deploymentRepository;

        @Override
        public boolean isValid(final Long deploymentId, final ConstraintValidatorContext context) {
            if (deploymentId == null) {
                return true;
            }
            return deploymentRepository.findById(deploymentId).isPresent();
        }
    }
}
