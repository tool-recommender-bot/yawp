package io.yawp.repository.shields.basic;

import io.yawp.commons.http.annotation.GET;
import io.yawp.commons.http.annotation.POST;
import io.yawp.commons.http.annotation.PUT;
import io.yawp.commons.utils.TestLoginManager;
import io.yawp.repository.IdRef;
import io.yawp.repository.actions.basic.ShieldedObjectAction;
import io.yawp.repository.models.basic.ShieldedObject;
import io.yawp.repository.models.basic.facades.ShieldedObjectFacades.AmyFacade;
import io.yawp.repository.shields.Shield;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class ObjectShield extends Shield<ShieldedObject> {

    @Override
    public void always() {
        allow(isJim());
        allow(isAmy()).facade(AmyFacade.class);
        allow(isNat() && isAction(ShieldedObjectAction.class));
    }

    @Override
    public void index(IdRef<?> parentId) {
        allow(isKurt()).where("stringValue", "=", "ok");

        twoWhereClausesTest();
        allowWithoutWhereRemovesOtherWhere();
    }

    private void twoWhereClausesTest() {
        allow(isLinda()).where("intValue", "=", 100);
        allow(isLinda()).where("intValue", "=", 200);
    }

    private void allowWithoutWhereRemovesOtherWhere() {
        allow(isRichey()).where("intValue", "=", 100);
        allow(isRichey());
    }

    @Override
    public void show(IdRef<ShieldedObject> id) {
        allow(isRobert());
        allow(isId100(id));
        allow(isKurt()).where("stringValue", "=", "ok");
    }

    @Override
    public void create(List<ShieldedObject> objects) {
        allow(isRequestWithValidObjects(objects));
        allow(isKurt()).where("stringValue", "=", "ok");
        allow(isJanis()).where("stringValue", "=", "ok-for-janis");
        allow(hasAppliedBeforeShield(objects));
    }

    @Override
    public void update(IdRef<ShieldedObject> id, ShieldedObject object) {
        allow(isRobert());
        allow(isId100(id));
        allow(isRequestWithValidObject(object));
        allow(isKurt()).where("stringValue", "=", "ok");
        allow(isJanis()).where("stringValue", "=", "ok-for-janis");
    }

    @Override
    public void destroy(IdRef<ShieldedObject> id) {
        allow(isId100(id));
        allow(isJanis()).where("stringValue", "=", "ok-for-janis");
    }

    @GET("header")
    public void header() {
        allow();
    }

    @PUT("something")
    public void something(IdRef<ShieldedObject> id) {
        allow(isJanis()).where("stringValue", "=", "ok-for-janis");
    }

    @PUT("anotherthing")
    public void anotherthing(IdRef<ShieldedObject> id, Map<String, String> params) {
        allow(isId100(id));
        allow(params.containsKey("x") && params.get("x").equals("ok"));
    }

    @GET("collection")
    public void collection() {
        allow();
    }

    @POST
    public void multipleWheres() {
        allow(true).where("lala", "=", "lala");
        allow(true).where("xpto", "=", "lala");
    }

    private boolean isRobert() {
        return is("robert");
    }

    private boolean isJim() {
        return is("jim");
    }

    private boolean isKurt() {
        return is("kurt");
    }

    private boolean isJanis() {
        return is("janis");
    }

    private boolean isAmy() {
        return is("amy");
    }

    private boolean isNat() {
        return is("nat");
    }

    private boolean isLinda() {
        return is("linda");
    }

    private boolean isKristen() {
        return is("kristen");
    }

    private boolean isRichey() {
        return is("richey");
    }

    private boolean is(String username) {
        return TestLoginManager.isLogged(username);
    }

    private boolean isId100(IdRef<ShieldedObject> id) {
        return id != null && id.asLong().equals(100L);
    }

    private boolean hasAppliedBeforeShield(List<ShieldedObject> objects) {
        for (ShieldedObject object : objects) {
            if (!object.getStringValue().contains("applied beforeShield")) {
                return false;
            }
        }
        return true;
    }

    private boolean isRequestWithValidObject(ShieldedObject object) {
        if (object == null) {
            return false;
        }
        return isRequestWithValidObjects(Arrays.asList(object));
    }

    private boolean isRequestWithValidObjects(List<ShieldedObject> objects) {
        if (!requestHasAnyObject()) {
            return false;
        }

        for (ShieldedObject objectInList : objects) {
            if (!isValidObject(objectInList)) {
                return false;
            }
        }

        return true;
    }

    private boolean isValidObject(ShieldedObject object) {
        if (object.getStringValue() == null) {
            return false;
        }
        return object.getStringValue().equals("valid object");
    }

    private boolean isAdmin() {
        return TestLoginManager.getLoggedUsername().contains("admin");
    }
}
