/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package loci.nodescheduler.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 *
 * @author aivar
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Input {
    Img[] value() default { @Img };
}
