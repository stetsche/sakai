package org.sakaiproject.webapi.beans;

import java.lang.reflect.InvocationTargetException;

import org.apache.commons.beanutils.BeanUtils;
import org.sakaiproject.condition.api.model.Condition;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
//@EqualsAndHashCode(callSuper = true)
public class ConditionRestBean {


    private boolean used;


    public ConditionRestBean(Condition condition, boolean used) {
        // try {
        //     BeanUtils.copyProperties(this, condition);
        // } catch (IllegalAccessException | InvocationTargetException e) {
        //     // Do nothing
        // }

        this.used = used;

    }
}
