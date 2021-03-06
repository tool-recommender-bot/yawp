package io.yawp.repository.hooks.basic;

import io.yawp.repository.IdRef;
import io.yawp.repository.hooks.Hook;
import io.yawp.repository.models.basic.BasicObject;
import io.yawp.repository.models.basic.HookedObject;
import io.yawp.repository.query.QueryBuilder;
import io.yawp.repository.query.QueryType;

import java.util.List;

public class SpecifObjectHook extends Hook<HookedObject> {

    @Override
    public void beforeSave(HookedObject object) {
        if (!isBeforeSaveTest(object)) {
            return;
        }
        object.setStringValue("xpto before save");
    }

    @Override
    public void afterSave(HookedObject object) {
        if (!isAfterSaveTest(object)) {
            return;
        }
        object.setStringValue("xpto after save");
    }

    @Override
    public void beforeQuery(QueryBuilder<HookedObject> q) {
        if (HookTest.LoggedUser.filter == null) {
            return;
        }
        q.where("stringValue", "=", HookTest.LoggedUser.filter);
    }

    @Override
    public void beforeDestroy(IdRef<HookedObject> id) {
        HookedObject object = id.fetch();
        yawp.save(new BasicObject(object.getStringValue() + ": " + id));
    }

    @Override
    public void afterDestroy(IdRef<HookedObject> id) {
        yawp.save(new BasicObject("afterDestroy test: " + id));
    }

    @Override
    public void afterQuery(QueryBuilder<HookedObject> q) {
        String msg = q.getExecutedQueryType() == QueryType.FETCH ? ((HookedObject) q.getExecutedResponse()).getId().toString() : String.valueOf(((List<?>) q.getExecutedResponse()).size());
        HookTest.AfterQueryTest.msgs.add("type: " + q.getExecutedQueryType() + " | obj: " + msg);
    }

    private boolean isBeforeSaveTest(HookedObject object) {
        return object.getStringValue() != null && object.getStringValue().equals("before_save");
    }

    private boolean isAfterSaveTest(HookedObject object) {
        return object.getStringValue() != null && object.getStringValue().equals("after_save");
    }
}
